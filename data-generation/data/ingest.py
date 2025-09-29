import requests
import json
import os
import zipfile
import asyncio
import aiohttp
from tqdm import tqdm
from rapidfuzz import process

# --- KONFIG ---
LIVS_URL = "https://dataportal.livsmedelsverket.se/livsmedel/api/v1"

# USDA dataset-länkar
USDA_URLS = {
    "foundation": "https://fdc.nal.usda.gov/fdc-datasets/FoodData_Central_foundation_food_json_2021-10-28.zip",
    "sr_legacy": "https://fdc.nal.usda.gov/fdc-datasets/FoodData_Central_sr_legacy_food_json_2021-10-28.zip",
    # Branded foods disabled by default - contains ~1.2M items (2.69 GB)
    # "branded": "https://fdc.nal.usda.gov/fdc-datasets/FoodData_Central_branded_food_json_2021-10-28.zip",
}

# Optional: Enable branded foods (WARNING: ~1.2M items, very slow!)
INCLUDE_BRANDED_FOODS = False

# Concurrent processing configuration
MAX_CONCURRENT_REQUESTS = 40  # Number of simultaneous API calls
BATCH_SIZE = 100              # Process foods in batches of this size
REQUEST_DELAY = 0.1          # Delay between requests (seconds) to be API-friendly

OUTPUT_FILE = "merged_livsmedel_usda.json"


# --- HJÄLPA FUNKTIONER ---

def download_file(url, dest):
    """Laddar ner en fil med progressbar"""
    resp = requests.get(url, stream=True)
    total = int(resp.headers.get("content-length", 0))
    with open(dest, "wb") as f, tqdm(
        desc=f"Laddar ner {os.path.basename(dest)}",
        total=total,
        unit="B",
        unit_scale=True,
        unit_divisor=1024
    ) as bar:
        for data in resp.iter_content(chunk_size=1024):
            size = f.write(data)
            bar.update(size)


def unzip_file(zip_path, extract_to="."):
    """Packar upp en zip-fil"""
    with zipfile.ZipFile(zip_path, "r") as zip_ref:
        zip_ref.extractall(extract_to)
    # returnera första json-filens path
    for name in zip_ref.namelist():
        if name.endswith(".json"):
            return os.path.join(extract_to, name)
    return None


def fetch_all_livsmedel():
    """Hämtar alla livsmedel från Livsmedelsverket (med paginering) - limit 2569 för att få all data."""
    all_items = []
    url = f"{LIVS_URL}/livsmedel?limit=2569"
    pbar = tqdm(desc="Hämtar lista från Livsmedelsverket", unit="page")
    while url:
        resp = requests.get(url)
        resp.raise_for_status()
        data = resp.json()
        all_items.extend(data.get("livsmedel", []))
        url = data.get("nextPageUrl")
        pbar.update(1)
    pbar.close()
    return all_items


def fetch_details_livsmedel(livs_id):
    """Hämtar detaljer + näringsvärden för ett livsmedel i både svenska och engelska."""
    # Hämta svenska detaljer (sprak=1)
    detaljer_sv = requests.get(f"{LIVS_URL}/livsmedel/{livs_id}?sprak=1").json()
    
    # Hämta engelska detaljer (sprak=2) 
    detaljer_en = requests.get(f"{LIVS_URL}/livsmedel/{livs_id}?sprak=2").json()
    
    # Hämta näringsvärden i svenska (sprak=1)
    naringsvarden_sv = requests.get(f"{LIVS_URL}/livsmedel/{livs_id}/naringsvarden?sprak=1").json()
    
    # Hämta näringsvärden i engelska (sprak=2)
    naringsvarden_en = requests.get(f"{LIVS_URL}/livsmedel/{livs_id}/naringsvarden?sprak=2").json()
    
    # Matcha näringsämnen baserat på euroFIRkod för korrekt mappning
    matched_nutrients = match_nutrients(naringsvarden_sv, naringsvarden_en)
    
    return {
        "detaljer_sv": detaljer_sv,
        "detaljer_en": detaljer_en, 
        "naringsvarden_sv": naringsvarden_sv,
        "naringsvarden_en": naringsvarden_en,
        "naringsvarden_matched": matched_nutrients
    }


