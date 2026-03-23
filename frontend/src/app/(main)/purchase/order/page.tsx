'use client';

import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import {
  Plus, Pencil, Trash2, Search, Loader2, MoreHorizontal,
  Eye, Send, Check, X, Ban, Package, CalendarIcon, Minus
} from 'lucide-react';
import { purchaseOrderApi, supplierApi, productApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
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
  DropdownMenuSeparator,
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
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';
import type { OrderStatus, PurchaseOrder, PurchaseOrderItem } from '@/types';

// Order status labels and colors
const orderStatusConfig: Record<OrderStatus, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }> = {
  DRAFT: { label: '草稿', variant: 'secondary' },
  PENDING: { label: '待审核', variant: 'default' },
  APPROVED: { label: '已审核', variant: 'default' },
  REJECTED: { label: '已拒绝', variant: 'destructive' },
  CANCELLED: { label: '已取消', variant: 'outline' },
  COMPLETED: { label: '已完成', variant: 'default' },
};

// Schema
const orderItemSchema = z.object({
  productId: z.number().min(1, '请选择商品'),
  quantity: z.number().min(1, '数量必须大于0'),
  unitPrice: z.number().min(0.01, '单价必须大于0'),
});

const orderSchema = z.object({
  supplierId: z.number().min(1, '请选择供应商'),
  remark: z.string().optional(),
  items: z.array(orderItemSchema).min(1, '至少添加一个商品'),
});

type OrderFormValues = z.infer<typeof orderSchema>;

