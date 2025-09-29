import { User, TrendingUp, Calendar, Award, Settings, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { useQuery } from '@tanstack/react-query';
import { api, getUserId, UserStats } from '@/lib/api';

export default function Profile() {
  const userId = getUserId();
  
  // Get user statistics
  const { data: userStats, isLoading: isLoadingStats } = useQuery<UserStats>({
    queryKey: ['user-stats', userId],
    queryFn: () => api.getUserStats(userId),
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="text-center mb-8">
        <div className="w-20 h-20 bg-gradient-primary rounded-full flex items-center justify-center mx-auto mb-4">
          <User className="w-10 h-10 text-white" />
        </div>
        <h1 className="text-2xl font-bold text-foreground mb-1">
          Welcome back, Parent! 👋
        </h1>
        <p className="text-muted-foreground">
          Track your child's food journey
        </p>
      </div>

      {/* Loading state */}
      {isLoadingStats && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-primary mr-2" />
          <span className="text-muted-foreground">Loading your progress...</span>
        </div>
      )}

      {/* User statistics */}
      {userStats && (
        <>
          <div className="grid grid-cols-2 gap-4 mb-8">
            <Card className="p-4 bg-gradient-success/10 border-success/20">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-success rounded-full flex items-center justify-center">
                  <TrendingUp className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-foreground">{userStats.totalFoodsTried}</p>
                  <p className="text-sm text-muted-foreground">Foods Tried</p>
                </div>
              </div>
            </Card>

            <Card className="p-4 bg-gradient-secondary/20 border-secondary/20">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-secondary rounded-full flex items-center justify-center">
                  <Award className="w-5 h-5 text-secondary-foreground" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-foreground">{userStats.positiveFoods}</p>
                  <p className="text-sm text-muted-foreground">New Favorites</p>
                </div>
              </div>
            </Card>
          </div>

          <Card className="p-6 mb-6 bg-gradient-soft">
            <h3 className="text-lg font-semibold text-foreground mb-4 flex items-center gap-2">
              <Calendar className="w-5 h-5" />
              Progress Overview
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-foreground">Food exploration streak</span>
                <span className="font-semibold text-success">{userStats.streak} days! 🔥</span>
              </div>
              
              <div className="flex items-center justify-between">
                <span className="text-foreground">Total foods tried</span>
                <span className="font-semibold text-primary">{userStats.totalFoodsTried} foods</span>
              </div>
              
              <div className="flex items-center justify-between">
                <span className="text-foreground">Positive reactions</span>
                <span className="font-semibold text-success">{Math.round(userStats.positivePercentage)}%</span>
              </div>
            </div>
            
            <div className="mt-4 pt-4 border-t border-border">
              <div className="w-full bg-muted rounded-full h-2">
                <div 
                  className="bg-gradient-success h-2 rounded-full transition-all duration-500" 
                  style={{width: `${Math.min(userStats.positivePercentage, 100)}%`}}
                ></div>
              </div>
              <p className="text-sm text-muted-foreground mt-2">
                Food journey progress: {Math.round(userStats.positivePercentage)}%
              </p>
            </div>
          </Card>

          {userStats.recentAchievements.length > 0 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-foreground">Recent Achievements</h3>
              
              <div className="space-y-3">
                {userStats.recentAchievements.map((achievement, index) => (
                  <div key={index} className="flex items-center gap-3 p-3 bg-card rounded-2xl shadow-gentle">
                    <div className="w-8 h-8 bg-warning rounded-full flex items-center justify-center">
                      <span className="text-sm">🏆</span>
                    </div>
                    <div>
                      <p className="font-medium text-foreground">{achievement}</p>
                      <p className="text-sm text-muted-foreground">Keep up the great progress!</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      <div className="mt-8">
        <Button
          variant="outline"
          size="lg"
          className="w-full"
        >
          <Settings className="w-4 h-4 mr-2" />
          Settings
        </Button>
      </div>
    </div>
  );
}