def match_nutrients(nutrients_sv, nutrients_en):
    """Matchar näringsämnen mellan svenska och engelska baserat på euroFIRkod."""
    # Skapa lookup för engelska näringsämnen baserat på euroFIRkod
    en_lookup = {}
    for nutrient in nutrients_en:
        code = nutrient.get("euroFIRkod", "")
        if code:
            en_lookup[code] = nutrient
    
    # Matcha svenska näringsämnen med engelska
    matched = []
    for sv_nutrient in nutrients_sv:
        code = sv_nutrient.get("euroFIRkod", "")
        en_nutrient = en_lookup.get(code)
        
        matched_nutrient = {
            "euroFIRkod": code,
            "sv": sv_nutrient,
            "en": en_nutrient if en_nutrient else None,
            "matched": en_nutrient is not None
        }
        matched.append(matched_nutrient)
    
    return matched


def match_classifications(classifications_sv, classifications_en):
    """Matchar klassificeringar mellan svenska och engelska baserat på typ och fasettkod."""
    # Skapa lookup för engelska klassificeringar baserat på typ och fasettkod
    en_lookup = {}
    for classification in classifications_en:
        typ = classification.get("typ", "")
        fasettkod = classification.get("fasettkod", "")
        key = f"{typ}|{fasettkod}"
        if key:
            en_lookup[key] = classification
    
    # Matcha svenska klassificeringar med engelska
    matched = []
    for sv_classification in classifications_sv:
        typ = sv_classification.get("typ", "")
        fasettkod = sv_classification.get("fasettkod", "")
        key = f"{typ}|{fasettkod}"
        en_classification = en_lookup.get(key)
        
        matched_classification = {
            "typ": typ,
            "fasettkod": fasettkod,
            "sv": sv_classification,
            "en": en_classification if en_classification else None,
            "matched": en_classification is not None
        }
        matched.append(matched_classification)
    
    return matched


async def fetch_details_livsmedel_async(session, livs_id, semaphore):
    """Async version: Hämtar detaljer + näringsvärden + klassificeringar + råvaror för ett livsmedel i både svenska och engelska."""
    async with semaphore:  # Limit concurrent requests
        try:
            # Small delay to be API-friendly
            await asyncio.sleep(REQUEST_DELAY)
            
            # All 8 API calls concurrently (was 4, now 8 with classifications + raw materials)
            tasks = [
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}?sprak=1"),                    # Swedish details
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}?sprak=2"),                    # English details  
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/naringsvarden?sprak=1"),      # Swedish nutrients
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/naringsvarden?sprak=2"),      # English nutrients
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/klassificeringar?sprak=1"),   # Swedish classifications
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/klassificeringar?sprak=2"),   # English classifications
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/ravaror?sprak=1"),            # Swedish raw materials
                session.get(f"{LIVS_URL}/livsmedel/{livs_id}/ravaror?sprak=2")             # English raw materials
            ]
            
            responses = await asyncio.gather(*tasks, return_exceptions=True)
            
            # Parse JSON responses with error handling for each endpoint
            async def safe_json_parse(response, default_value):
                try:
                    if isinstance(response, Exception):
                        return default_value
                    if hasattr(response, 'json'):
                        return await response.json()
                    return default_value
                except:
                    return default_value
            
            # Parse all responses
            detaljer_sv = await safe_json_parse(responses[0], {"namn": ""})
            detaljer_en = await safe_json_parse(responses[1], {"namn": ""})
            naringsvarden_sv = await safe_json_parse(responses[2], [])
            naringsvarden_en = await safe_json_parse(responses[3], [])
            klassificeringar_sv = await safe_json_parse(responses[4], [])
            klassificeringar_en = await safe_json_parse(responses[5], [])
            ravaror_sv = await safe_json_parse(responses[6], [])
            ravaror_en = await safe_json_parse(responses[7], [])
            
            # Match nutrients using existing logic
            matched_nutrients = match_nutrients(naringsvarden_sv, naringsvarden_en)
            
            # Match classifications by typ and fasettkod
            matched_classifications = match_classifications(klassificeringar_sv, klassificeringar_en)
            
            return {
                "detaljer_sv": detaljer_sv,
                "detaljer_en": detaljer_en, 
                "naringsvarden_sv": naringsvarden_sv,
                "naringsvarden_en": naringsvarden_en,
                "naringsvarden_matched": matched_nutrients,
                "klassificeringar_sv": klassificeringar_sv,
                "klassificeringar_en": klassificeringar_en,
                "klassificeringar_matched": matched_classifications,
                "ravaror_sv": ravaror_sv,
                "ravaror_en": ravaror_en
            }
            
        except Exception as e:
            print(f"   ⚠️ Error fetching details for food #{livs_id}: {e}")
            # Return empty structure to avoid breaking the processing
            return {
                "detaljer_sv": {"namn": ""},
                "detaljer_en": {"namn": ""}, 
                "naringsvarden_sv": [],
                "naringsvarden_en": [],
                "naringsvarden_matched": [],
                "klassificeringar_sv": [],
                "klassificeringar_en": [],
                "klassificeringar_matched": [],
                "ravaror_sv": [],
                "ravaror_en": []
            }


