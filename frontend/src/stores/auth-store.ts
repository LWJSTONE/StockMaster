import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Menu, Role } from '@/types';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  status: number;
  roles: Role[];
  permissions: string[];
}

interface AuthState {
  accessToken: string | null;
  userInfo: UserInfo | null;
  menus: Menu[];
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  setAccessToken: (token: string | null) => void;
  setUserInfo: (info: UserInfo | null) => void;
  setMenus: (menus: Menu[]) => void;
  setSidebarOpen: (open: boolean) => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  toggleSidebar: () => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      userInfo: null,
      menus: [],
      sidebarOpen: true,
      sidebarCollapsed: false,

      setAccessToken: (token) => {
        set({ accessToken: token });
        if (token) {
          localStorage.setItem('accessToken', token);
        } else {
          localStorage.removeItem('accessToken');
        }
      },

      setUserInfo: (info) => set({ userInfo: info }),

      setMenus: (menus) => set({ menus }),

      setSidebarOpen: (open) => set({ sidebarOpen: open }),

      setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),

      toggleSidebar: () => set((state) => ({ 
        sidebarCollapsed: !state.sidebarCollapsed 
      })),

      logout: () => {
        localStorage.removeItem('accessToken');
        set({ 
          accessToken: null, 
          userInfo: null, 
          menus: [] 
        });
      },

      isAuthenticated: () => {
        const state = get();
        return !!state.accessToken && !!state.userInfo;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        userInfo: state.userInfo,
      }),
    }
  )
);
