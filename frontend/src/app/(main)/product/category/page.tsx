'use client';

import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  ChevronRight,
  ChevronDown,
  Plus,
  Pencil,
  Trash2,
  Folder,
  FolderOpen,
  MoreHorizontal,
  Loader2,
  Layers,
  GripVertical,
} from 'lucide-react';
import { categoryApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { ScrollArea } from '@/components/ui/scroll-area';
import { toast } from 'sonner';
import type { Category } from '@/types';

// Category form schema
const categorySchema = z.object({
  parentId: z.number().nullable(),
  name: z.string().min(1, '请输入分类名称'),
  code: z.string().min(1, '请输入分类编码'),
  sort: z.number().min(0, '排序不能为负数'),
  status: z.number(),
});

type CategoryFormValues = z.infer<typeof categorySchema>;

// Status options
const statusOptions = [
  { value: 1, label: '启用', color: 'bg-green-500' },
  { value: 0, label: '禁用', color: 'bg-gray-500' },
];

// Tree node component
interface TreeNodeProps {
  category: Category;
  level: number;
  expandedIds: number[];
  onToggle: (id: number) => void;
  onEdit: (category: Category) => void;
  onDelete: (id: number) => void;
  onAddChild: (parentId: number) => void;
  onToggleStatus: (category: Category) => void;
}

function TreeNode({
  category,
  level,
  expandedIds,
  onToggle,
  onEdit,
  onDelete,
  onAddChild,
  onToggleStatus,
}: TreeNodeProps) {
  const hasChildren = category.children && category.children.length > 0;
  const isExpanded = expandedIds.includes(category.id);
  const indent = level * 24;

  return (
    <div>
      <div
        className="flex items-center gap-2 py-2 px-3 hover:bg-muted/50 rounded-lg group"
        style={{ paddingLeft: `${indent + 12}px` }}
      >
        {/* Expand/Collapse Button */}
        <div className="w-5 h-5 flex items-center justify-center">
          {hasChildren ? (
            <button
              onClick={() => onToggle(category.id)}
              className="hover:bg-muted rounded p-0.5"
            >
              {isExpanded ? (
                <ChevronDown className="h-4 w-4" />
              ) : (
                <ChevronRight className="h-4 w-4" />
              )}
            </button>
          ) : (
            <span className="w-4" />
          )}
        </div>

        {/* Folder Icon */}
        {hasChildren ? (
          isExpanded ? (
            <FolderOpen className="h-4 w-4 text-amber-500" />
          ) : (
            <Folder className="h-4 w-4 text-amber-500" />
          )
        ) : (
          <Folder className="h-4 w-4 text-muted-foreground" />
        )}

        {/* Category Info */}
        <div className="flex-1 flex items-center gap-2">
          <span className="font-medium">{category.name}</span>
          <span className="text-xs text-muted-foreground font-mono">
            {category.code}
          </span>
          <Badge
            variant={category.status === 1 ? 'default' : 'secondary'}
            className={`text-xs ${category.status === 1 ? 'bg-green-500 hover:bg-green-600' : ''}`}
          >
            {category.status === 1 ? '启用' : '禁用'}
          </Badge>
          <span className="text-xs text-muted-foreground">
            排序: {category.sort}
          </span>
        </div>

        {/* Actions */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => onEdit(category)}>
              <Pencil className="mr-2 h-4 w-4" />
              编辑
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => onAddChild(category.id)}>
              <Plus className="mr-2 h-4 w-4" />
              添加子分类
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => onToggleStatus(category)}>
              {category.status === 1 ? '禁用' : '启用'}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="text-destructive"
              onClick={() => onDelete(category.id)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              删除
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Children */}
      {hasChildren && isExpanded && (
        <div>
          {category.children!.map((child) => (
            <TreeNode
              key={child.id}
              category={child}
              level={level + 1}
              expandedIds={expandedIds}
              onToggle={onToggle}
              onEdit={onEdit}
              onDelete={onDelete}
              onAddChild={onAddChild}
              onToggleStatus={onToggleStatus}
            />
          ))}
        </div>
      )}
    </div>
  );
}

