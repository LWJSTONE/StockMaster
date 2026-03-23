'use client';

import { usePathname } from 'next/navigation';
import { ChevronRight, Home } from 'lucide-react';
import Link from 'next/link';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb';

const routeNames: Record<string, string> = {
  dashboard: '仪表盘',
  product: '商品管理',
  'product/list': '商品列表',
  'product/category': '分类管理',
  stock: '库存管理',
  'stock/inventory': '库存查询',
  'stock/inbound': '入库管理',
  'stock/outbound': '出库管理',
  purchase: '采购管理',
  'purchase/supplier': '供应商管理',
  'purchase/evaluation': '供应商评价',
  'purchase/order': '采购订单',
  system: '系统管理',
  'system/user': '用户管理',
  'system/role': '角色管理',
  'system/menu': '菜单管理',
  'system/log': '操作日志',
};

export function DynamicBreadcrumb() {
  const pathname = usePathname();
  
  // Split pathname into segments
  const segments = pathname.split('/').filter(Boolean);
  
  // Build breadcrumb items
  const items: { label: string; href: string }[] = [];
  
  // Add home
  items.push({ label: '首页', href: '/dashboard' });
  
  // Build path segments
  let currentPath = '';
  segments.forEach((segment, index) => {
    currentPath += `/${segment}`;
    const label = routeNames[currentPath.slice(1)] || routeNames[segment] || segment;
    items.push({ label, href: currentPath });
  });

  return (
    <Breadcrumb>
      <BreadcrumbList>
        {items.map((item, index) => (
          <BreadcrumbItem key={item.href}>
            {index < items.length - 1 ? (
              <>
                <BreadcrumbLink asChild>
                  <Link href={item.href}>{item.label}</Link>
                </BreadcrumbLink>
                <BreadcrumbSeparator />
              </>
            ) : (
              <BreadcrumbPage>{item.label}</BreadcrumbPage>
            )}
          </BreadcrumbItem>
        ))}
      </BreadcrumbList>
    </Breadcrumb>
  );
}
