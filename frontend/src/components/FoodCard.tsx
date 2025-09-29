interface FoodCardProps {
  name: string;
  image: string;
  tags?: string[];
  isSelected?: boolean;
  onClick?: () => void;
  className?: string;
  onError?: (e: React.SyntheticEvent<HTMLImageElement>) => void;
}

export default function FoodCard({ 
  name, 
  image, 
  tags = [], 
  isSelected = false, 
  onClick,
  className = '',
  onError
}: FoodCardProps) {
  return (
    <div
      onClick={onClick}
      className={`
        relative bg-card rounded-3xl p-6 shadow-gentle hover:shadow-card 
        transition-all duration-300 cursor-pointer group
        ${isSelected ? 'ring-2 ring-primary shadow-card bg-gradient-primary/5' : 'hover:scale-105'}
        ${className}
      `}
    >
      <div className="aspect-square mb-4 flex items-center justify-center">
        <img 
          src={image} 
          alt={name}
          onError={onError}
          className="w-16 h-16 object-contain group-hover:scale-110 transition-transform duration-300"
        />
      </div>
      
      <h3 className="text-lg font-semibold text-center text-foreground mb-2">
        {name}
      </h3>
      
      {tags.length > 0 && (
        <div className="flex flex-wrap gap-1 justify-center">
          {tags.map((tag, index) => (
            <span
              key={index}
              className="px-2 py-1 bg-secondary/50 text-secondary-foreground rounded-full text-xs font-medium"
            >
              {tag}
            </span>
          ))}
        </div>
      )}
      
      {isSelected && (
        <div className="absolute top-2 right-2 w-6 h-6 bg-success rounded-full flex items-center justify-center">
          <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
        </div>
      )}
    </div>
  );
}