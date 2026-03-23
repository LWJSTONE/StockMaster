'use client';

import { cn } from '@/lib/utils';
import { useAuthStore } from '@/stores/auth-store';
import {
  LayoutDashboard,
  Package,
  Warehouse,
  ShoppingCart,
  Settings,
  ChevronDown,
  Menu,
  X,
  Boxes,
  LogIn,
  LogOut,
  FileText,
  Users,
  ShieldCheck,
  PanelLeftClose,
  PanelLeft,
  Building2,
} from 'lucide-react';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip';

interface MenuItem {
  id: string;
  name: string;
  path?: string;
  icon: React.ElementType;
  children?: MenuItem[];
}

const menuItems: MenuItem[] = [
  {
    id: 'dashboard',
    name: '仪表盘',
    path: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    id: 'product',
    name: '商品管理',
    icon: Package,
    children: [
      { id: 'product-list', name: '商品列表', path: '/product/list', icon: Boxes },
      { id: 'product-category', name: '分类管理', path: '/product/category', icon: FileText },
    ],
  },
  {
    id: 'stock',
    name: '库存管理',
    icon: Warehouse,
    children: [
      { id: 'stock-inventory', name: '库存查询', path: '/stock/inventory', icon: Boxes },
      { id: 'stock-warehouse', name: '仓库管理', path: '/stock/warehouse', icon: Building2 },
      { id: 'stock-inbound', name: '入库管理', path: '/stock/inbound', icon: LogIn },
      { id: 'stock-outbound', name: '出库管理', path: '/stock/outbound', icon: LogOut },
    ],
  },
  {
    id: 'purchase',
    name: '采购管理',
    icon: ShoppingCart,
    children: [
      { id: 'purchase-supplier', name: '供应商管理', path: '/purchase/supplier', icon: Users },
      { id: 'purchase-evaluation', name: '供应商评价', path: '/purchase/evaluation', icon: ShieldCheck },
      { id: 'purchase-order', name: '采购订单', path: '/purchase/order', icon: FileText },
    ],
  },
  {
    id: 'system',
    name: '系统管理',
    icon: Settings,
    children: [
      { id: 'system-user', name: '用户管理', path: '/system/user', icon: Users },
      { id: 'system-role', name: '角色管理', path: '/system/role', icon: ShieldCheck },
      { id: 'system-menu', name: '菜单管理', path: '/system/menu', icon: Menu },
      { id: 'system-log', name: '操作日志', path: '/system/log', icon: FileText },
    ],
  },
];

interface SidebarProps {
  className?: string;
}

export function Sidebar({ className }: SidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { sidebarCollapsed, toggleSidebar, logout } = useAuthStore();
  const [openMenus, setOpenMenus] = useState<string[]>([]);

  const toggleMenu = (menuId: string) => {
    setOpenMenus((prev) =>
      prev.includes(menuId)
        ? prev.filter((id) => id !== menuId)
        : [...prev, menuId]
    );
  };

  const isActive = (path: string) => pathname === path;
  const isParentActive = (children?: MenuItem[]) =>
    children?.some((child) => child.path && isActive(child.path));

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <div
      className={cn(
        'flex flex-col h-full bg-sidebar border-r border-sidebar-border transition-all duration-300',
        sidebarCollapsed ? 'w-16' : 'w-64',
        className
      )}
    >
      {/* Logo */}
      <div className="flex items-center h-16 px-4 border-b border-sidebar-border">
        {!sidebarCollapsed && (
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <Warehouse className="w-5 h-5 text-primary-foreground" />
            </div>
            <span className="font-bold text-lg text-sidebar-foreground">
              StockMaster
            </span>
          </div>
        )}
        {sidebarCollapsed && (
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center mx-auto">
            <Warehouse className="w-5 h-5 text-primary-foreground" />
          </div>
        )}
      </div>

      {/* Menu */}
      <ScrollArea className="flex-1 py-4">
        <nav className="px-2 space-y-1">
          <TooltipProvider delayDuration={0}>
            {menuItems.map((item) => {
              if (item.children) {
                const isOpen = openMenus.includes(item.id) || isParentActive(item.children);
                return (
                  <Collapsible
                    key={item.id}
                    open={isOpen}
                    onOpenChange={() => toggleMenu(item.id)}
                  >
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <CollapsibleTrigger asChild>
                          <button
                            className={cn(
                              'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                              'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                              isParentActive(item.children)
                                ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                                : 'text-sidebar-foreground'
                            )}
                          >
                            <item.icon className="w-5 h-5 shrink-0" />
                            {!sidebarCollapsed && (
                              <>
                                <span className="flex-1 text-left">{item.name}</span>
                                <ChevronDown
                                  className={cn(
                                    'w-4 h-4 transition-transform',
                                    isOpen && 'rotate-180'
                                  )}
                                />
                              </>
                            )}
                          </button>
                        </CollapsibleTrigger>
                      </TooltipTrigger>
                      {sidebarCollapsed && (
                        <TooltipContent side="right">
                          <p>{item.name}</p>
                        </TooltipContent>
                      )}
                    </Tooltip>
                    {!sidebarCollapsed && (
                      <CollapsibleContent className="ml-4 mt-1 space-y-1">
                        {item.children.map((child) => (
                          <button
                            key={child.id}
                            onClick={() => child.path && router.push(child.path)}
                            className={cn(
                              'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
                              'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                              child.path && isActive(child.path)
                                ? 'bg-primary text-primary-foreground'
                                : 'text-sidebar-foreground'
                            )}
                          >
                            <child.icon className="w-4 h-4" />
                            <span>{child.name}</span>
                          </button>
                        ))}
                      </CollapsibleContent>
                    )}
                  </Collapsible>
                );
              } else {
                return (
                  <Tooltip key={item.id}>
                    <TooltipTrigger asChild>
                      <button
                        onClick={() => item.path && router.push(item.path)}
                        className={cn(
                          'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                          'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                          item.path && isActive(item.path)
                            ? 'bg-primary text-primary-foreground'
                            : 'text-sidebar-foreground'
                        )}
                      >
                        <item.icon className="w-5 h-5 shrink-0" />
                        {!sidebarCollapsed && <span>{item.name}</span>}
                      </button>
                    </TooltipTrigger>
                    {sidebarCollapsed && (
                      <TooltipContent side="right">
                        <p>{item.name}</p>
                      </TooltipContent>
                    )}
                  </Tooltip>
                );
              }
            })}
          </TooltipProvider>
        </nav>
      </ScrollArea>

      {/* Footer */}
      <div className="p-4 border-t border-sidebar-border">
        <Button
          variant="ghost"
          className={cn(
            'w-full justify-start gap-3 text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
            sidebarCollapsed && 'justify-center'
          )}
          onClick={toggleSidebar}
        >
          {sidebarCollapsed ? (
            <PanelLeft className="w-5 h-5" />
          ) : (
            <>
              <PanelLeftClose className="w-5 h-5" />
              <span>收起菜单</span>
            </>
          )}
        </Button>
      </div>
    </div>
  );
}

