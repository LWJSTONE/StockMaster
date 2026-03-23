'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { menuApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { useToast } from '@/hooks/use-toast';
import {
  Plus,
  MoreHorizontal,
  Pencil,
  Trash2,
  ChevronRight,
  ChevronDown,
  Folder,
  FileText,
  Circle,
} from 'lucide-react';
import type { Menu } from '@/types';

// Menu form schema
const menuFormSchema = z.object({
  parentId: z.number().nullable(),
  name: z.string().min(1, '请输入菜单名称').max(20, '菜单名称最多20个字符'),
  path: z.string().max(100, '路径最多100个字符').optional().or(z.literal('')),
  icon: z.string().max(50, '图标最多50个字符').optional().or(z.literal('')),
  sort: z.number().min(0, '排序不能为负数'),
  type: z.enum(['directory', 'menu', 'button']),
  permission: z.string().max(100, '权限标识最多100个字符').optional().or(z.literal('')),
  visible: z.number(),
  status: z.number(),
});

type MenuFormData = z.infer<typeof menuFormSchema>;

interface MenuTreeNode extends Menu {
  children?: MenuTreeNode[];
}

export default function MenuManagementPage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  // State
  const [expandedMenus, setExpandedMenus] = useState<Set<number>>(new Set());

  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [currentMenu, setCurrentMenu] = useState<MenuTreeNode | null>(null);
  const [parentMenuId, setParentMenuId] = useState<number | null>(null);

  // Form
  const form = useForm<MenuFormData>({
    resolver: zodResolver(menuFormSchema),
    defaultValues: {
      parentId: null,
      name: '',
      path: '',
      icon: '',
      sort: 0,
      type: 'directory',
      permission: '',
      visible: 1,
      status: 1,
    },
  });

  // Queries
  const { data: menusData, isLoading } = useQuery({
    queryKey: ['menus-tree'],
    queryFn: () => menuApi.getTree(),
  });

  const menus = menusData || [];

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: MenuFormData) =>
      menuApi.create({
        parentId: data.parentId,
        name: data.name,
        path: data.path || '',
        icon: data.icon || '',
        sort: data.sort,
        type: data.type,
        permission: data.permission || '',
        visible: data.visible,
        status: data.status,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus-tree'] });
      setIsCreateDialogOpen(false);
      form.reset();
      setParentMenuId(null);
      toast({ title: '创建成功', description: '菜单已成功创建' });
    },
    onError: (error: Error) => {
      toast({ title: '创建失败', description: error.message, variant: 'destructive' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: MenuFormData }) =>
      menuApi.update(id, {
        parentId: data.parentId,
        name: data.name,
        path: data.path || '',
        icon: data.icon || '',
        sort: data.sort,
        type: data.type,
        permission: data.permission || '',
        visible: data.visible,
        status: data.status,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus-tree'] });
      setIsEditDialogOpen(false);
      setCurrentMenu(null);
      form.reset();
      toast({ title: '更新成功', description: '菜单信息已更新' });
    },
    onError: (error: Error) => {
      toast({ title: '更新失败', description: error.message, variant: 'destructive' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => menuApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus-tree'] });
      setIsDeleteDialogOpen(false);
      setCurrentMenu(null);
      toast({ title: '删除成功', description: '菜单已删除' });
    },
    onError: (error: Error) => {
      toast({ title: '删除失败', description: error.message, variant: 'destructive' });
    },
  });

  // Handlers
  const toggleExpand = (menuId: number) => {
    const newExpanded = new Set(expandedMenus);
    if (newExpanded.has(menuId)) {
      newExpanded.delete(menuId);
    } else {
      newExpanded.add(menuId);
    }
    setExpandedMenus(newExpanded);
  };

  const handleCreateChild = (parentMenu: MenuTreeNode) => {
    setParentMenuId(parentMenu.id);
    form.reset({
      parentId: parentMenu.id,
      name: '',
      path: '',
      icon: '',
      sort: 0,
      type: 'menu',
      permission: '',
      visible: 1,
      status: 1,
    });
    setIsCreateDialogOpen(true);
  };

  const handleCreateRoot = () => {
    setParentMenuId(null);
    form.reset({
      parentId: null,
      name: '',
      path: '',
      icon: '',
      sort: 0,
      type: 'directory',
      permission: '',
      visible: 1,
      status: 1,
    });
    setIsCreateDialogOpen(true);
  };

  const handleEdit = (menu: MenuTreeNode) => {
    setCurrentMenu(menu);
    form.reset({
      parentId: menu.parentId,
      name: menu.name,
      path: menu.path || '',
      icon: menu.icon || '',
      sort: menu.sort,
      type: menu.type as 'directory' | 'menu' | 'button',
      permission: menu.permission || '',
      visible: menu.visible,
      status: menu.status,
    });
    setIsEditDialogOpen(true);
  };

  const handleDelete = (menu: MenuTreeNode) => {
    setCurrentMenu(menu);
    setIsDeleteDialogOpen(true);
  };

  const handleCreateSubmit = (data: MenuFormData) => {
    createMutation.mutate(data);
  };

  const handleEditSubmit = (data: MenuFormData) => {
    if (currentMenu) {
      updateMutation.mutate({ id: currentMenu.id, data });
    }
  };

  const getTypeBadge = (type: string) => {
    switch (type) {
      case 'directory':
        return (
          <Badge variant="secondary" className="bg-purple-50 text-purple-700 border-purple-200">
            <Folder className="h-3 w-3 mr-1" />
            目录
          </Badge>
        );
      case 'menu':
        return (
          <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
            <FileText className="h-3 w-3 mr-1" />
            菜单
          </Badge>
        );
      case 'button':
        return (
          <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
            <Circle className="h-3 w-3 mr-1" />
            按钮
          </Badge>
        );
      default:
        return null;
    }
  };

  const getStatusBadge = (status: number) => {
    return status === 1 ? (
      <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
        启用
      </Badge>
    ) : (
      <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200">
        禁用
      </Badge>
    );
  };

  const getVisibleBadge = (visible: number) => {
    return visible === 1 ? (
      <Badge variant="outline" className="text-xs">显示</Badge>
    ) : (
      <Badge variant="secondary" className="text-xs">隐藏</Badge>
    );
  };

  // Flatten menus for parent selection
  const flattenMenus = (menuList: MenuTreeNode[], result: { id: number; name: string; level: number }[] = [], level: number = 0): { id: number; name: string; level: number }[] => {
    menuList.forEach((menu) => {
      result.push({ id: menu.id, name: menu.name, level });
      if (menu.children && menu.children.length > 0) {
        flattenMenus(menu.children as MenuTreeNode[], result, level + 1);
      }
    });
    return result;
  };

  const flatMenus = flattenMenus(menus as MenuTreeNode[]);

  // Render tree table rows
  const renderMenuRows = (menuList: MenuTreeNode[], level: number = 0): React.ReactNode => {
    return menuList.map((menu) => {
      const hasChildren = menu.children && menu.children.length > 0;
      const isExpanded = expandedMenus.has(menu.id);

      return (
        <Collapsible key={menu.id} open={isExpanded} onOpenChange={() => toggleExpand(menu.id)}>
          <TableRow>
            <TableCell>
              <div className="flex items-center" style={{ paddingLeft: `${level * 24}px` }}>
                {hasChildren ? (
                  <CollapsibleTrigger asChild>
                    <button className="p-0.5 hover:bg-muted rounded mr-1">
                      {isExpanded ? (
                        <ChevronDown className="h-4 w-4" />
                      ) : (
                        <ChevronRight className="h-4 w-4" />
                      )}
                    </button>
                  </CollapsibleTrigger>
                ) : (
                  <span className="w-5 inline-block" />
                )}
                <span className="font-medium">{menu.name}</span>
              </div>
            </TableCell>
            <TableCell>
              <code className="bg-muted px-2 py-0.5 rounded text-xs">
                {menu.path || '-'}
              </code>
            </TableCell>
            <TableCell>
              {menu.icon ? (
                <Badge variant="outline" className="text-xs">
                  {menu.icon}
                </Badge>
              ) : (
                '-'
              )}
            </TableCell>
            <TableCell>{getTypeBadge(menu.type)}</TableCell>
            <TableCell>
              {menu.permission ? (
                <code className="bg-muted px-2 py-0.5 rounded text-xs">
                  {menu.permission}
                </code>
              ) : (
                '-'
              )}
            </TableCell>
            <TableCell className="text-center">{menu.sort}</TableCell>
            <TableCell>{getVisibleBadge(menu.visible)}</TableCell>
            <TableCell>{getStatusBadge(menu.status)}</TableCell>
            <TableCell>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <MoreHorizontal className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  {menu.type !== 'button' && (
                    <DropdownMenuItem onClick={() => handleCreateChild(menu)}>
                      <Plus className="h-4 w-4 mr-2" />
                      新增子菜单
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuItem onClick={() => handleEdit(menu)}>
                    <Pencil className="h-4 w-4 mr-2" />
                    编辑
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    className="text-red-600"
                    onClick={() => handleDelete(menu)}
                  >
                    <Trash2 className="h-4 w-4 mr-2" />
                    删除
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </TableCell>
          </TableRow>
          {hasChildren && (
            <CollapsibleContent asChild>
              <TableBody>
                {renderMenuRows(menu.children as MenuTreeNode[], level + 1)}
              </TableBody>
            </CollapsibleContent>
          )}
        </Collapsible>
      );
    });
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <CardTitle className="text-xl font-semibold">菜单管理</CardTitle>
            <Button size="sm" onClick={handleCreateRoot}>
              <Plus className="h-4 w-4 mr-1" />
              新增菜单
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>菜单名称</TableHead>
                  <TableHead>路径</TableHead>
                  <TableHead>图标</TableHead>
                  <TableHead className="w-24">类型</TableHead>
                  <TableHead>权限标识</TableHead>
                  <TableHead className="w-16">排序</TableHead>
                  <TableHead className="w-16">可见</TableHead>
                  <TableHead className="w-20">状态</TableHead>
                  <TableHead className="w-24">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : menus.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  renderMenuRows(menus as MenuTreeNode[])
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {parentMenuId ? '新增子菜单' : '新增菜单'}
            </DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleCreateSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="parentId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>上级菜单</FormLabel>
                    <Select
                      onValueChange={(v) => field.onChange(v === 'root' ? null : Number(v))}
                      value={field.value === null ? 'root' : String(field.value)}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择上级菜单" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="root">根目录</SelectItem>
                        {flatMenus.map((menu) => (
                          <SelectItem key={menu.id} value={String(menu.id)}>
                            {'　'.repeat(menu.level)}{menu.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>菜单名称 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入菜单名称" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="type"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>菜单类型 *</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择类型" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="directory">目录</SelectItem>
                          <SelectItem value="menu">菜单</SelectItem>
                          <SelectItem value="button">按钮</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="path"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>路由路径</FormLabel>
                      <FormControl>
                        <Input placeholder="如: /system/user" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="icon"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>图标</FormLabel>
                      <FormControl>
                        <Input placeholder="如: Users" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="permission"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>权限标识</FormLabel>
                      <FormControl>
                        <Input placeholder="如: system:user:list" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="sort"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>排序</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          {...field}
                          onChange={(e) => field.onChange(Number(e.target.value))}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="visible"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>是否可见</FormLabel>
                      <Select onValueChange={(v) => field.onChange(Number(v))} value={String(field.value)}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="1">显示</SelectItem>
                          <SelectItem value="0">隐藏</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="status"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>状态</FormLabel>
                      <Select onValueChange={(v) => field.onChange(Number(v))} value={String(field.value)}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择状态" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="1">启用</SelectItem>
                          <SelectItem value="0">禁用</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                  取消
                </Button>
                <Button type="submit" disabled={createMutation.isPending}>
                  {createMutation.isPending ? '创建中...' : '创建'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>编辑菜单</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleEditSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="parentId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>上级菜单</FormLabel>
                    <Select
                      onValueChange={(v) => field.onChange(v === 'root' ? null : Number(v))}
                      value={field.value === null ? 'root' : String(field.value)}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择上级菜单" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="root">根目录</SelectItem>
                        {flatMenus
                          .filter((menu) => menu.id !== currentMenu?.id)
                          .map((menu) => (
                            <SelectItem key={menu.id} value={String(menu.id)}>
                              {'　'.repeat(menu.level)}{menu.name}
                            </SelectItem>
                          ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>菜单名称 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入菜单名称" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="type"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>菜单类型 *</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择类型" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="directory">目录</SelectItem>
                          <SelectItem value="menu">菜单</SelectItem>
                          <SelectItem value="button">按钮</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="path"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>路由路径</FormLabel>
                      <FormControl>
                        <Input placeholder="如: /system/user" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="icon"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>图标</FormLabel>
                      <FormControl>
                        <Input placeholder="如: Users" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="permission"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>权限标识</FormLabel>
                      <FormControl>
                        <Input placeholder="如: system:user:list" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="sort"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>排序</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          {...field}
                          onChange={(e) => field.onChange(Number(e.target.value))}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="visible"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>是否可见</FormLabel>
                      <Select onValueChange={(v) => field.onChange(Number(v))} value={String(field.value)}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="1">显示</SelectItem>
                          <SelectItem value="0">隐藏</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="status"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>状态</FormLabel>
                      <Select onValueChange={(v) => field.onChange(Number(v))} value={String(field.value)}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="选择状态" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="1">启用</SelectItem>
                          <SelectItem value="0">禁用</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                  取消
                </Button>
                <Button type="submit" disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? '保存中...' : '保存'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Delete Dialog */}
      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              确定要删除菜单 &quot;{currentMenu?.name}&quot; 吗？
              {currentMenu?.children && currentMenu.children.length > 0 && (
                <span className="block mt-2 text-red-600 font-medium">
                  该菜单下有 {currentMenu.children.length} 个子菜单，删除后子菜单也会被删除！
                </span>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-red-600 hover:bg-red-700"
              onClick={() => currentMenu && deleteMutation.mutate(currentMenu.id)}
              disabled={deleteMutation.isPending}
            >
              {deleteMutation.isPending ? '删除中...' : '确认删除'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