async def process_food_batch_async(session, semaphore, food_batch, usda_lookup, pbar):
    """Process a batch of foods concurrently."""
    batch_results = []
    batch_matched_count = 0
    
    # Create tasks for the entire batch
    tasks = []
    for item in food_batch:
        task = fetch_details_livsmedel_async(session, item["nummer"], semaphore)
        tasks.append((item, task))
    
    # Execute all tasks concurrently
    for item, task in tasks:
        try:
            lid = item["nummer"]
            details = await task
            
            namn_sv = details["detaljer_sv"].get("namn", "")
            namn_en = details["detaljer_en"].get("namn", "")
            
            # USDA matching (this is fast, so keep it synchronous)
            usda_match = match_to_usda(namn_en, usda_lookup)
            is_matched = usda_match is not None
            
            if is_matched:
                batch_matched_count += 1
            
            # Create comprehensive entries with ALL data including classifications
            en_entry = {
                "nummer": lid,
                "language": "en",
                "name": namn_en,
                "alt_name": namn_sv,
                "food_type": details["detaljer_en"].get("livsmedelsTyp", ""),
                "project": details["detaljer_en"].get("projekt", ""),
                "scientific_name": details["detaljer_en"].get("vetenskapligtNamn", ""),
                "livsmedelsverket_details": details["detaljer_en"],
                "nutritional_data": details["naringsvarden_en"], 
                "nutritional_data_matched": details["naringsvarden_matched"],
                "classifications": details["klassificeringar_en"],
                "classifications_matched": details["klassificeringar_matched"],
                "raw_materials": details["ravaror_en"],
                "usda_match": usda_match,
                "matched": is_matched
            }
            
            sv_entry = {
                "nummer": lid,
                "language": "sv", 
                "name": namn_sv,
                "alt_name": namn_en,
                "food_type": details["detaljer_sv"].get("livsmedelsTyp", ""),
                "project": details["detaljer_sv"].get("projekt", ""),
                "scientific_name": details["detaljer_sv"].get("vetenskapligtNamn", ""),
                "livsmedelsverket_details": details["detaljer_sv"],
                "nutritional_data": details["naringsvarden_sv"],
                "nutritional_data_matched": details["naringsvarden_matched"],
                "classifications": details["klassificeringar_sv"],
                "classifications_matched": details["klassificeringar_matched"],
                "raw_materials": details["ravaror_sv"],
                "usda_match": usda_match,
                "matched": is_matched
            }
            
            batch_results.extend([en_entry, sv_entry])
            
            # Update progress
            current_name = namn_sv[:25] if namn_sv else f"Food #{lid}"
            pbar.set_postfix_str(f"{current_name}... {'✓' if is_matched else '○'}")
            pbar.update(1)
            
        except Exception as e:
            print(f"   ⚠️ Error processing food #{item.get('nummer', '?')}: {e}")
            pbar.update(1)
    
    return batch_results, batch_matched_count


