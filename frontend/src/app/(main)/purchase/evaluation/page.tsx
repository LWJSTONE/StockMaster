'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, Search, Loader2, MoreHorizontal, Star, StarOff } from 'lucide-react';
import { evaluationApi, supplierApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
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
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationPrevious,
  PaginationNext,
  PaginationEllipsis,
} from '@/components/ui/pagination';
import { cn } from '@/lib/utils';

// Schema - Updated for multi-dimensional evaluation
const evaluationSchema = z.object({
  supplierId: z.number().min(1, '请选择供应商'),
  qualityScore: z.number().min(1).max(5, '质量评分在1-5之间'),
  deliveryScore: z.number().min(1).max(5, '交付评分在1-5之间'),
  serviceScore: z.number().min(1).max(5, '服务评分在1-5之间'),
  priceScore: z.number().min(1).max(5, '价格评分在1-5之间'),
  content: z.string().min(1, '请输入评价内容'),
});

type EvaluationFormValues = z.infer<typeof evaluationSchema>;

// Star Rating Component
function StarRating({
  value,
  onChange,
  disabled = false,
  size = 'default',
}: {
  value: number;
  onChange: (value: number) => void;
  disabled?: boolean;
  size?: 'default' | 'sm';
}) {
  const [hoverValue, setHoverValue] = useState(0);

  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          disabled={disabled}
          onClick={() => onChange(star)}
          onMouseEnter={() => setHoverValue(star)}
          onMouseLeave={() => setHoverValue(0)}
          className={cn(
            'focus:outline-none transition-colors',
            disabled && 'cursor-not-allowed opacity-50'
          )}
        >
          <Star
            className={cn(
              size === 'sm' ? 'h-4 w-4' : 'h-6 w-6',
              (hoverValue || value) >= star
                ? 'fill-yellow-400 text-yellow-400'
                : 'text-gray-300'
            )}
          />
        </button>
      ))}
    </div>
  );
}

// Display Star Rating
function StarDisplay({ value, size = 'default' }: { value: number; size?: 'default' | 'sm' }) {
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={cn(
            size === 'sm' ? 'h-3 w-3' : 'h-4 w-4',
            value >= star ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'
          )}
        />
      ))}
    </div>
  );
}