export default function PurchaseOrderPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [supplierIdFilter, setSupplierIdFilter] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [startDate, setStartDate] = useState<Date | undefined>();
  const [endDate, setEndDate] = useState<Date | undefined>();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<number | null>(null);
  const [editingOrder, setEditingOrder] = useState<PurchaseOrder | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [viewingOrder, setViewingOrder] = useState<PurchaseOrder | null>(null);

  // Form
  const form = useForm<OrderFormValues>({
    resolver: zodResolver(orderSchema),
    defaultValues: {
      supplierId: 0,
      remark: '',
      items: [{ productId: 0, quantity: 1, unitPrice: 0 }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'items',
  });

  // Queries
  const { data: suppliersData } = useQuery({
    queryKey: ['suppliers-all'],
    queryFn: () => supplierApi.getAll(),
  });

  const { data: productsData } = useQuery({
    queryKey: ['products-select'],
    queryFn: () => productApi.getSelect(),
  });

  const { data, isLoading } = useQuery({
    queryKey: ['purchase-orders', page, size, searchKeyword, supplierIdFilter, statusFilter, startDate, endDate],
    queryFn: () =>
      purchaseOrderApi.getList({
        page: page + 1,
        size,
        keyword: searchKeyword || undefined,
        supplierId: supplierIdFilter === 'all' ? undefined : Number(supplierIdFilter),
        status: statusFilter === 'all' ? undefined : statusFilter as OrderStatus,
        startDate: startDate ? format(startDate, 'yyyy-MM-dd') : undefined,
        endDate: endDate ? format(endDate, 'yyyy-MM-dd') : undefined,
      }),
  });

  // Detail query
  const { data: orderDetail, isLoading: isLoadingDetail, refetch: refetchDetail } = useQuery({
    queryKey: ['purchase-order-detail', selectedOrder],
    queryFn: () => selectedOrder ? purchaseOrderApi.getById(selectedOrder) : null,
    enabled: !!selectedOrder && (detailDialogOpen || dialogOpen),
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: OrderFormValues) => purchaseOrderApi.create(data),
    onSuccess: () => {
      toast.success('订单创建成功');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      toast.error(error.message || '创建失败');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: OrderFormValues }) =>
      purchaseOrderApi.update(id, data),
    onSuccess: () => {
      toast.success('订单更新成功');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      handleCloseDialog();
    },
    onError: (error: Error) => {
      toast.error(error.message || '更新失败');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => purchaseOrderApi.delete(id),
    onSuccess: () => {
      toast.success('订单删除成功');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setDeleteDialogOpen(false);
      setSelectedOrder(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || '删除失败');
    },
  });

  const submitMutation = useMutation({
    mutationFn: (id: number) => purchaseOrderApi.submit(id),
    onSuccess: () => {
      toast.success('订单已提交审核');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
    },
    onError: (error: Error) => {
      toast.error(error.message || '提交失败');
    },
  });

  const approveMutation = useMutation({
    mutationFn: (id: number) => purchaseOrderApi.approve(id),
    onSuccess: () => {
      toast.success('订单已审核通过');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setDetailDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '审核失败');
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      purchaseOrderApi.reject(id, reason),
    onSuccess: () => {
      toast.success('订单已拒绝');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setRejectDialogOpen(false);
      setRejectReason('');
      setDetailDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '拒绝失败');
    },
  });

  const cancelMutation = useMutation({
    mutationFn: (id: number) => purchaseOrderApi.cancel(id),
    onSuccess: () => {
      toast.success('订单已取消');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      setDetailDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '取消失败');
    },
  });

  const receiveMutation = useMutation({
    mutationFn: (id: number) => purchaseOrderApi.receive(id),
    onSuccess: () => {
      toast.success('订单已收货，库存已更新');
      queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      setDetailDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '收货失败');
    },
  });

  // Handlers
  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(0);
  };

  const handleOpenCreate = () => {
    setEditingOrder(null);
    form.reset({
      supplierId: 0,
      remark: '',
      items: [{ productId: 0, quantity: 1, unitPrice: 0 }],
    });
    setDialogOpen(true);
  };

  const handleOpenEdit = async (order: { id: number }) => {
    setSelectedOrder(order.id);
    setEditingOrder(null);
    try {
      const detail = await purchaseOrderApi.getById(order.id);
      setEditingOrder(detail);
      form.reset({
        supplierId: detail.supplierId,
        remark: detail.remark || '',
        items: detail.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
        })),
      });
      setDialogOpen(true);
    } catch (error) {
      toast.error('获取订单详情失败');
    }
  };

  const handleViewDetail = async (order: { id: number }) => {
    setSelectedOrder(order.id);
    setDetailDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedOrder(null);
    setEditingOrder(null);
    form.reset();
  };

  const handleSubmit = (data: OrderFormValues) => {
    if (editingOrder && selectedOrder) {
      updateMutation.mutate({ id: selectedOrder, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleDelete = () => {
    if (selectedOrder) {
      deleteMutation.mutate(selectedOrder);
    }
  };

  const handleReject = () => {
    if (selectedOrder && rejectReason) {
      rejectMutation.mutate({ id: selectedOrder, reason: rejectReason });
    }
  };

  const addItem = () => {
    append({ productId: 0, quantity: 1, unitPrice: 0 });
  };

  const orders = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const suppliers = suppliersData || [];
  const products = productsData || [];

  // Calculate total amount for form
  const watchedItems = form.watch('items');
  const formTotalAmount = watchedItems.reduce((sum, item) => {
    return sum + (item.quantity || 0) * (item.unitPrice || 0);
  }, 0);

  // Get product info by ID
  const getProductInfo = (productId: number) => {
    return products.find(p => p.id === productId);
  };

  // Handle product selection to auto-fill unit price
  const handleProductSelect = (index: number, productId: number) => {
    const product = getProductInfo(productId);
    if (product) {
      form.setValue(`items.${index}.unitPrice`, product.salePrice);
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <CardTitle className="text-xl">采购订单</CardTitle>
            <Button onClick={handleOpenCreate}>
              <Plus className="mr-2 h-4 w-4" />
              新增订单
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {/* Search & Filters */}
          <div className="flex flex-col gap-4 mb-6">
            <div className="flex flex-wrap gap-2">
              <Input
                placeholder="搜索订单编号..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="w-[200px]"
              />
              <Button variant="secondary" onClick={handleSearch}>
                <Search className="mr-2 h-4 w-4" />
                搜索
              </Button>
              <Select value={supplierIdFilter} onValueChange={(v) => { setSupplierIdFilter(v); setPage(0); }}>
                <SelectTrigger className="w-[150px]">
                  <SelectValue placeholder="供应商" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部供应商</SelectItem>
                  {suppliers.map((s) => (
                    <SelectItem key={s.id} value={String(s.id)}>{s.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select value={statusFilter} onValueChange={(v) => { setStatusFilter(v); setPage(0); }}>
                <SelectTrigger className="w-[120px]">
                  <SelectValue placeholder="状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部状态</SelectItem>
                  {Object.entries(orderStatusConfig).map(([key, { label }]) => (
                    <SelectItem key={key} value={key}>{label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" size="sm">
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {startDate ? format(startDate, 'yyyy-MM-dd') : '开始日期'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={startDate}
                    onSelect={(date) => { setStartDate(date); setPage(0); }}
                    locale={zhCN}
                  />
                </PopoverContent>
              </Popover>
              <span className="text-muted-foreground">至</span>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" size="sm">
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {endDate ? format(endDate, 'yyyy-MM-dd') : '结束日期'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={endDate}
                    onSelect={(date) => { setEndDate(date); setPage(0); }}
                    locale={zhCN}
                  />
                </PopoverContent>
              </Popover>
              {(startDate || endDate) && (
                <Button variant="ghost" size="sm" onClick={() => { setStartDate(undefined); setEndDate(undefined); }}>
                  清除日期
                </Button>
              )}
            </div>
          </div>

          {/* Table */}
          <div className="rounded-md border overflow-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>订单编号</TableHead>
                  <TableHead>供应商</TableHead>
                  <TableHead className="text-right">商品数量</TableHead>
                  <TableHead className="text-right">总金额</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>创建人</TableHead>
                  <TableHead>创建时间</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin mx-auto text-muted-foreground" />
                    </TableCell>
                  </TableRow>
                ) : orders.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  orders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell className="font-medium">{order.orderNo}</TableCell>
                      <TableCell>{order.supplierName}</TableCell>
                      <TableCell className="text-right">{order.totalAmount > 0 ? '1' : '0'}</TableCell>
                      <TableCell className="text-right font-medium">
                        ¥{order.totalAmount?.toLocaleString() || '0.00'}
                      </TableCell>
                      <TableCell>
                        <Badge variant={orderStatusConfig[order.status as OrderStatus]?.variant || 'secondary'}>
                          {orderStatusConfig[order.status as OrderStatus]?.label || order.status}
                        </Badge>
                      </TableCell>
                      <TableCell>{order.creator}</TableCell>
                      <TableCell>
                        {order.createdAt ? new Date(order.createdAt).toLocaleDateString() : '-'}
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleViewDetail(order)}>
                              <Eye className="mr-2 h-4 w-4" />
                              查看详情
                            </DropdownMenuItem>
                            {order.status === 'DRAFT' && (
                              <>
                                <DropdownMenuItem onClick={() => handleOpenEdit(order)}>
                                  <Pencil className="mr-2 h-4 w-4" />
                                  编辑
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => submitMutation.mutate(order.id)}>
                                  <Send className="mr-2 h-4 w-4" />
                                  提交审核
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => {
                                    setSelectedOrder(order.id);
                                    setDeleteDialogOpen(true);
                                  }}
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  删除
                                </DropdownMenuItem>
                              </>
                            )}
                            {order.status === 'PENDING' && (
                              <>
                                <DropdownMenuItem onClick={() => approveMutation.mutate(order.id)}>
                                  <Check className="mr-2 h-4 w-4" />
                                  审核通过
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => {
                                    setSelectedOrder(order.id);
                                    setRejectDialogOpen(true);
                                  }}
                                >
                                  <X className="mr-2 h-4 w-4" />
                                  审核拒绝
                                </DropdownMenuItem>
                              </>
                            )}
                            {order.status === 'APPROVED' && (
                              <>
                                <DropdownMenuItem onClick={() => receiveMutation.mutate(order.id)}>
                                  <Package className="mr-2 h-4 w-4" />
                                  确认收货
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => cancelMutation.mutate(order.id)}>
                                  <Ban className="mr-2 h-4 w-4" />
                                  取消订单
                                </DropdownMenuItem>
                              </>
                            )}
                            {(order.status === 'REJECTED' || order.status === 'CANCELLED') && (
                              <DropdownMenuItem
                                className="text-destructive"
                                onClick={() => {
                                  setSelectedOrder(order.id);
                                  setDeleteDialogOpen(true);
                                }}
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                                删除
                              </DropdownMenuItem>
                            )}
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

      {/* Create/Edit Order Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{editingOrder ? '编辑采购订单' : '新增采购订单'}</DialogTitle>
            <DialogDescription>
              {editingOrder ? '修改采购订单信息' : '创建新的采购订单'}
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

              {/* Order Items */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <FormLabel>商品明细 <span className="text-destructive">*</span></FormLabel>
                  <Button type="button" variant="outline" size="sm" onClick={addItem}>
                    <Plus className="mr-2 h-4 w-4" />
                    添加商品
                  </Button>
                </div>

                <div className="rounded-md border">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead className="w-[200px]">商品</TableHead>
                        <TableHead className="w-[100px]">规格</TableHead>
                        <TableHead className="w-[80px]">单位</TableHead>
                        <TableHead className="w-[100px]">数量</TableHead>
                        <TableHead className="w-[120px]">单价</TableHead>
                        <TableHead className="w-[100px]">小计</TableHead>
                        <TableHead className="w-[50px]"></TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {fields.map((field, index) => {
                        const currentItem = watchedItems[index];
                        const productInfo = getProductInfo(currentItem?.productId || 0);
                        return (
                          <TableRow key={field.id}>
                            <TableCell>
                              <FormField
                                control={form.control}
                                name={`items.${index}.productId`}
                                render={({ field }) => (
                                  <FormItem>
                                    <Select
                                      onValueChange={(value) => {
                                        field.onChange(Number(value));
                                        handleProductSelect(index, Number(value));
                                      }}
                                      value={field.value ? String(field.value) : ''}
                                    >
                                      <FormControl>
                                        <SelectTrigger>
                                          <SelectValue placeholder="选择商品" />
                                        </SelectTrigger>
                                      </FormControl>
                                      <SelectContent>
                                        {products.map((product) => (
                                          <SelectItem key={product.id} value={String(product.id)}>
                                            {product.name}
                                          </SelectItem>
                                        ))}
                                      </SelectContent>
                                    </Select>
                                  </FormItem>
                                )}
                              />
                            </TableCell>
                            <TableCell className="text-muted-foreground">
                              {productInfo?.specification || '-'}
                            </TableCell>
                            <TableCell className="text-muted-foreground">
                              {productInfo?.unit || '-'}
                            </TableCell>
                            <TableCell>
                              <FormField
                                control={form.control}
                                name={`items.${index}.quantity`}
                                render={({ field }) => (
                                  <FormItem>
                                    <FormControl>
                                      <Input
                                        type="number"
                                        min={1}
                                        {...field}
                                        onChange={(e) => field.onChange(Number(e.target.value))}
                                      />
                                    </FormControl>
                                  </FormItem>
                                )}
                              />
                            </TableCell>
                            <TableCell>
                              <FormField
                                control={form.control}
                                name={`items.${index}.unitPrice`}
                                render={({ field }) => (
                                  <FormItem>
                                    <FormControl>
                                      <Input
                                        type="number"
                                        step="0.01"
                                        min={0}
                                        {...field}
                                        onChange={(e) => field.onChange(Number(e.target.value))}
                                      />
                                    </FormControl>
                                  </FormItem>
                                )}
                              />
                            </TableCell>
                            <TableCell className="font-medium">
                              ¥{((currentItem?.quantity || 0) * (currentItem?.unitPrice || 0)).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </TableCell>
                            <TableCell>
                              {fields.length > 1 && (
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon"
                                  onClick={() => remove(index)}
                                >
                                  <Minus className="h-4 w-4" />
                                </Button>
                              )}
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </div>

                <div className="flex justify-end">
                  <div className="text-right">
                    <span className="text-muted-foreground mr-2">订单总金额:</span>
                    <span className="text-xl font-bold text-primary">
                      ¥{formTotalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  </div>
                </div>
              </div>

              <FormField
                control={form.control}
                name="remark"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea placeholder="请输入备注" {...field} />
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
                  {editingOrder ? '保存' : '创建'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Order Detail Dialog */}
      <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
        <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>订单详情</DialogTitle>
          </DialogHeader>
          {isLoadingDetail ? (
            <div className="py-8 flex justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : orderDetail ? (
            <div className="space-y-6">
              {/* Basic Info */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <span className="text-muted-foreground text-sm">订单编号</span>
                  <p className="font-medium">{orderDetail.orderNo}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-sm">供应商</span>
                  <p className="font-medium">{orderDetail.supplierName}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-sm">状态</span>
                  <p>
                    <Badge variant={orderStatusConfig[orderDetail.status as OrderStatus]?.variant || 'secondary'}>
                      {orderStatusConfig[orderDetail.status as OrderStatus]?.label || orderDetail.status}
                    </Badge>
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground text-sm">总金额</span>
                  <p className="font-medium text-lg text-primary">
                    ¥{orderDetail.totalAmount?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground text-sm">创建人</span>
                  <p className="font-medium">{orderDetail.creator}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-sm">创建时间</span>
                  <p className="font-medium">
                    {orderDetail.createdAt ? new Date(orderDetail.createdAt).toLocaleString() : '-'}
                  </p>
                </div>
                {orderDetail.approver && (
                  <>
                    <div>
                      <span className="text-muted-foreground text-sm">审核人</span>
                      <p className="font-medium">{orderDetail.approver}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-sm">审核时间</span>
                      <p className="font-medium">
                        {orderDetail.approveTime ? new Date(orderDetail.approveTime).toLocaleString() : '-'}
                      </p>
                    </div>
                  </>
                )}
              </div>

              {orderDetail.remark && (
                <div>
                  <span className="text-muted-foreground text-sm">备注</span>
                  <p className="font-medium">{orderDetail.remark}</p>
                </div>
              )}

              <Separator />

              {/* Items */}
              <div>
                <h4 className="font-medium mb-3">商品明细</h4>
                <div className="rounded-md border">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>商品名称</TableHead>
                        <TableHead>编码</TableHead>
                        <TableHead>规格</TableHead>
                        <TableHead>单位</TableHead>
                        <TableHead className="text-right">数量</TableHead>
                        <TableHead className="text-right">单价</TableHead>
                        <TableHead className="text-right">小计</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {orderDetail.items?.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="font-medium">{item.productName}</TableCell>
                          <TableCell>{item.productCode}</TableCell>
                          <TableCell>{item.specification || '-'}</TableCell>
                          <TableCell>{item.unit}</TableCell>
                          <TableCell className="text-right">{item.quantity}</TableCell>
                          <TableCell className="text-right">
                            ¥{item.unitPrice?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </TableCell>
                          <TableCell className="text-right font-medium">
                            ¥{item.totalAmount?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>

              {/* Actions */}
              {orderDetail.status === 'PENDING' && (
                <div className="flex gap-2 justify-end">
                  <Button
                    variant="destructive"
                    onClick={() => {
                      setSelectedOrder(orderDetail.id);
                      setDetailDialogOpen(false);
                      setRejectDialogOpen(true);
                    }}
                  >
                    <X className="mr-2 h-4 w-4" />
                    拒绝
                  </Button>
                  <Button onClick={() => approveMutation.mutate(orderDetail.id)}>
                    <Check className="mr-2 h-4 w-4" />
                    审核通过
                  </Button>
                </div>
              )}
              {orderDetail.status === 'APPROVED' && (
                <div className="flex gap-2 justify-end">
                  <Button variant="outline" onClick={() => cancelMutation.mutate(orderDetail.id)}>
                    <Ban className="mr-2 h-4 w-4" />
                    取消订单
                  </Button>
                  <Button onClick={() => receiveMutation.mutate(orderDetail.id)}>
                    <Package className="mr-2 h-4 w-4" />
                    确认收货
                  </Button>
                </div>
              )}
            </div>
          ) : null}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              此操作将永久删除该采购订单，删除后无法恢复。确定要继续吗？
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

      {/* Reject Dialog */}
      <AlertDialog open={rejectDialogOpen} onOpenChange={setRejectDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>审核拒绝</AlertDialogTitle>
            <AlertDialogDescription>
              请输入拒绝原因
            </AlertDialogDescription>
          </AlertDialogHeader>
          <Textarea
            placeholder="请输入拒绝原因..."
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            className="min-h-[100px]"
          />
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setRejectReason('')}>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleReject}
              disabled={!rejectReason || rejectMutation.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {rejectMutation.isPending && (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              )}
              确认拒绝
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
