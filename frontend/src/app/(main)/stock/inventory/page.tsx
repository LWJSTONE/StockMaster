'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { inventoryApi } from '@/lib/api';
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
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Badge } from '@/components/ui/badge';
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { toast } from 'sonner';
import {
  Search,
  Package,
  AlertTriangle,
  TrendingUp,
  MoreHorizontal,
  Edit,
  Settings,
  RefreshCw,
} from 'lucide-react';
import type { Inventory } from '@/types';

// Form schemas
const adjustQuantitySchema = z.object({
  quantity: z.number().min(0, '库存数量不能为负数'),
});

const warningSchema = z.object({
  warningQuantity: z.number().min(0, '预警数量不能为负数'),
});

type AdjustQuantityForm = z.infer<typeof adjustQuantitySchema>;
type WarningForm = z.infer<typeof warningSchema>;

export default function InventoryPage() {
  const queryClient = useQueryClient();
  
  // State
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [warningStatus, setWarningStatus] = useState<string>('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [adjustDialogOpen, setAdjustDialogOpen] = useState(false);
  const [warningDialogOpen, setWarningDialogOpen] = useState(false);
  const [lowStockDialogOpen, setLowStockDialogOpen] = useState(false);
  const [overStockDialogOpen, setOverStockDialogOpen] = useState(false);
  const [currentItem, setCurrentItem] = useState<Inventory | null>(null);

  // Queries
  const { data: inventoryData, isLoading } = useQuery({
    queryKey: ['inventory', page, size, keyword, warningStatus],
    queryFn: () => inventoryApi.getList({ page, size, keyword, warningStatus: warningStatus || undefined }),
  });

  const { data: lowStockData } = useQuery({
    queryKey: ['lowStock'],
    queryFn: () => inventoryApi.getLowStock(),
    enabled: lowStockDialogOpen,
  });

  const { data: overStockData } = useQuery({
    queryKey: ['overStock'],
    queryFn: () => inventoryApi.getOverStock(),
    enabled: overStockDialogOpen,
  });

  // Mutations
  const updateQuantityMutation = useMutation({
    mutationFn: ({ id, quantity }: { id: number; quantity: number }) =>
      inventoryApi.updateQuantity(id, { quantity }),
    onSuccess: () => {
      toast.success('库存数量更新成功');
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      setAdjustDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '更新失败');
    },
  });

  const updateWarningMutation = useMutation({
    mutationFn: ({ id, warningQuantity }: { id: number; warningQuantity: number }) =>
      inventoryApi.updateWarning(id, { warningQuantity }),
    onSuccess: () => {
      toast.success('预警阈值设置成功');
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      setWarningDialogOpen(false);
    },
    onError: (error: Error) => {
      toast.error(error.message || '设置失败');
    },
  });

  // Forms
  const adjustForm = useForm<AdjustQuantityForm>({
    resolver: zodResolver(adjustQuantitySchema),
    defaultValues: { quantity: 0 },
  });

  const warningForm = useForm<WarningForm>({
    resolver: zodResolver(warningSchema),
    defaultValues: { warningQuantity: 0 },
  });

  // Handlers
  const handleSearch = () => {
    setPage(0);
    setSelectedIds([]);
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked && inventoryData?.content) {
      setSelectedIds(inventoryData.content.map((item) => item.id));
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

  const openAdjustDialog = (item: Inventory) => {
    setCurrentItem(item);
    adjustForm.reset({ quantity: item.quantity });
    setAdjustDialogOpen(true);
  };

  const openWarningDialog = (item: Inventory) => {
    setCurrentItem(item);
    warningForm.reset({ warningQuantity: item.warningQuantity });
    setWarningDialogOpen(true);
  };

  const handleAdjustQuantity = (data: AdjustQuantityForm) => {
    if (currentItem) {
      updateQuantityMutation.mutate({ id: currentItem.id, quantity: data.quantity });
    }
  };

  const handleSetWarning = (data: WarningForm) => {
    if (currentItem) {
      updateWarningMutation.mutate({ id: currentItem.id, warningQuantity: data.warningQuantity });
    }
  };

  const getWarningStatusBadge = (status: string) => {
    switch (status) {
      case 'low':
        return <Badge variant="destructive">低库存</Badge>;
      case 'over':
        return <Badge variant="secondary">超储</Badge>;
      default:
        return <Badge variant="default">正常</Badge>;
    }
  };

  const totalPages = inventoryData?.totalPages || 0;
  const currentPage = (inventoryData?.number || 0) + 1;

  return (
    <div className="space-y-6">
      {/* Quick Access Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={() => setLowStockDialogOpen(true)}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">低库存商品</CardTitle>
            <AlertTriangle className="h-4 w-4 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-destructive">点击查看</div>
            <p className="text-xs text-muted-foreground">库存低于预警值的商品</p>
          </CardContent>
        </Card>

        <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={() => setOverStockDialogOpen(true)}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">超储商品</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">点击查看</div>
            <p className="text-xs text-muted-foreground">库存超过正常水平的商品</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">库存商品总数</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{inventoryData?.totalElements || 0}</div>
            <p className="text-xs text-muted-foreground">当前库存商品数量</p>
          </CardContent>
        </Card>
      </div>

      {/* Search and Filter */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 flex gap-2">
              <Input
                placeholder="搜索商品名称、编码..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="max-w-sm"
              />
              <Button onClick={handleSearch}>
                <Search className="h-4 w-4 mr-2" />
                搜索
              </Button>
            </div>
            <div className="flex gap-2">
              <Select value={warningStatus} onValueChange={setWarningStatus}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="库存状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">全部状态</SelectItem>
                  <SelectItem value="normal">正常</SelectItem>
                  <SelectItem value="low">低库存</SelectItem>
                  <SelectItem value="over">超储</SelectItem>
                </SelectContent>
              </Select>
              <Button variant="outline" onClick={() => queryClient.invalidateQueries({ queryKey: ['inventory'] })}>
                <RefreshCw className="h-4 w-4 mr-2" />
                刷新
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
                      checked={selectedIds.length === inventoryData?.content?.length && inventoryData?.content?.length > 0}
                      onCheckedChange={handleSelectAll}
                    />
                  </TableHead>
                  <TableHead>商品名称</TableHead>
                  <TableHead>商品编码</TableHead>
                  <TableHead>分类</TableHead>
                  <TableHead>规格</TableHead>
                  <TableHead>单位</TableHead>
                  <TableHead className="text-right">当前库存</TableHead>
                  <TableHead className="text-right">预警数量</TableHead>
                  <TableHead>库存状态</TableHead>
                  <TableHead>更新时间</TableHead>
                  <TableHead className="w-20">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={11} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : inventoryData?.content?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={11} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  inventoryData?.content?.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>
                        <Checkbox
                          checked={selectedIds.includes(item.id)}
                          onCheckedChange={(checked) => handleSelectItem(item.id, !!checked)}
                        />
                      </TableCell>
                      <TableCell className="font-medium">{item.productName}</TableCell>
                      <TableCell>{item.productCode}</TableCell>
                      <TableCell>{item.categoryName}</TableCell>
                      <TableCell>{item.specification || '-'}</TableCell>
                      <TableCell>{item.unit}</TableCell>
                      <TableCell className="text-right font-medium">{item.quantity}</TableCell>
                      <TableCell className="text-right">{item.warningQuantity}</TableCell>
                      <TableCell>{getWarningStatusBadge(item.warningStatus)}</TableCell>
                      <TableCell>{new Date(item.updatedAt).toLocaleString()}</TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => openAdjustDialog(item)}>
                              <Edit className="h-4 w-4 mr-2" />
                              调整库存
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => openWarningDialog(item)}>
                              <Settings className="h-4 w-4 mr-2" />
                              设置预警
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
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

      {/* Adjust Quantity Dialog */}
      <Dialog open={adjustDialogOpen} onOpenChange={setAdjustDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>调整库存数量</DialogTitle>
            <DialogDescription>
              商品: {currentItem?.productName} ({currentItem?.productCode})
            </DialogDescription>
          </DialogHeader>
          <Form {...adjustForm}>
            <form onSubmit={adjustForm.handleSubmit(handleAdjustQuantity)} className="space-y-4">
              <FormField
                control={adjustForm.control}
                name="quantity"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>库存数量</FormLabel>
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
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setAdjustDialogOpen(false)}>
                  取消
                </Button>
                <Button type="submit" disabled={updateQuantityMutation.isPending}>
                  {updateQuantityMutation.isPending ? '保存中...' : '保存'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Warning Settings Dialog */}
      <Dialog open={warningDialogOpen} onOpenChange={setWarningDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>设置预警阈值</DialogTitle>
            <DialogDescription>
              商品: {currentItem?.productName} ({currentItem?.productCode})
            </DialogDescription>
          </DialogHeader>
          <Form {...warningForm}>
            <form onSubmit={warningForm.handleSubmit(handleSetWarning)} className="space-y-4">
              <FormField
                control={warningForm.control}
                name="warningQuantity"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>预警数量</FormLabel>
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
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setWarningDialogOpen(false)}>
                  取消
                </Button>
                <Button type="submit" disabled={updateWarningMutation.isPending}>
                  {updateWarningMutation.isPending ? '保存中...' : '保存'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Low Stock Dialog */}
      <Dialog open={lowStockDialogOpen} onOpenChange={setLowStockDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>低库存商品列表</DialogTitle>
            <DialogDescription>库存数量低于预警阈值的商品</DialogDescription>
          </DialogHeader>
          <ScrollArea className="h-[400px]">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>商品名称</TableHead>
                  <TableHead>商品编码</TableHead>
                  <TableHead className="text-right">当前库存</TableHead>
                  <TableHead className="text-right">预警数量</TableHead>
                  <TableHead>差额</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {lowStockData?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8 text-muted-foreground">
                      暂无低库存商品
                    </TableCell>
                  </TableRow>
                ) : (
                  lowStockData?.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.productName}</TableCell>
                      <TableCell>{item.productCode}</TableCell>
                      <TableCell className="text-right text-destructive font-bold">
                        {item.quantity}
                      </TableCell>
                      <TableCell className="text-right">{item.warningQuantity}</TableCell>
                      <TableCell className="text-destructive">
                        缺 {item.warningQuantity - item.quantity}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </ScrollArea>
        </DialogContent>
      </Dialog>

      {/* Over Stock Dialog */}
      <Dialog open={overStockDialogOpen} onOpenChange={setOverStockDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>超储商品列表</DialogTitle>
            <DialogDescription>库存数量超过正常水平的商品</DialogDescription>
          </DialogHeader>
          <ScrollArea className="h-[400px]">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>商品名称</TableHead>
                  <TableHead>商品编码</TableHead>
                  <TableHead className="text-right">当前库存</TableHead>
                  <TableHead className="text-right">预警数量</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {overStockData?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                      暂无超储商品
                    </TableCell>
                  </TableRow>
                ) : (
                  overStockData?.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.productName}</TableCell>
                      <TableCell>{item.productCode}</TableCell>
                      <TableCell className="text-right font-bold">{item.quantity}</TableCell>
                      <TableCell className="text-right">{item.warningQuantity}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </ScrollArea>
        </DialogContent>
      </Dialog>
    </div>
  );
}
