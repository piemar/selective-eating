import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Star, Send } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/hooks/use-toast';
import { api, getUserId } from '@/lib/api';

export default function Log() {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const foodNumber = location.state?.foodNumber;
  const foodName = location.state?.foodName || 'Unknown Food';
  const initialRating = location.state?.initialRating;
  
  const [rating, setRating] = useState(initialRating || (location.state?.initialReaction === 'liked' ? 5 : 1));
  const [notes, setNotes] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!foodNumber || !foodName) {
      toast({
        title: "Missing information",
        description: "We need food information to log this experience.",
        variant: "destructive",
      });
      return;
    }

    setIsSubmitting(true);
    
    try {
      await api.logFoodExperience({
        userId: getUserId(),
        foodNumber: foodNumber,
        foodName: foodName,
        rating: rating,
        notes: notes || undefined,
        context: 'manual_log'
      });

      setSubmitted(true);
      toast({
        title: "Food experience logged! 📝",
        description: "Your notes help us suggest better foods for your child.",
      });
      
      setTimeout(() => {
        navigate('/');
      }, 2000);
      
    } catch (error) {
      console.error('Error logging food experience:', error);
      toast({
        title: "Failed to save",
        description: "We couldn't save your experience. Please try again.",
        variant: "destructive",
      });
      setIsSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="p-6 max-w-md mx-auto flex flex-col items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <div className="w-20 h-20 bg-gradient-success rounded-full flex items-center justify-center mx-auto mb-4">
            <Star className="w-10 h-10 text-white" />
          </div>
          <h2 className="text-2xl font-bold text-foreground mb-2">
            Thank you! 🌟
          </h2>
          <p className="text-muted-foreground">
            Your feedback helps us learn what works for your child.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-md mx-auto">
      <div className="flex items-center mb-6">
        <Button
          variant="ghost"
          onClick={() => navigate(-1)}
          className="mr-4 p-2"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h1 className="text-2xl font-bold text-foreground">
          Log Food Experience
        </h1>
        {foodName !== 'Unknown Food' && (
          <p className="text-muted-foreground mt-1">
            {foodName}
          </p>
        )}
      </div>

      <div className="space-y-6">
        <div className="bg-card rounded-3xl p-6 shadow-gentle">
          <h3 className="text-lg font-semibold text-foreground mb-4">
            How did it go? ⭐
          </h3>
          
          <div className="flex justify-center gap-2 mb-6">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                onClick={() => setRating(star)}
                className="p-1"
              >
                <Star
                  className={`w-8 h-8 ${
                    star <= rating
                      ? 'fill-warning text-warning'
                      : 'text-muted-foreground'
                  }`}
                />
              </button>
            ))}
          </div>

          <div className="text-center mb-4">
            <span className="text-sm text-muted-foreground">
              {rating === 1 && "Didn't like it"}
              {rating === 2 && "Not very interested"}
              {rating === 3 && "It was okay"}
              {rating === 4 && "Liked it!"}
              {rating === 5 && "Loved it! 🎉"}
            </span>
          </div>
        </div>

        <div className="bg-card rounded-3xl p-6 shadow-gentle">
          <h3 className="text-lg font-semibold text-foreground mb-4">
            Any notes to remember? 📝
          </h3>
          <Textarea
            placeholder="e.g., Only ate a small bite, liked the sweetness but not the texture, asked for more..."
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="min-h-[100px] border-border bg-background"
          />
        </div>

        <Button
          onClick={handleSubmit}
          disabled={isSubmitting}
          variant="gradient"
          className="w-full font-semibold py-4 text-lg transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          size="lg"
        >
          {isSubmitting ? (
            <>
              <div className="w-5 h-5 mr-2 animate-spin rounded-full border-2 border-current border-t-transparent" />
              Saving...
            </>
          ) : (
            <>
              <Send className="w-5 h-5 mr-2" />
              Save Experience
            </>
          )}
        </Button>
      </div>
    </div>
  );
}