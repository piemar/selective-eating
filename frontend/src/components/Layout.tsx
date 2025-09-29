import { ReactNode } from 'react';
import { Home, Lightbulb, BookOpen, Users, User } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { cn } from '@/lib/utils';

interface LayoutProps {
  children: ReactNode;
}

const navigation = [
  { name: 'Home', href: '/', icon: Home },
  { name: 'Suggestions', href: '/suggestions', icon: Lightbulb },
  { name: 'Log', href: '/log', icon: BookOpen },
  { name: 'Community', href: '/community', icon: Users },
  { name: 'Profile', href: '/profile', icon: User },
];

export default function Layout({ children }: LayoutProps) {
  const location = useLocation();

  return (
    <div className="min-h-screen bg-gradient-soft flex flex-col">
      <main className="flex-1 pb-20">
        {children}
      </main>
      
      <nav className="fixed bottom-0 left-0 right-0 bg-card border-t border-border shadow-card">
        <div className="px-4 py-2">
          <div className="flex justify-around">
            {navigation.map((item) => {
              const isActive = location.pathname === item.href;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={cn(
                    'flex flex-col items-center px-3 py-2 rounded-2xl transition-all duration-300',
                    isActive 
                      ? 'bg-gradient-primary text-white shadow-soft transform scale-105' 
                      : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
                  )}
                >
                  <item.icon className={cn(
                    'h-5 w-5 mb-1',
                    isActive ? 'text-white' : ''
                  )} />
                  <span className={cn(
                    'text-xs font-medium',
                    isActive ? 'text-white' : ''
                  )}>
                    {item.name}
                  </span>
                </Link>
              );
            })}
          </div>
        </div>
      </nav>
    </div>
  );
}