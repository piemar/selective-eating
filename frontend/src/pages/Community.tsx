import React from 'react';
import { Heart, MessageCircle, Star, User } from 'lucide-react';
import { Button } from '@/components/ui/button';

const communityPosts = [
  {
    id: 1,
    author: 'Sarah M.',
    time: '2 hours ago',
    content: 'Success! My 4-year-old finally tried carrots after weeks of offering them. The key was letting her help prepare them - she felt more in control! 🥕✨',
    likes: 12,
    comments: 3,
    food: 'Carrots',
    rating: 4
  },
  {
    id: 2,
    author: 'Mike D.',
    time: '5 hours ago',
    content: 'Tip: We started with tiny portions - just one piece of new food alongside familiar favorites. No pressure, just exposure. It really works! 😊',
    likes: 8,
    comments: 5,
    food: 'Broccoli',
    rating: 3
  },
  {
    id: 3,
    author: 'Jennifer K.',
    time: '1 day ago',
    content: 'The sensory tags in this app are so helpful! Realized my son prefers crunchy foods, so we tried apple slices instead of applesauce. Big win! 🍎',
    likes: 15,
    comments: 7,
    food: 'Apples',
    rating: 5
  },
];

export default function Community() {
  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-foreground mb-2">
          Community Stories 👨‍👩‍👧‍👦
        </h1>
        <p className="text-muted-foreground text-lg">
          Share experiences and get inspired by other families
        </p>
      </div>

      <div className="space-y-6">
        {communityPosts.map((post) => (
          <div key={post.id} className="bg-card rounded-3xl p-6 shadow-gentle hover:shadow-card transition-all duration-300">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 bg-gradient-secondary rounded-full flex items-center justify-center flex-shrink-0">
                <User className="w-5 h-5 text-secondary-foreground" />
              </div>
              
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <span className="font-semibold text-foreground">{post.author}</span>
                  <span className="text-sm text-muted-foreground">•</span>
                  <span className="text-sm text-muted-foreground">{post.time}</span>
                  {post.food && (
                    <>
                      <span className="text-sm text-muted-foreground">•</span>
                      <span className="px-2 py-1 bg-info/20 text-info-foreground rounded-full text-xs font-medium">
                        {post.food}
                      </span>
                    </>
                  )}
                </div>
                
                <p className="text-foreground mb-4 leading-relaxed">
                  {post.content}
                </p>

                {post.rating && (
                  <div className="flex items-center gap-1 mb-4">
                    <span className="text-sm text-muted-foreground mr-2">Rating:</span>
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star
                        key={star}
                        className={`w-4 h-4 ${
                          star <= post.rating
                            ? 'fill-warning text-warning'
                            : 'text-muted-foreground'
                        }`}
                      />
                    ))}
                  </div>
                )}
                
                <div className="flex items-center gap-6 text-muted-foreground">
                  <button className="flex items-center gap-2 hover:text-foreground transition-colors">
                    <Heart className="w-4 h-4" />
                    <span className="text-sm">{post.likes} likes</span>
                  </button>
                  
                  <button className="flex items-center gap-2 hover:text-foreground transition-colors">
                    <MessageCircle className="w-4 h-4" />
                    <span className="text-sm">{post.comments} comments</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-8 text-center">
        <Button
          variant="outline"
          size="lg"
          className="bg-gradient-soft hover:bg-gradient-secondary border-primary/20"
        >
          Share Your Story
        </Button>
      </div>
    </div>
  );
}