def load_usda_json(path):
    """Läser in USDA JSON dump och extraherar food arrays från olika dataset-strukturer."""
    import os
    
    # Visa filstorlek och ladda med progressbar
    file_size = os.path.getsize(path)
    file_size_mb = file_size / (1024 * 1024)
    print(f"   📁 Läser JSON-fil: {os.path.basename(path)} ({file_size_mb:.1f} MB)")
    
    with tqdm(desc=f"   📂 Laddar {os.path.basename(path)}", 
              unit="MB", 
              unit_scale=True,
              total=file_size_mb) as pbar:
        with open(path, "r", encoding="utf-8") as f:
            # Läs filen i chunks för att visa progress
            content = ""
            chunk_size = 1024 * 1024  # 1MB chunks
            while True:
                chunk = f.read(chunk_size)
                if not chunk:
                    break
                content += chunk
                pbar.update(len(chunk.encode('utf-8')) / (1024 * 1024))
            
            data = json.loads(content)
    
    # USDA datasets har olika top-level nycklar
    food_arrays = []
    possible_keys = ["FoundationFoods", "SRLegacyFoods", "BrandedFoods", "foods"]
    
    print("   🔍 Analyserar JSON-struktur...")
    for key in possible_keys:
        if key in data:
            foods = data[key]
            if isinstance(foods, list):
                food_arrays.extend(foods)
                print(f"   🍎 Hittade {len(foods)} livsmedel under '{key}'")
    
    # Om inga kända nycklar hittades, kolla om data själv är en lista
    if not food_arrays and isinstance(data, list):
        food_arrays = data
        print(f"   🍎 Data är direkt en lista med {len(food_arrays)} livsmedel")
    
    # Om fortfarande inga foods hittades, visa vad som finns i JSON:en
    if not food_arrays:
        print(f"   ❌ Hittade inga livsmedel! JSON struktur: {list(data.keys()) if isinstance(data, dict) else type(data)}")
    
    return food_arrays


def build_usda_lookup(usda_foods, lookup=None):
    """Bygger eller uppdaterar en lookup med USDA-data."""
    if lookup is None:
        lookup = {}
    
    if not isinstance(usda_foods, list):
        print(f"   ⚠️ Varning: USDA data är inte en lista, typ: {type(usda_foods)}")
        return lookup
    
    print(f"   🔨 Bygger lookup från {len(usda_foods)} USDA-livsmedel...")
    
    with tqdm(desc="   📚 Bygger USDA lookup", 
              total=len(usda_foods), 
              unit="foods") as pbar:
        valid_count = 0
        for item in usda_foods:
            if not isinstance(item, dict):
                pbar.update(1)
                continue
                
            name = item.get("description", "")
            if name:
                lookup[name] = item
                valid_count += 1
            pbar.update(1)
    
    print(f"   ✅ Lookup komplett: {valid_count} giltiga livsmedel")
    return lookup


def match_to_usda(name, usda_lookup, limit=1):
    """Matchar namn mot USDA med fuzzy string matchning."""
    if not name:
        return None
    matches = process.extract(name, usda_lookup.keys(), limit=limit)
    if matches:
        best_match, score, _ = matches[0]
        if score > 70:  # bara ta rimliga träffar
            return usda_lookup[best_match]
    return None


