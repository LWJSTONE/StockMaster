'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { inboundApi, productApi, supplierApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
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
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { Checkbox } from '@/components/ui/checkbox';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import {
  Search,
  Plus,
  Pencil,
  Trash2,
  CalendarIcon,
  RefreshCw,
  Package,
} from 'lucide-react';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import type { Inbound, ProductSelect } from '@/types';

// Form schema
const inboundSchema = z.object({
  productId: z.number().min(1, '请选择商品'),
  quantity: z.number().min(1, '入库数量必须大于0'),
  unitPrice: z.number().min(0, '单价不能为负数'),
  supplierId: z.number().min(1, '请选择供应商'),
  remark: z.string().optional(),
});

type InboundForm = z.infer<typeof inboundSchema>;

export default function InboundPage() {
  const queryClient = useQueryClient();

  // State
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [supplierId, setSupplierId] = useState<string>('');
  const [startDate, setStartDate] = useState<Date | undefined>();
  const [endDate, setEndDate] = useState<Date | undefined>();
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [batchDeleteDialogOpen, setBatchDeleteDialogOpen] = useState(false);
  const [currentItem, setCurrentItem] = useState<Inbound | null>(null);
  const [productSearch, setProductSearch] = useState('');
  const [productSelectOpen, setProductSelectOpen] = useState(false);

  // Queries
  const { data: inboundData, isLoading } = useQuery({
    queryKey: ['inbound', page, size, keyword, supplierId, startDate, endDate],
    queryFn: () =>
      inboundApi.getList({
        page,
        size,
        keyword,
        supplierId: supplierId ? Number(supplierId) : undefined,
        startDate: startDate ? format(startDate, 'yyyy-MM-dd') : undefined,
        endDate: endDate ? format(endDate, 'yyyy-MM-dd') : undefined,
      }),
  });

  const { data: suppliers } = useQuery({
    queryKey: ['suppliers-all'],
    queryFn: () => supplierApi.getAll(),
  });

  const { data: products } = useQuery({
    queryKey: ['products-select', productSearch],
    queryFn: () => productApi.getSelect(),
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: InboundForm) => inboundApi.create(data),
    onSuccess: () => {
      toast.success('入库记录添加成功');
      queryClient.invalidateQueries({ queryKey: ['inbound'] });
      setDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '添加失败');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: InboundForm }) =>
      inboundApi.update(id, data),
    onSuccess: () => {
      toast.success('入库记录更新成功');
      queryClient.invalidateQueries({ queryKey: ['inbound'] });
      setDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '更新失败');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => inboundApi.delete(id),
    onSuccess: () => {
      toast.success('入库记录删除成功');
      queryClient.invalidateQueries({ queryKey: ['inbound'] });
      setDeleteDialogOpen(false);
      setSelectedIds([]);
    },
    onError: (error: Error) => {
      toast.error(error.message || '删除失败');
    },
  });

  const batchDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => inboundApi.batchDelete(ids),
    onSuccess: () => {
      toast.success('批量删除成功');
      queryClient.invalidateQueries({ queryKey: ['inbound'] });
      setBatchDeleteDialogOpen(false);
      setSelectedIds([]);
    },
    onError: (error: Error) => {
      toast.error(error.message || '批量删除失败');
    },
  });

  // Form
  const form = useForm<InboundForm>({
    resolver: zodResolver(inboundSchema),
    defaultValues: {
      productId: 0,
      quantity: 1,
      unitPrice: 0,
      supplierId: 0,
      remark: '',
    },
  });

  // Handlers
  const handleSearch = () => {
    setPage(0);
    setSelectedIds([]);
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked && inboundData?.content) {
      setSelectedIds(inboundData.content.map((item) => item.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectItem = (id: number, checked: boolean) => {
    if (checked) {
      setSelectedIds([...selectedIds, id]);
    } else {
      setSelectedIds(selectedIds.filter((i) => i !== id));
    }
  };

  const openCreateDialog = () => {
    setCurrentItem(null);
    form.reset({
      productId: 0,
      quantity: 1,
      unitPrice: 0,
      supplierId: 0,
      remark: '',
    });
    setDialogOpen(true);
  };

  const openEditDialog = (item: Inbound) => {
    setCurrentItem(item);
    form.reset({
      productId: item.productId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      supplierId: item.supplierId,
      remark: item.remark || '',
    });
    setDialogOpen(true);
  };

  const openDeleteDialog = (item: Inbound) => {
    setCurrentItem(item);
    setDeleteDialogOpen(true);
  };

  const handleSubmit = (data: InboundForm) => {
    if (currentItem) {
      updateMutation.mutate({ id: currentItem.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleDelete = () => {
    if (currentItem) {
      deleteMutation.mutate(currentItem.id);
    }
  };

  const handleBatchDelete = () => {
    if (selectedIds.length > 0) {
      batchDeleteMutation.mutate(selectedIds);
    }
  };

  const handleProductSelect = (product: ProductSelect) => {
    form.setValue('productId', product.id);
    form.setValue('unitPrice', product.salePrice);
    setProductSelectOpen(false);
  };

  const selectedProduct = products?.find((p) => p.id === form.watch('productId'));

  const totalPages = inboundData?.totalPages || 0;

  return (
    <div className="space-y-6">
      {/* Search and Filter */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 flex flex-wrap gap-2">
              <Input
                placeholder="搜索入库单号、商品..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="w-full md:w-64"
              />
              <Select value={supplierId} onValueChange={setSupplierId}>
                <SelectTrigger className="w-full md:w-48">
                  <SelectValue placeholder="选择供应商" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">全部供应商</SelectItem>
                  {suppliers?.map((supplier) => (
                    <SelectItem key={supplier.id} value={String(supplier.id)}>
                      {supplier.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full md:w-44">
                    <CalendarIcon className="h-4 w-4 mr-2" />
                    {startDate ? format(startDate, 'yyyy-MM-dd') : '开始日期'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={startDate}
                    onSelect={setStartDate}
                    locale={zhCN}
                  />
                </PopoverContent>
              </Popover>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full md:w-44">
                    <CalendarIcon className="h-4 w-4 mr-2" />
                    {endDate ? format(endDate, 'yyyy-MM-dd') : '结束日期'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={endDate}
                    onSelect={setEndDate}
                    locale={zhCN}
                  />
                </PopoverContent>
              </Popover>
              <Button onClick={handleSearch}>
                <Search className="h-4 w-4 mr-2" />
                搜索
              </Button>
            </div>
            <div className="flex gap-2">
              <Button onClick={openCreateDialog}>
                <Plus className="h-4 w-4 mr-2" />
                新增入库
              </Button>
              {selectedIds.length > 0 && (
                <Button
                  variant="destructive"
                  onClick={() => setBatchDeleteDialogOpen(true)}
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  批量删除 ({selectedIds.length})
                </Button>
              )}
              <Button
                variant="outline"
                onClick={() => queryClient.invalidateQueries({ queryKey: ['inbound'] })}
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Data Table */}
      <Card>
        <CardContent className="p-0">
          <ScrollArea className="h-[500px]">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12">
                    <Checkbox
                      checked={
                        selectedIds.length === inboundData?.content?.length &&
                        inboundData?.content?.length > 0
                      }
                      onCheckedChange={handleSelectAll}
                    />
                  </TableHead>
                  <TableHead>入库单号</TableHead>
                  <TableHead>商品名称</TableHead>
                  <TableHead>商品编码</TableHead>
                  <TableHead className="text-right">入库数量</TableHead>
                  <TableHead className="text-right">单价</TableHead>
                  <TableHead className="text-right">总金额</TableHead>
                  <TableHead>供应商</TableHead>
                  <TableHead>操作员</TableHead>
                  <TableHead>入库时间</TableHead>
                  <TableHead className="w-24">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={11} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : inboundData?.content?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={11} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  inboundData?.content?.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>
                        <Checkbox
                          checked={selectedIds.includes(item.id)}
                          onCheckedChange={(checked) =>
                            handleSelectItem(item.id, !!checked)
                          }
                        />
                      </TableCell>
                      <TableCell className="font-mono">{item.inboundNo}</TableCell>
                      <TableCell className="font-medium">{item.productName}</TableCell>
                      <TableCell>{item.productCode}</TableCell>
                      <TableCell className="text-right">{item.quantity}</TableCell>
                      <TableCell className="text-right">
                        ¥{item.unitPrice.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        ¥{item.totalAmount.toFixed(2)}
                      </TableCell>
                      <TableCell>{item.supplierName}</TableCell>
                      <TableCell>{item.operator}</TableCell>
                      <TableCell>
                        {new Date(item.createdAt).toLocaleString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => openEditDialog(item)}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => openDeleteDialog(item)}
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </ScrollArea>
        </CardContent>
      </Card>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setPage(Math.max(0, page - 1))}
                  className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const pageNum = page < 3 ? i : page - 2 + i;
                if (pageNum >= totalPages) return null;
                return (
                  <PaginationItem key={pageNum}>
                    <PaginationLink
                      onClick={() => setPage(pageNum)}
                      isActive={pageNum === page}
                      className="cursor-pointer"
                    >
                      {pageNum + 1}
                    </PaginationLink>
                  </PaginationItem>
                );
              })}
              <PaginationItem>
                <PaginationNext
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  className={page >= totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{currentItem ? '编辑入库记录' : '新增入库'}</DialogTitle>
            <DialogDescription>
              {currentItem ? '修改入库记录信息' : '录入新的入库记录'}
            </DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="productId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>商品</FormLabel>
                    <FormControl>
                      <Popover open={productSelectOpen} onOpenChange={setProductSelectOpen}>
                        <PopoverTrigger asChild>
                          <Button
                            variant="outline"
                            role="combobox"
                            className="w-full justify-between"
                          >
                            {selectedProduct ? (
                              <span>
                                {selectedProduct.name} ({selectedProduct.code})
                              </span>
                            ) : (
                              <span className="text-muted-foreground">选择商品</span>
                            )}
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-[400px] p-0" align="start">
                          <div className="p-2 border-b">
                            <Input
                              placeholder="搜索商品..."
                              value={productSearch}
                              onChange={(e) => setProductSearch(e.target.value)}
                            />
                          </div>
                          <ScrollArea className="h-[300px]">
                            {products
                              ?.filter((p) =>
                                productSearch
                                  ? p.name.includes(productSearch) ||
                                    p.code.includes(productSearch)
                                  : true
                              )
                              .map((product) => (
                                <div
                                  key={product.id}
                                  className="flex items-center justify-between p-2 hover:bg-muted cursor-pointer"
                                  onClick={() => handleProductSelect(product)}
                                >
                                  <div>
                                    <div className="font-medium">{product.name}</div>
                                    <div className="text-sm text-muted-foreground">
                                      {product.code} | {product.specification || '-'} |{' '}
                                      {product.unit}
                                    </div>
                                  </div>
                                  <div className="text-sm">¥{product.salePrice}</div>
                                </div>
                              ))}
                          </ScrollArea>
                        </PopoverContent>
                      </Popover>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="quantity"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>入库数量</FormLabel>
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
                <FormField
                  control={form.control}
                  name="unitPrice"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>单价</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          {...field}
                          onChange={(e) => field.onChange(Number(e.target.value))}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name="supplierId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>供应商</FormLabel>
                    <Select
                      value={field.value ? String(field.value) : ''}
                      onValueChange={(value) => field.onChange(Number(value))}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="选择供应商" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {suppliers?.map((supplier) => (
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
              <FormField
                control={form.control}
                name="remark"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea placeholder="输入备注信息..." {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setDialogOpen(false)}
                >
                  取消
                </Button>
                <Button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                >
                  {createMutation.isPending || updateMutation.isPending
                    ? '保存中...'
                    : '保存'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              确定要删除入库单号 &quot;{currentItem?.inboundNo}&quot; 的记录吗？此操作无法撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleteMutation.isPending ? '删除中...' : '删除'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Batch Delete Confirmation Dialog */}
      <AlertDialog open={batchDeleteDialogOpen} onOpenChange={setBatchDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认批量删除</AlertDialogTitle>
            <AlertDialogDescription>
              确定要删除选中的 {selectedIds.length} 条入库记录吗？此操作无法撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleBatchDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {batchDeleteMutation.isPending ? '删除中...' : '删除'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
