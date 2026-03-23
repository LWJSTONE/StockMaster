'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { roleApi, menuApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
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
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { useToast } from '@/hooks/use-toast';
import {
  Search,
  Plus,
  MoreHorizontal,
  Pencil,
  Trash2,
  Settings,
  ChevronRight,
  ChevronDown,
} from 'lucide-react';
import type { Role, Menu } from '@/types';

// Role form schema
const roleFormSchema = z.object({
  name: z.string().min(1, '请输入角色名称').max(20, '角色名称最多20个字符'),
  code: z.string().min(1, '请输入角色编码').max(20, '角色编码最多20个字符'),
  description: z.string().max(100, '描述最多100个字符').optional().or(z.literal('')),
  status: z.number(),
  menuIds: z.array(z.number()),
});

type RoleFormData = z.infer<typeof roleFormSchema>;

interface MenuTreeNode extends Menu {
  children?: MenuTreeNode[];
}

export default function RoleManagementPage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  // State
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);

  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isMenuDialogOpen, setIsMenuDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [currentRole, setCurrentRole] = useState<Role | null>(null);
  const [expandedMenus, setExpandedMenus] = useState<Set<number>>(new Set());

  // Form
  const form = useForm<RoleFormData>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: {
      name: '',
      code: '',
      description: '',
      status: 1,
      menuIds: [],
    },
  });

  // Queries
  const { data: rolesData, isLoading } = useQuery({
    queryKey: ['roles', page, pageSize, searchKeyword, statusFilter],
    queryFn: () =>
      roleApi.getList({
        page: page,
        size: pageSize,
        keyword: searchKeyword || undefined,
        status: statusFilter === 'all' ? undefined : Number(statusFilter),
      }),
  });

  const { data: menusData } = useQuery({
    queryKey: ['menus-tree'],
    queryFn: () => menuApi.getTree(),
  });

  const menus = menusData || [];

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: RoleFormData) =>
      roleApi.create({
        name: data.name,
        code: data.code,
        description: data.description || '',
        status: data.status,
        menuIds: data.menuIds,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setIsCreateDialogOpen(false);
      form.reset();
      toast({ title: '创建成功', description: '角色已成功创建' });
    },
    onError: (error: Error) => {
      toast({ title: '创建失败', description: error.message, variant: 'destructive' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: RoleFormData }) =>
      roleApi.update(id, {
        name: data.name,
        code: data.code,
        description: data.description || '',
        status: data.status,
        menuIds: data.menuIds,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setIsEditDialogOpen(false);
      setCurrentRole(null);
      form.reset();
      toast({ title: '更新成功', description: '角色信息已更新' });
    },
    onError: (error: Error) => {
      toast({ title: '更新失败', description: error.message, variant: 'destructive' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => roleApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setIsDeleteDialogOpen(false);
      setCurrentRole(null);
      toast({ title: '删除成功', description: '角色已删除' });
    },
    onError: (error: Error) => {
      toast({ title: '删除失败', description: error.message, variant: 'destructive' });
    },
  });

  const updateMenusMutation = useMutation({
    mutationFn: ({ id, menuIds }: { id: number; menuIds: number[] }) =>
      roleApi.updateMenus(id, menuIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setIsMenuDialogOpen(false);
      setCurrentRole(null);
      toast({ title: '权限分配成功' });
    },
    onError: (error: Error) => {
      toast({ title: '权限分配失败', description: error.message, variant: 'destructive' });
    },
  });

  // Handlers
  const handleEdit = async (role: Role) => {
    setCurrentRole(role);
    // Fetch role details to get menuIds
    try {
      const detail = await roleApi.getById(role.id);
      form.reset({
        name: role.name,
        code: role.code,
        description: role.description || '',
        status: role.status,
        menuIds: detail.menus?.map((m) => m.id) || [],
      });
    } catch {
      form.reset({
        name: role.name,
        code: role.code,
        description: role.description || '',
        status: role.status,
        menuIds: [],
      });
    }
    setIsEditDialogOpen(true);
  };

  const handleAssignMenus = async (role: Role) => {
    setCurrentRole(role);
    // Fetch role details to get menuIds
    try {
      const detail = await roleApi.getById(role.id);
      form.reset({
        ...form.getValues(),
        menuIds: detail.menus?.map((m) => m.id) || [],
      });
    } catch {
      form.reset({
        ...form.getValues(),
        menuIds: [],
      });
    }
    setIsMenuDialogOpen(true);
  };

  const handleDelete = (role: Role) => {
    setCurrentRole(role);
    setIsDeleteDialogOpen(true);
  };

  const handleCreateSubmit = (data: RoleFormData) => {
    createMutation.mutate(data);
  };

  const handleEditSubmit = (data: RoleFormData) => {
    if (currentRole) {
      updateMutation.mutate({ id: currentRole.id, data });
    }
  };

  const handleMenuSubmit = (data: RoleFormData) => {
    if (currentRole) {
      updateMenusMutation.mutate({ id: currentRole.id, menuIds: data.menuIds });
    }
  };

  const toggleMenuExpand = (menuId: number) => {
    const newExpanded = new Set(expandedMenus);
    if (newExpanded.has(menuId)) {
      newExpanded.delete(menuId);
    } else {
      newExpanded.add(menuId);
    }
    setExpandedMenus(newExpanded);
  };

  const handleMenuCheck = (menuId: number, checked: boolean, childMenus?: MenuTreeNode[]) => {
    const currentMenuIds = form.getValues('menuIds') || [];
    let newMenuIds: number[];

    if (checked) {
      newMenuIds = [...currentMenuIds, menuId];
      // Also select all children
      if (childMenus && childMenus.length > 0) {
        const collectChildIds = (children: MenuTreeNode[]): number[] => {
          return children.flatMap((child) => [child.id, ...collectChildIds(child.children || [])]);
        };
        newMenuIds = [...new Set([...newMenuIds, ...collectChildIds(childMenus)])];
      }
    } else {
      newMenuIds = currentMenuIds.filter((id) => id !== menuId);
      // Also deselect all children
      if (childMenus && childMenus.length > 0) {
        const collectChildIds = (children: MenuTreeNode[]): number[] => {
          return children.flatMap((child) => [child.id, ...collectChildIds(child.children || [])]);
        };
        const childIds = collectChildIds(childMenus);
        newMenuIds = newMenuIds.filter((id) => !childIds.includes(id));
      }
    }

    form.setValue('menuIds', newMenuIds);
  };

  const handleSelectAllMenus = (checked: boolean) => {
    if (checked) {
      const collectAllIds = (menuList: MenuTreeNode[]): number[] => {
        return menuList.flatMap((menu) => [menu.id, ...collectAllIds(menu.children || [])]);
      };
      form.setValue('menuIds', collectAllIds(menus as MenuTreeNode[]));
    } else {
      form.setValue('menuIds', []);
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

  const renderMenuTree = (menuList: MenuTreeNode[], level: number = 0) => {
    return menuList.map((menu) => {
      const hasChildren = menu.children && menu.children.length > 0;
      const isExpanded = expandedMenus.has(menu.id);
      const isChecked = form.watch('menuIds')?.includes(menu.id);

      return (
        <div key={menu.id}>
          <div
            className="flex items-center gap-2 py-2 hover:bg-muted/50 rounded px-2"
            style={{ paddingLeft: `${level * 20 + 8}px` }}
          >
            {hasChildren ? (
              <button
                type="button"
                onClick={() => toggleMenuExpand(menu.id)}
                className="p-0.5 hover:bg-muted rounded"
              >
                {isExpanded ? (
                  <ChevronDown className="h-4 w-4" />
                ) : (
                  <ChevronRight className="h-4 w-4" />
                )}
              </button>
            ) : (
              <span className="w-5" />
            )}
            <Checkbox
              checked={isChecked}
              onCheckedChange={(checked) =>
                handleMenuCheck(menu.id, checked as boolean, menu.children)
              }
            />
            <span className="text-sm">{menu.name}</span>
            {menu.type === 'directory' && (
              <Badge variant="secondary" className="text-xs">目录</Badge>
            )}
            {menu.type === 'menu' && (
              <Badge variant="outline" className="text-xs">菜单</Badge>
            )}
            {menu.type === 'button' && (
              <Badge variant="outline" className="text-xs bg-blue-50 text-blue-700 border-blue-200">按钮</Badge>
            )}
          </div>
          {hasChildren && isExpanded && (
            <div>
              {renderMenuTree(menu.children as MenuTreeNode[], level + 1)}
            </div>
          )}
        </div>
      );
    });
  };

  const roles = rolesData?.content || [];
  const totalElements = rolesData?.totalElements || 0;
  const totalPages = rolesData?.totalPages || 0;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <CardTitle className="text-xl font-semibold">角色管理</CardTitle>
            <div className="flex flex-col sm:flex-row gap-2">
              <div className="flex gap-2">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="搜索角色名称、编码..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="pl-9 w-[200px]"
                  />
                </div>
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-[120px]">
                    <SelectValue placeholder="状态" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部状态</SelectItem>
                    <SelectItem value="1">启用</SelectItem>
                    <SelectItem value="0">禁用</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button size="sm" onClick={() => {
                form.reset({
                  name: '',
                  code: '',
                  description: '',
                  status: 1,
                  menuIds: [],
                });
                setIsCreateDialogOpen(true);
              }}>
                <Plus className="h-4 w-4 mr-1" />
                新增角色
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">ID</TableHead>
                  <TableHead>角色名称</TableHead>
                  <TableHead>角色编码</TableHead>
                  <TableHead>描述</TableHead>
                  <TableHead className="w-20">状态</TableHead>
                  <TableHead>创建时间</TableHead>
                  <TableHead className="w-24">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : roles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  roles.map((role) => (
                    <TableRow key={role.id}>
                      <TableCell>{role.id}</TableCell>
                      <TableCell className="font-medium">{role.name}</TableCell>
                      <TableCell>
                        <code className="bg-muted px-2 py-0.5 rounded text-sm">
                          {role.code}
                        </code>
                      </TableCell>
                      <TableCell className="max-w-[200px] truncate">
                        {role.description || '-'}
                      </TableCell>
                      <TableCell>{getStatusBadge(role.status)}</TableCell>
                      <TableCell>
                        {role.createdAt ? new Date(role.createdAt).toLocaleString('zh-CN') : '-'}
                      </TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleEdit(role)}>
                              <Pencil className="h-4 w-4 mr-2" />
                              编辑
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleAssignMenus(role)}>
                              <Settings className="h-4 w-4 mr-2" />
                              分配权限
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              className="text-red-600"
                              onClick={() => handleDelete(role)}
                            >
                              <Trash2 className="h-4 w-4 mr-2" />
                              删除
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          <div className="flex items-center justify-between mt-4">
            <div className="text-sm text-muted-foreground">
              共 {totalElements} 条记录，第 {page + 1} / {totalPages} 页
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
              >
                上一页
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
              >
                下一页
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>新增角色</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleCreateSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色名称 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入角色名称" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="code"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色编码 *</FormLabel>
                      <FormControl>
                        <Input placeholder="如: ROLE_ADMIN" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>描述</FormLabel>
                    <FormControl>
                      <Textarea placeholder="请输入描述" {...field} />
                    </FormControl>
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
            <DialogTitle>编辑角色</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleEditSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色名称 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入角色名称" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="code"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色编码 *</FormLabel>
                      <FormControl>
                        <Input placeholder="如: ROLE_ADMIN" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>描述</FormLabel>
                    <FormControl>
                      <Textarea placeholder="请输入描述" {...field} />
                    </FormControl>
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

      {/* Assign Menus Dialog */}
      <Dialog open={isMenuDialogOpen} onOpenChange={setIsMenuDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>分配菜单权限 - {currentRole?.name}</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleMenuSubmit)} className="space-y-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-muted-foreground">
                  已选择 {form.watch('menuIds')?.length || 0} 项
                </span>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => handleSelectAllMenus(!(form.watch('menuIds')?.length === menus.length))}
                >
                  {form.watch('menuIds')?.length === collectAllMenuIds(menus as MenuTreeNode[]).length ? '取消全选' : '全选'}
                </Button>
              </div>
              <ScrollArea className="h-80 border rounded-md p-2">
                {menus.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">暂无菜单数据</div>
                ) : (
                  renderMenuTree(menus as MenuTreeNode[])
                )}
              </ScrollArea>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsMenuDialogOpen(false)}>
                  取消
                </Button>
                <Button type="submit" disabled={updateMenusMutation.isPending}>
                  {updateMenusMutation.isPending ? '保存中...' : '保存'}
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
              确定要删除角色 &quot;{currentRole?.name}&quot; 吗？此操作不可恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-red-600 hover:bg-red-700"
              onClick={() => currentRole && deleteMutation.mutate(currentRole.id)}
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

// Helper function to collect all menu IDs
function collectAllMenuIds(menus: MenuTreeNode[]): number[] {
  return menus.flatMap((menu) => [menu.id, ...collectAllMenuIds(menu.children || [])]);
}
