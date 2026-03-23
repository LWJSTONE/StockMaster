'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';

export default function Home() {
  const router = useRouter();
  const { accessToken, userInfo } = useAuthStore();

  useEffect(() => {
    if (accessToken && userInfo) {
      router.push('/dashboard');
    } else {
      router.push('/login');
    }
  }, [accessToken, userInfo, router]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="animate-pulse text-muted-foreground">加载中...</div>
    </div>
  );
}
