'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import { Sidebar, MobileSidebar } from '@/components/layout/sidebar';
import { Header } from '@/components/layout/header';
import { DynamicBreadcrumb } from '@/components/layout/breadcrumb';
import { useIsMobile } from '@/hooks/use-mobile';

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const isMobile = useIsMobile();
  const { accessToken, userInfo, setSidebarOpen } = useAuthStore();

  useEffect(() => {
    // Check authentication
    if (!accessToken || !userInfo) {
      router.push('/login');
    }
  }, [accessToken, userInfo, router]);

  if (!accessToken || !userInfo) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse text-muted-foreground">加载中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex">
      {/* Desktop Sidebar */}
      <div className="hidden lg:block">
        <Sidebar />
      </div>

      {/* Mobile Sidebar */}
      <MobileSidebar />

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        <Header onMenuClick={() => setSidebarOpen(true)} />
        
        {/* Page Content */}
        <main className="flex-1 p-4 md:p-6 bg-muted/30">
          <div className="mb-4">
            <DynamicBreadcrumb />
          </div>
          {children}
        </main>
      </div>
    </div>
  );
}
