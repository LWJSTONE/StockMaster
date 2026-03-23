'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { logApi } from '@/lib/api';
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
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { useToast } from '@/hooks/use-toast';
import {
  Search,
  Eye,
  Trash2,
  CalendarIcon,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import type { SysLog } from '@/types';

// Operation types
const OPERATION_TYPES = [
  { value: 'LOGIN', label: '登录', color: 'bg-green-50 text-green-700 border-green-200' },
  { value: 'LOGOUT', label: '登出', color: 'bg-gray-50 text-gray-700 border-gray-200' },
  { value: 'CREATE', label: '新增', color: 'bg-blue-50 text-blue-700 border-blue-200' },
  { value: 'UPDATE', label: '修改', color: 'bg-yellow-50 text-yellow-700 border-yellow-200' },
  { value: 'DELETE', label: '删除', color: 'bg-red-50 text-red-700 border-red-200' },
  { value: 'QUERY', label: '查询', color: 'bg-purple-50 text-purple-700 border-purple-200' },
  { value: 'APPROVE', label: '审批', color: 'bg-teal-50 text-teal-700 border-teal-200' },
  { value: 'REJECT', label: '拒绝', color: 'bg-orange-50 text-orange-700 border-orange-200' },
];

// Method colors
const METHOD_COLORS: Record<string, string> = {
  GET: 'bg-green-100 text-green-700',
  POST: 'bg-blue-100 text-blue-700',
  PUT: 'bg-yellow-100 text-yellow-700',
  DELETE: 'bg-red-100 text-red-700',
  PATCH: 'bg-purple-100 text-purple-700',
};

export default function LogManagementPage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  // State
  const [searchKeyword, setSearchKeyword] = useState('');
  const [operatorFilter, setOperatorFilter] = useState('');
  const [operationFilter, setOperationFilter] = useState<string>('all');
  const [startDate, setStartDate] = useState<Date | undefined>();
  const [endDate, setEndDate] = useState<Date | undefined>();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);

  // Dialog states
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false);
  const [isClearDialogOpen, setIsClearDialogOpen] = useState(false);
  const [currentLog, setCurrentLog] = useState<SysLog | null>(null);

  // Queries
  const { data: logsData, isLoading } = useQuery({
    queryKey: ['logs', page, pageSize, searchKeyword, operatorFilter, operationFilter, startDate, endDate],
    queryFn: () =>
      logApi.getList({
        page: page,
        size: pageSize,
        keyword: searchKeyword || undefined,
        operator: operatorFilter || undefined,
        operation: operationFilter === 'all' ? undefined : operationFilter,
        startDate: startDate ? format(startDate, 'yyyy-MM-dd') : undefined,
        endDate: endDate ? format(endDate, 'yyyy-MM-dd') : undefined,
      }),
  });

  // Mutations
  const clearMutation = useMutation({
    mutationFn: () => logApi.clear(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['logs'] });
      setIsClearDialogOpen(false);
      toast({ title: '清空成功', description: '操作日志已清空' });
    },
    onError: (error: Error) => {
      toast({ title: '清空失败', description: error.message, variant: 'destructive' });
    },
  });

  // Handlers
  const handleViewDetail = (log: SysLog) => {
    setCurrentLog(log);
    setIsDetailDialogOpen(true);
  };

  const handleClearLogs = () => {
    setIsClearDialogOpen(true);
  };

  const handleDateSelect = (date: Date | undefined, type: 'start' | 'end') => {
    if (type === 'start') {
      setStartDate(date);
    } else {
      setEndDate(date);
    }
  };

  const getOperationBadge = (operation: string) => {
    const op = OPERATION_TYPES.find((o) => o.value === operation);
    if (op) {
      return (
        <Badge variant="outline" className={op.color}>
          {op.label}
        </Badge>
      );
    }
    return (
      <Badge variant="outline">
        {operation}
      </Badge>
    );
  };

  const getMethodBadge = (method: string) => {
    const color = METHOD_COLORS[method] || 'bg-gray-100 text-gray-700';
    return (
      <Badge variant="outline" className={`font-mono text-xs ${color}`}>
        {method}
      </Badge>
    );
  };

  const formatDuration = (time: number) => {
    if (time < 1000) {
      return `${time}ms`;
    }
    return `${(time / 1000).toFixed(2)}s`;
  };

  const logs = logsData?.content || [];
  const totalElements = logsData?.totalElements || 0;
  const totalPages = logsData?.totalPages || 0;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-4">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <CardTitle className="text-xl font-semibold">操作日志</CardTitle>
            <div className="flex flex-col sm:flex-row gap-2">
              <div className="flex gap-2 flex-wrap">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="搜索..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="pl-9 w-[150px]"
                  />
                </div>
                <Input
                  placeholder="操作人"
                  value={operatorFilter}
                  onChange={(e) => setOperatorFilter(e.target.value)}
                  className="w-[120px]"
                />
                <Select value={operationFilter} onValueChange={setOperationFilter}>
                  <SelectTrigger className="w-[120px]">
                    <SelectValue placeholder="操作类型" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部类型</SelectItem>
                    {OPERATION_TYPES.map((op) => (
                      <SelectItem key={op.value} value={op.value}>
                        {op.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" size="sm" className="w-[140px] justify-start">
                      <CalendarIcon className="h-4 w-4 mr-2" />
                      {startDate ? format(startDate, 'yyyy-MM-dd') : '开始日期'}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={startDate}
                      onSelect={(date) => handleDateSelect(date, 'start')}
                      locale={zhCN}
                    />
                  </PopoverContent>
                </Popover>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" size="sm" className="w-[140px] justify-start">
                      <CalendarIcon className="h-4 w-4 mr-2" />
                      {endDate ? format(endDate, 'yyyy-MM-dd') : '结束日期'}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={endDate}
                      onSelect={(date) => handleDateSelect(date, 'end')}
                      locale={zhCN}
                    />
                  </PopoverContent>
                </Popover>
              </div>
              <Button variant="destructive" size="sm" onClick={handleClearLogs}>
                <Trash2 className="h-4 w-4 mr-1" />
                清空日志
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
                  <TableHead>操作人</TableHead>
                  <TableHead className="w-24">操作类型</TableHead>
                  <TableHead className="w-24">请求方法</TableHead>
                  <TableHead>IP地址</TableHead>
                  <TableHead>操作时间</TableHead>
                  <TableHead className="w-20">耗时</TableHead>
                  <TableHead className="w-20">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : logs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  logs.map((log) => (
                    <TableRow key={log.id}>
                      <TableCell>{log.id}</TableCell>
                      <TableCell className="font-medium">{log.operator}</TableCell>
                      <TableCell>{getOperationBadge(log.operation)}</TableCell>
                      <TableCell>{getMethodBadge(log.method)}</TableCell>
                      <TableCell>
                        <code className="bg-muted px-2 py-0.5 rounded text-xs">
                          {log.ip}
                        </code>
                      </TableCell>
                      <TableCell>
                        {log.createTime ? new Date(log.createTime).toLocaleString('zh-CN') : '-'}
                      </TableCell>
                      <TableCell>
                        <span className={log.time > 1000 ? 'text-red-600' : ''}>
                          {formatDuration(log.time)}
                        </span>
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8"
                          onClick={() => handleViewDetail(log)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
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
              共 {totalElements} 条记录，第 {page + 1} / {totalPages || 1} 页
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
              >
                <ChevronLeft className="h-4 w-4 mr-1" />
                上一页
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
              >
                下一页
                <ChevronRight className="h-4 w-4 ml-1" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Detail Dialog */}
      <Dialog open={isDetailDialogOpen} onOpenChange={setIsDetailDialogOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>操作日志详情</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">操作人</label>
                <p className="mt-1">{currentLog?.operator}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">操作类型</label>
                <p className="mt-1">{currentLog && getOperationBadge(currentLog.operation)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">请求方法</label>
                <p className="mt-1">{currentLog && getMethodBadge(currentLog.method)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">IP地址</label>
                <p className="mt-1">
                  <code className="bg-muted px-2 py-0.5 rounded text-sm">
                    {currentLog?.ip}
                  </code>
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">操作时间</label>
                <p className="mt-1">
                  {currentLog?.createTime && new Date(currentLog.createTime).toLocaleString('zh-CN')}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">耗时</label>
                <p className={`mt-1 ${currentLog && currentLog.time > 1000 ? 'text-red-600' : ''}`}>
                  {currentLog && formatDuration(currentLog.time)}
                </p>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-muted-foreground">请求参数</label>
              <ScrollArea className="mt-1 h-32 border rounded-md">
                <pre className="p-3 text-xs bg-muted/50 overflow-x-auto">
                  {currentLog?.params ? (
                    typeof currentLog.params === 'string'
                      ? currentLog.params
                      : JSON.stringify(currentLog.params, null, 2)
                  ) : (
                    <span className="text-muted-foreground">无</span>
                  )}
                </pre>
              </ScrollArea>
            </div>

            <div>
              <label className="text-sm font-medium text-muted-foreground">操作描述</label>
              <div className="mt-1 p-3 border rounded-md bg-muted/50">
                <p className="text-sm">
                  用户 <span className="font-medium">{currentLog?.operator}</span> 于{' '}
                  {currentLog?.createTime && new Date(currentLog.createTime).toLocaleString('zh-CN')}{' '}
                  执行了 <span className="font-medium">{currentLog?.operation}</span> 操作，
                  IP地址：<span className="font-mono">{currentLog?.ip}</span>，
                  耗时：<span className={currentLog && currentLog.time > 1000 ? 'text-red-600 font-medium' : ''}>
                    {currentLog && formatDuration(currentLog.time)}
                  </span>
                </p>
              </div>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Clear Logs Dialog */}
      <AlertDialog open={isClearDialogOpen} onOpenChange={setIsClearDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认清空日志</AlertDialogTitle>
            <AlertDialogDescription>
              确定要清空所有操作日志吗？此操作不可恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-red-600 hover:bg-red-700"
              onClick={() => clearMutation.mutate()}
              disabled={clearMutation.isPending}
            >
              {clearMutation.isPending ? '清空中...' : '确认清空'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