async def process_all_foods_concurrent_streaming(livs_items, usda_lookup, output_file):
    """Process all foods using concurrent batch processing with STREAMING output to avoid memory issues."""
    print(f"🚀 CONCURRENT MODE: {MAX_CONCURRENT_REQUESTS} simultaneous requests, {BATCH_SIZE} foods per batch")
    print(f"📊 Data collected per food: 8 API endpoints (details, nutrients, classifications, raw materials in SV+EN)")
    print(f"💾 STREAMING MODE: Each batch written immediately to avoid memory buildup")
    print(f"⚡ Expected speedup: 5-10x faster than sequential processing")
    
    total_matched_count = 0
    total_entries_written = 0
    
    # Create batches
    batches = [livs_items[i:i+BATCH_SIZE] for i in range(0, len(livs_items), BATCH_SIZE)]
    print(f"📦 Split {len(livs_items)} foods into {len(batches)} batches")
    
    # Setup async HTTP session and semaphore
    connector = aiohttp.TCPConnector(limit=MAX_CONCURRENT_REQUESTS, limit_per_host=MAX_CONCURRENT_REQUESTS)
    timeout = aiohttp.ClientTimeout(total=30)  # 30 second timeout per request
    semaphore = asyncio.Semaphore(MAX_CONCURRENT_REQUESTS)
    
    # Open output file for streaming JSON writing
    with open(output_file, "w", encoding="utf-8") as f:
        # Write JSON array opening bracket
        f.write("[\n")
        f.flush()
        
        async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
            
            # Progress bar for all foods
            with tqdm(total=len(livs_items), 
                      desc="🚀 Concurrent processing", 
                      unit="foods",
                      bar_format="{desc}: {percentage:3.0f}%|{bar}| {n_fmt}/{total_fmt} [{elapsed}<{remaining}, {rate_fmt}]") as pbar:
                
                # Process batches sequentially (but foods within batch are concurrent)
                for batch_idx, batch in enumerate(batches):
                    batch_start_time = asyncio.get_event_loop().time()
                    
                    # Update progress description with batch info
                    pbar.set_description(f"🚀 Batch {batch_idx+1}/{len(batches)} ({len(batch)} foods)")
                    
                    # Process the batch concurrently
                    batch_results, batch_matched = await process_food_batch_async(
                        session, semaphore, batch, usda_lookup, pbar
                    )
                    
                    # Write batch results immediately to file (streaming)
                    for i, entry in enumerate(batch_results):
                        # Add comma before entry (except for very first entry)
                        if total_entries_written > 0:
                            f.write(",\n")
                        
                        # Write the entry as formatted JSON
                        json.dump(entry, f, ensure_ascii=False, indent=2)
                        f.flush()  # Force write to disk
                        total_entries_written += 1
                    
                    total_matched_count += batch_matched
                    
                    # Show batch completion stats
                    batch_time = asyncio.get_event_loop().time() - batch_start_time
                    foods_per_sec = len(batch) / batch_time if batch_time > 0 else 0
                    match_rate = (total_matched_count / pbar.n) * 100 if pbar.n > 0 else 0
                    file_size_mb = f.tell() / (1024 * 1024)
                    
                    print(f"   ✅ Batch {batch_idx+1} complete: {len(batch)} foods in {batch_time:.1f}s "
                          f"({foods_per_sec:.1f} foods/sec, {match_rate:.1f}% matched)")
                    print(f"   💾 Written {len(batch_results)} entries to file ({file_size_mb:.1f} MB total)")
        
        # Write JSON array closing bracket
        f.write("\n]")
        f.flush()
    
    return total_entries_written, total_matched_count


# --- HUVUDPROGRAM ---