export function MobileSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { sidebarOpen, setSidebarOpen, logout } = useAuthStore();
  const [openMenus, setOpenMenus] = useState<string[]>([]);

  const toggleMenu = (menuId: string) => {
    setOpenMenus((prev) =>
      prev.includes(menuId)
        ? prev.filter((id) => id !== menuId)
        : [...prev, menuId]
    );
  };

  const isActive = (path: string) => pathname === path;
  const isParentActive = (children?: MenuItem[]) =>
    children?.some((child) => child.path && isActive(child.path));

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <>
      {/* Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div
        className={cn(
          'fixed inset-y-0 left-0 z-50 w-64 bg-sidebar border-r border-sidebar-border transform transition-transform duration-300 lg:hidden',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between h-16 px-4 border-b border-sidebar-border">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <Warehouse className="w-5 h-5 text-primary-foreground" />
            </div>
            <span className="font-bold text-lg text-sidebar-foreground">
              StockMaster
            </span>
          </div>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="w-5 h-5" />
          </Button>
        </div>

        {/* Menu */}
        <ScrollArea className="flex-1 py-4">
          <nav className="px-2 space-y-1">
            {menuItems.map((item) => {
              if (item.children) {
                const isOpen = openMenus.includes(item.id) || isParentActive(item.children);
                return (
                  <Collapsible
                    key={item.id}
                    open={isOpen}
                    onOpenChange={() => toggleMenu(item.id)}
                  >
                    <CollapsibleTrigger asChild>
                      <button
                        className={cn(
                          'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                          'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                          isParentActive(item.children)
                            ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                            : 'text-sidebar-foreground'
                        )}
                      >
                        <item.icon className="w-5 h-5" />
                        <span className="flex-1 text-left">{item.name}</span>
                        <ChevronDown
                          className={cn(
                            'w-4 h-4 transition-transform',
                            isOpen && 'rotate-180'
                          )}
                        />
                      </button>
                    </CollapsibleTrigger>
                    <CollapsibleContent className="ml-4 mt-1 space-y-1">
                      {item.children.map((child) => (
                        <button
                          key={child.id}
                          onClick={() => {
                            if (child.path) {
                              router.push(child.path);
                              setSidebarOpen(false);
                            }
                          }}
                          className={cn(
                            'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
                            'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                            child.path && isActive(child.path)
                              ? 'bg-primary text-primary-foreground'
                              : 'text-sidebar-foreground'
                          )}
                        >
                          <child.icon className="w-4 h-4" />
                          <span>{child.name}</span>
                        </button>
                      ))}
                    </CollapsibleContent>
                  </Collapsible>
                );
              } else {
                return (
                  <button
                    key={item.id}
                    onClick={() => {
                      if (item.path) {
                        router.push(item.path);
                        setSidebarOpen(false);
                      }
                    }}
                    className={cn(
                      'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                      'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                      item.path && isActive(item.path)
                        ? 'bg-primary text-primary-foreground'
                        : 'text-sidebar-foreground'
                    )}
                  >
                    <item.icon className="w-5 h-5" />
                    <span>{item.name}</span>
                  </button>
                );
              }
            })}
          </nav>
        </ScrollArea>
      </div>
    </>
  );
}