export default function EvaluationPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [supplierIdFilter, setSupplierIdFilter] = useState<string>('all');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedEvaluation, setSelectedEvaluation] = useState<number | null>(null);
  const [editingEvaluation, setEditingEvaluation] = useState<{
    id: number;
    supplierId: number;
    qualityScore: number;
    deliveryScore: number;
    serviceScore: number;
    priceScore: number;
    content: string;
  } | null>(null);

  // Form
  const form = useForm<EvaluationFormValues>({
    resolver: zodResolver(evaluationSchema),
    defaultValues: {
      supplierId: 0,
      qualityScore: 5,
      deliveryScore: 5,
      serviceScore: 5,
      priceScore: 5,
      content: '',
    },
  });

  // Query suppliers for dropdown
  const { data: suppliersData } = useQuery({
    queryKey: ['suppliers-all'],
    queryFn: () => supplierApi.getAll(),
  });

  // Query evaluations
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['evaluations', page, size, supplierIdFilter],
    queryFn: () =>
      evaluationApi.getList({
        page: page + 1,
        size,
        supplierId: supplierIdFilter === 'all' ? undefined : Number(supplierIdFilter),
      }),
  });

  // Calculate average score from dimension scores
  const calculateAverageScore = (quality: number, delivery: number, service: number, price: number) => {
    return ((quality + delivery + service + price) / 4).toFixed(1);
  };

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: EvaluationFormValues) => {
      const avgScore = Number(calculateAverageScore(data.qualityScore, data.deliveryScore, data.serviceScore, data.priceScore));
      return evaluationApi.create({
        supplierId: data.supplierId,
        score: avgScore,
        content: data.content,
      });
    },
    onSuccess: () => {
      toast.success('评价创建成功');
      queryClient.invalidateQueries({ queryKey: ['evaluations'] });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      toast.error(error.message || '创建失败');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: EvaluationFormValues }) => {
      const avgScore = Number(calculateAverageScore(data.qualityScore, data.deliveryScore, data.serviceScore, data.priceScore));
      return evaluationApi.update(id, {
        supplierId: data.supplierId,
        score: avgScore,
        content: data.content,
      });
    },
    onSuccess: () => {
      toast.success('评价更新成功');
      queryClient.invalidateQueries({ queryKey: ['evaluations'] });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      toast.error(error.message || '更新失败');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => evaluationApi.delete(id),
    onSuccess: () => {
      toast.success('评价删除成功');
      queryClient.invalidateQueries({ queryKey: ['evaluations'] });
      setDeleteDialogOpen(false);
      setSelectedEvaluation(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || '删除失败');
    },
  });

  // Handlers
  const handleOpenCreate = () => {
    setEditingEvaluation(null);
    form.reset({
      supplierId: 0,
      qualityScore: 5,
      deliveryScore: 5,
      serviceScore: 5,
      priceScore: 5,
      content: '',
    });
    setDialogOpen(true);
  };

  const handleOpenEdit = (evaluation: {
    id: number;
    supplierId: number;
    supplierName: string;
    score: number;
    content: string;
    evaluator: string;
    createdAt: string;
  }) => {
    // For editing, we use the score as all dimension scores since backend only stores single score
    setEditingEvaluation({
      id: evaluation.id,
      supplierId: evaluation.supplierId,
      qualityScore: evaluation.score,
      deliveryScore: evaluation.score,
      serviceScore: evaluation.score,
      priceScore: evaluation.score,
      content: evaluation.content,
    });
    form.reset({
      supplierId: evaluation.supplierId,
      qualityScore: evaluation.score,
      deliveryScore: evaluation.score,
      serviceScore: evaluation.score,
      priceScore: evaluation.score,
      content: evaluation.content,
    });
    setSelectedEvaluation(evaluation.id);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedEvaluation(null);
    setEditingEvaluation(null);
    form.reset();
  };

  const handleSubmit = (data: EvaluationFormValues) => {
    if (editingEvaluation && selectedEvaluation) {
      updateMutation.mutate({ id: selectedEvaluation, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleDelete = () => {
    if (selectedEvaluation) {
      deleteMutation.mutate(selectedEvaluation);
    }
  };

  const evaluations = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const suppliers = suppliersData || [];

  // Watch form values for real-time score display
  const watchedScores = form.watch(['qualityScore', 'deliveryScore', 'serviceScore', 'priceScore']);
  const avgScoreDisplay = calculateAverageScore(
    watchedScores[0],
    watchedScores[1],
    watchedScores[2],
    watchedScores[3]
  );

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <CardTitle className="text-xl">供应商评价</CardTitle>
            <Button onClick={handleOpenCreate}>
              <Plus className="mr-2 h-4 w-4" />
              新增评价
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {/* Filter */}
          <div className="flex flex-col md:flex-row gap-4 mb-6">
            <Select value={supplierIdFilter} onValueChange={(value) => { setSupplierIdFilter(value); setPage(0); }}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="筛选供应商" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部供应商</SelectItem>
                {suppliers.map((supplier) => (
                  <SelectItem key={supplier.id} value={String(supplier.id)}>
                    {supplier.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Table */}
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>供应商名称</TableHead>
                  <TableHead>综合评分</TableHead>
                  <TableHead>评价内容</TableHead>
                  <TableHead>评价人</TableHead>
                  <TableHead>评价时间</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin mx-auto text-muted-foreground" />
                    </TableCell>
                  </TableRow>
                ) : evaluations.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  evaluations.map((evaluation) => (
                    <TableRow key={evaluation.id}>
                      <TableCell className="font-medium">{evaluation.supplierName}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <StarDisplay value={evaluation.score} />
                          <span className="text-sm font-medium">{evaluation.score.toFixed(1)}</span>
                        </div>
                      </TableCell>
                      <TableCell className="max-w-[300px] truncate">{evaluation.content}</TableCell>
                      <TableCell>{evaluation.evaluator}</TableCell>
                      <TableCell>
                        {evaluation.createdAt ? new Date(evaluation.createdAt).toLocaleDateString() : '-'}
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleOpenEdit(evaluation)}>
                              <Pencil className="mr-2 h-4 w-4" />
                              编辑
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              className="text-destructive"
                              onClick={() => {
                                setSelectedEvaluation(evaluation.id);
                                setDeleteDialogOpen(true);
                              }}
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
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
          {totalPages > 1 && (
            <div className="mt-4">
              <Pagination>
                <PaginationContent>
                  <PaginationItem>
                    <PaginationPrevious
                      onClick={() => page > 0 && setPage(page - 1)}
                      className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                    />
                  </PaginationItem>
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i;
                    } else if (page < 2) {
                      pageNum = i;
                    } else if (page > totalPages - 3) {
                      pageNum = totalPages - 5 + i;
                    } else {
                      pageNum = page - 2 + i;
                    }
                    return (
                      <PaginationItem key={pageNum}>
                        <PaginationLink
                          isActive={page === pageNum}
                          onClick={() => setPage(pageNum)}
                          className="cursor-pointer"
                        >
                          {pageNum + 1}
                        </PaginationLink>
                      </PaginationItem>
                    );
                  })}
                  {totalPages > 5 && page < totalPages - 3 && (
                    <PaginationItem>
                      <PaginationEllipsis />
                    </PaginationItem>
                  )}
                  <PaginationItem>
                    <PaginationNext
                      onClick={() => page < totalPages - 1 && setPage(page + 1)}
                      className={page === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                    />
                  </PaginationItem>
                </PaginationContent>
              </Pagination>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editingEvaluation ? '编辑评价' : '新增评价'}</DialogTitle>
            <DialogDescription>
              {editingEvaluation ? '修改供应商评价信息' : '填写供应商评价信息'}
            </DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="supplierId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>供应商 <span className="text-destructive">*</span></FormLabel>
                    <Select
                      onValueChange={(value) => field.onChange(Number(value))}
                      value={field.value ? String(field.value) : ''}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="请选择供应商" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {suppliers.map((supplier) => (
                          <SelectItem key={supplier.id} value={String(supplier.id)}>
                            {supplier.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Rating Dimensions */}
              <div className="space-y-4 p-4 border rounded-lg bg-muted/30">
                <h4 className="font-medium text-sm">评分维度</h4>
                
                <FormField
                  control={form.control}
                  name="qualityScore"
                  render={({ field }) => (
                    <FormItem>
                      <div className="flex items-center justify-between">
                        <FormLabel className="text-sm">质量评分</FormLabel>
                        <StarRating
                          value={field.value}
                          onChange={field.onChange}
                        />
                      </div>
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="deliveryScore"
                  render={({ field }) => (
                    <FormItem>
                      <div className="flex items-center justify-between">
                        <FormLabel className="text-sm">交付评分</FormLabel>
                        <StarRating
                          value={field.value}
                          onChange={field.onChange}
                        />
                      </div>
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="serviceScore"
                  render={({ field }) => (
                    <FormItem>
                      <div className="flex items-center justify-between">
                        <FormLabel className="text-sm">服务评分</FormLabel>
                        <StarRating
                          value={field.value}
                          onChange={field.onChange}
                        />
                      </div>
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="priceScore"
                  render={({ field }) => (
                    <FormItem>
                      <div className="flex items-center justify-between">
                        <FormLabel className="text-sm">价格评分</FormLabel>
                        <StarRating
                          value={field.value}
                          onChange={field.onChange}
                        />
                      </div>
                    </FormItem>
                  )}
                />

                <div className="flex items-center justify-between pt-2 border-t">
                  <span className="font-medium text-sm">综合评分</span>
                  <div className="flex items-center gap-2">
                    <StarDisplay value={Number(avgScoreDisplay)} />
                    <span className="text-lg font-bold text-primary">{avgScoreDisplay}</span>
                  </div>
                </div>
              </div>

              <FormField
                control={form.control}
                name="content"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>评价内容 <span className="text-destructive">*</span></FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="请输入评价内容"
                        className="min-h-[100px]"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <DialogFooter>
                <Button type="button" variant="outline" onClick={handleCloseDialog}>
                  取消
                </Button>
                <Button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                >
                  {(createMutation.isPending || updateMutation.isPending) && (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  )}
                  {editingEvaluation ? '保存' : '创建'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              此操作将永久删除该评价，删除后无法恢复。确定要继续吗？
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
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