def main():
    # Steg 1: ladda ner & packa upp alla USDA-filer
    print("🗂️  USDA Dataset Configuration:")
    print(f"   📊 Foundation Foods: ✅ Enabled (~340 high-quality foods)")
    print(f"   📚 SR Legacy Foods: ✅ Enabled (~7,700 classic USDA foods)")
    print(f"   🏪 Branded Foods: {'✅ Enabled' if INCLUDE_BRANDED_FOODS else '❌ Disabled'} (~1.2M commercial products)")
    if not INCLUDE_BRANDED_FOODS:
        print(f"      💡 Branded foods disabled for performance (enable with INCLUDE_BRANDED_FOODS=True)")
    
    # Add branded foods to URLs if enabled
    urls_to_process = USDA_URLS.copy()
    if INCLUDE_BRANDED_FOODS:
        urls_to_process["branded"] = "https://fdc.nal.usda.gov/fdc-datasets/FoodData_Central_branded_food_json_2021-10-28.zip"
    
    usda_lookup = {}
    for dataset, url in urls_to_process.items():
        zip_path = f"{dataset}.zip"

        if not os.path.exists(zip_path):
            print(f"⬇️ Laddar ner {dataset} USDA-data...")
            download_file(url, zip_path)

        print(f"📦 Packar upp {dataset}.zip...")
        json_path = unzip_file(zip_path)

        print(f"📂 Läser in {dataset} JSON...")
        
        # Show expected dataset size
        expected_sizes = {
            "foundation": "~340 foods",
            "sr_legacy": "~7,700 foods", 
            "branded": "~1.2M foods (WARNING: Very large!)"
        }
        expected = expected_sizes.get(dataset, "Unknown size")
        print(f"   📈 Förväntat innehåll: {expected}")
        
        usda_foods = load_usda_json(json_path)
        usda_lookup = build_usda_lookup(usda_foods, usda_lookup)

    print(f"✅ USDA lookup byggd med {len(usda_lookup)} livsmedel från {len(urls_to_process)} dataset")

    # Steg 2: hämta Livsmedelsverket
    print("🔄 Hämtar Livsmedelsverket-data...")
    livs_items = fetch_all_livsmedel()
    print(f"📊 Hämtade {len(livs_items)} svenska livsmedel från Livsmedelsverket")

    # Steg 3: Process foods concurrently with STREAMING output (MUCH FASTER & MEMORY EFFICIENT!)
    print(f"\n🚀 CONCURRENT PROCESSING ENABLED!")
    print(f"⚡ Configuration: {MAX_CONCURRENT_REQUESTS} concurrent requests, {BATCH_SIZE} foods per batch")
    print(f"📊 Complete data collection: Details + Nutrients + Classifications + Raw Materials (SV+EN)")
    print(f"🔌 API calls per food: 8 endpoints (was 4, now includes classifications & raw materials)")
    print(f"💾 STREAMING MODE: Each batch written immediately to '{OUTPUT_FILE}'")
    print(f"🧠 Memory efficient: No data accumulation in RAM")
    print(f"⏱️ Expected time: ~15-20 minutes (vs 30+ minutes sequential)")
    
    # Run the async processing with streaming
    total_entries, matched_count = asyncio.run(
        process_all_foods_concurrent_streaming(livs_items, usda_lookup, OUTPUT_FILE)
    )

    # Steg 4: visa slutstatistik (file already saved via streaming)
    match_rate = (matched_count / len(livs_items)) * 100 if len(livs_items) > 0 else 0
    
    print(f"\n🎉 === SLUTRAPPORT ===")
    print(f"📊 Svenska livsmedel processerade: {len(livs_items)}")
    print(f"🔍 USDA-matchningar hittade: {matched_count}")
    print(f"📈 Matchning-procent: {match_rate:.1f}%")
    print(f"📝 Totala entries skapade: {total_entries} (Svenska + Engelska versioner)")
    print(f"📁 Resultat sparat i: '{OUTPUT_FILE}' (streaming mode)")
    print(f"💿 Filstorlek: {os.path.getsize(OUTPUT_FILE) / (1024*1024):.1f} MB")
    print(f"🧠 Max memory usage: Minimal (streaming approach)")
    print(f"🎯 Klar för import i selective eating applikation!")


if __name__ == "__main__":
    main()