// Flatten categories for parent select
function flattenCategoriesForSelect(
  categories: Category[],
  level = 0,
  excludeId?: number
): { id: number; name: string; level: number }[] {
  const result: { id: number; name: string; level: number }[] = [];
  for (const cat of categories) {
    if (cat.id === excludeId) continue;
    result.push({ id: cat.id, name: cat.name, level });
    if (cat.children && cat.children.length > 0) {
      result.push(...flattenCategoriesForSelect(cat.children, level + 1, excludeId));
    }
  }
  return result;
}

export default function CategoryManagePage() {
  const queryClient = useQueryClient();

  // State
  const [expandedIds, setExpandedIds] = useState<number[]>([]);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [parentForNew, setParentForNew] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  // Queries
  const { data: categoriesData, isLoading } = useQuery({
    queryKey: ['categories-tree'],
    queryFn: categoryApi.getTree,
  });

  const flatCategoriesForSelect = useMemo(() => {
    if (!categoriesData) return [];
    return flattenCategoriesForSelect(categoriesData, 0, editingCategory?.id);
  }, [categoriesData, editingCategory?.id]);

  // Form
  const form = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      parentId: null,
      name: '',
      code: '',
      sort: 0,
      status: 1,
    },
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: CategoryFormValues) => categoryApi.create(data),
    onSuccess: () => {
      toast.success('分类创建成功');
      setIsFormOpen(false);
      form.reset();
      queryClient.invalidateQueries({ queryKey: ['categories-tree'] });
    },
    onError: (error: Error) => {
      toast.error(error.message || '创建失败');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CategoryFormValues }) =>
      categoryApi.update(id, data),
    onSuccess: () => {
      toast.success('分类更新成功');
      setIsFormOpen(false);
      setEditingCategory(null);
      form.reset();
      queryClient.invalidateQueries({ queryKey: ['categories-tree'] });
    },
    onError: (error: Error) => {
      toast.error(error.message || '更新失败');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => categoryApi.delete(id),
    onSuccess: () => {
      toast.success('分类删除成功');
      setDeleteId(null);
      queryClient.invalidateQueries({ queryKey: ['categories-tree'] });
    },
    onError: (error: Error) => {
      toast.error(error.message || '删除失败');
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CategoryFormValues }) =>
      categoryApi.update(id, data),
    onSuccess: () => {
      toast.success('状态更新成功');
      queryClient.invalidateQueries({ queryKey: ['categories-tree'] });
    },
    onError: (error: Error) => {
      toast.error(error.message || '状态更新失败');
    },
  });

  // Handlers
  const handleToggle = (id: number) => {
    if (expandedIds.includes(id)) {
      setExpandedIds(expandedIds.filter((i) => i !== id));
    } else {
      setExpandedIds([...expandedIds, id]);
    }
  };

  const handleExpandAll = () => {
    if (!categoriesData) return;
    const getAllIds = (cats: Category[]): number[] => {
      return cats.flatMap((cat) =>
        cat.children && cat.children.length > 0
          ? [cat.id, ...getAllIds(cat.children)]
          : []
      );
    };
    setExpandedIds(getAllIds(categoriesData));
  };

  const handleCollapseAll = () => {
    setExpandedIds([]);
  };

  const handleAdd = () => {
    setEditingCategory(null);
    setParentForNew(null);
    form.reset({
      parentId: null,
      name: '',
      code: '',
      sort: 0,
      status: 1,
    });
    setIsFormOpen(true);
  };

  const handleAddChild = (parentId: number) => {
    setEditingCategory(null);
    setParentForNew(parentId);
    form.reset({
      parentId: parentId,
      name: '',
      code: '',
      sort: 0,
      status: 1,
    });
    setIsFormOpen(true);
  };

  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    setParentForNew(null);
    form.reset({
      parentId: category.parentId,
      name: category.name,
      code: category.code,
      sort: category.sort,
      status: category.status,
    });
    setIsFormOpen(true);
  };

  const handleSubmit = (data: CategoryFormValues) => {
    if (editingCategory) {
      updateMutation.mutate({ id: editingCategory.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleToggleStatus = (category: Category) => {
    const newStatus = category.status === 1 ? 0 : 1;
    updateStatusMutation.mutate({
      id: category.id,
      data: {
        parentId: category.parentId,
        name: category.name,
        code: category.code,
        sort: category.sort,
        status: newStatus,
      },
    });
  };

  // Count all categories
  const countCategories = (cats: Category[]): number => {
    return cats.reduce((acc, cat) => {
      return acc + 1 + (cat.children ? countCategories(cat.children) : 0);
    }, 0);
  };

  const totalCategories = categoriesData ? countCategories(categoriesData) : 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">分类管理</h1>
          <p className="text-muted-foreground">
            管理商品分类，支持多级分类结构
          </p>
        </div>
        <Button onClick={handleAdd}>
          <Plus className="mr-2 h-4 w-4" />
          新增分类
        </Button>
      </div>

      {/* Stats Card */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总分类数</CardTitle>
            <Layers className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCategories}</div>
            <p className="text-xs text-muted-foreground">
              {categoriesData?.length || 0} 个一级分类
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">启用分类</CardTitle>
            <FolderOpen className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {categoriesData
                ? countCategories(
                    categoriesData.filter((c) => c.status === 1)
                  )
                : 0}
            </div>
            <p className="text-xs text-muted-foreground">可正常使用</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">禁用分类</CardTitle>
            <Folder className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-500">
              {categoriesData
                ? countCategories(
                    categoriesData.filter((c) => c.status === 0)
                  )
                : 0}
            </div>
            <p className="text-xs text-muted-foreground">已暂停使用</p>
          </CardContent>
        </Card>
      </div>

      {/* Category Tree Card */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-base">分类树</CardTitle>
              <CardDescription>点击展开/折叠查看子分类</CardDescription>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={handleExpandAll}>
                展开全部
              </Button>
              <Button variant="outline" size="sm" onClick={handleCollapseAll}>
                折叠全部
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="flex items-center gap-2">
                  <Skeleton className="h-5 w-5" />
                  <Skeleton className="h-4 w-[200px]" />
                  <Skeleton className="h-4 w-[100px]" />
                </div>
              ))}
            </div>
          ) : !categoriesData || categoriesData.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
              <Folder className="h-12 w-12 mb-4 opacity-50" />
              <p>暂无分类数据</p>
              <Button variant="link" onClick={handleAdd} className="mt-2">
                点击新增分类
              </Button>
            </div>
          ) : (
            <ScrollArea className="h-[500px] pr-4">
              <div className="space-y-1">
                {categoriesData.map((category) => (
                  <TreeNode
                    key={category.id}
                    category={category}
                    level={0}
                    expandedIds={expandedIds}
                    onToggle={handleToggle}
                    onEdit={handleEdit}
                    onDelete={setDeleteId}
                    onAddChild={handleAddChild}
                    onToggleStatus={handleToggleStatus}
                  />
                ))}
              </div>
            </ScrollArea>
          )}
        </CardContent>
      </Card>

      {/* Add/Edit Dialog */}
      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingCategory ? '编辑分类' : parentForNew ? '新增子分类' : '新增分类'}
            </DialogTitle>
            <DialogDescription>
              {editingCategory ? '修改分类信息' : '填写分类基本信息'}
            </DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="parentId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>上级分类</FormLabel>
                    <Select
                      value={field.value?.toString() || 'none'}
                      onValueChange={(value) =>
                        field.onChange(value === 'none' ? null : Number(value))
                      }
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择上级分类（可选）" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="none">无（作为一级分类）</SelectItem>
                        {flatCategoriesForSelect.map((cat) => (
                          <SelectItem key={cat.id} value={cat.id.toString()}>
                            {'　'.repeat(cat.level)}
                            {cat.name}
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
                      <FormLabel>分类名称 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入分类名称" {...field} />
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
                      <FormLabel>分类编码 *</FormLabel>
                      <FormControl>
                        <Input placeholder="请输入分类编码" {...field} />
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
                          placeholder="排序值"
                          {...field}
                          onChange={(e) =>
                            field.onChange(parseInt(e.target.value) || 0)
                          }
                        />
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
                      <Select
                        value={field.value.toString()}
                        onValueChange={(value) => field.onChange(Number(value))}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {statusOptions.map((opt) => (
                            <SelectItem key={opt.value} value={opt.value.toString()}>
                              {opt.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <DialogFooter>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setIsFormOpen(false)}
                >
                  取消
                </Button>
                <Button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                >
                  {(createMutation.isPending || updateMutation.isPending) && (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  )}
                  {editingCategory ? '保存' : '创建'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              此操作将永久删除该分类。如果该分类下有子分类，需要先删除子分类。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => deleteId && deleteMutation.mutate(deleteId)}
            >
              {deleteMutation.isPending && (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              )}
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
