'use client';

import { useQuery } from '@tanstack/react-query';
import {
  Package,
  Users,
  ShoppingCart,
  AlertTriangle,
  ArrowDownToLine,
  ArrowUpFromLine,
  DollarSign,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';
import { dashboardApi, inventoryApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
  type ChartConfig,
} from '@/components/ui/chart';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
} from 'recharts';
import { Skeleton } from '@/components/ui/skeleton';

// Chart colors
const COLORS = [
  'hsl(var(--chart-1))',
  'hsl(var(--chart-2))',
  'hsl(var(--chart-3))',
  'hsl(var(--chart-4))',
  'hsl(var(--chart-5))',
];

// Stats card component
interface StatsCardProps {
  title: string;
  value: string | number;
  icon: React.ElementType;
  trend?: number;
  trendLabel?: string;
  color?: string;
}

function StatsCard({
  title,
  value,
  icon: Icon,
  trend,
  trendLabel,
  color = 'text-primary',
}: StatsCardProps) {
  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <div className={`p-2 rounded-lg bg-muted ${color}`}>
          <Icon className="h-4 w-4" />
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {trend !== undefined && (
          <div className="flex items-center gap-1 mt-1">
            {trend >= 0 ? (
              <TrendingUp className="h-4 w-4 text-green-500" />
            ) : (
              <TrendingDown className="h-4 w-4 text-red-500" />
            )}
            <span
              className={`text-xs ${trend >= 0 ? 'text-green-500' : 'text-red-500'}`}
            >
              {trend >= 0 ? '+' : ''}
              {trend}%
            </span>
            {trendLabel && (
              <span className="text-xs text-muted-foreground">{trendLabel}</span>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// Skeleton for loading state
function StatsSkeleton() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <Skeleton className="h-4 w-20" />
        <Skeleton className="h-8 w-8 rounded-lg" />
      </CardHeader>
      <CardContent>
        <Skeleton className="h-8 w-24 mb-2" />
        <Skeleton className="h-4 w-16" />
      </CardContent>
    </Card>
  );
}

// Chart config for trend chart
const trendChartConfig = {
  inbound: {
    label: '入库',
    color: 'hsl(var(--chart-1))',
  },
  outbound: {
    label: '出库',
    color: 'hsl(var(--chart-2))',
  },
  purchase: {
    label: '采购',
    color: 'hsl(var(--chart-3))',
  },
} satisfies ChartConfig;

// Chart config for bar chart
const barChartConfig = {
  purchase: {
    label: '采购量',
    color: 'hsl(var(--chart-1))',
  },
  stock: {
    label: '库存量',
    color: 'hsl(var(--chart-2))',
  },
} satisfies ChartConfig;

// Custom label for pie chart
interface PieLabelProps {
  cx: number;
  cy: number;
  midAngle: number;
  innerRadius: number;
  outerRadius: number;
  percent: number;
  name: string;
}

const RADIAN = Math.PI / 180;
const renderCustomizedLabel = ({
  cx,
  cy,
  midAngle,
  innerRadius,
  outerRadius,
  percent,
}: PieLabelProps) => {
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (
    <text
      x={x}
      y={y}
      fill="white"
      textAnchor={x > cx ? 'start' : 'end'}
      dominantBaseline="central"
      className="text-xs font-medium"
    >
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  );
};

// Warning status badge
function WarningBadge({ status }: { status: string }) {
  if (status === 'low') {
    return (
      <Badge variant="destructive" className="text-xs">
        低库存
      </Badge>
    );
  }
  if (status === 'over') {
    return (
      <Badge variant="secondary" className="text-xs bg-orange-500 text-white">
        超储
      </Badge>
    );
  }
  return (
    <Badge variant="outline" className="text-xs">
      正常
    </Badge>
  );
}

// Format number with commas
function formatNumber(num: number): string {
  return num.toLocaleString('zh-CN');
}

// Format currency
function formatCurrency(num: number): string {
  return `¥${formatNumber(num.toFixed(2))}`;
}

export default function DashboardPage() {
  // Fetch stats
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => dashboardApi.getStats(),
  });

  // Fetch trend data
  const { data: trendData, isLoading: trendLoading } = useQuery({
    queryKey: ['dashboard-trend'],
    queryFn: () => dashboardApi.getTrend(7),
  });

  // Fetch category distribution
  const { data: categoryData, isLoading: categoryLoading } = useQuery({
    queryKey: ['dashboard-category'],
    queryFn: () => dashboardApi.getCategoryDistribution(),
  });

  // Fetch purchase vs stock
  const { data: comparisonData, isLoading: comparisonLoading } = useQuery({
    queryKey: ['dashboard-comparison'],
    queryFn: () => dashboardApi.getPurchaseVsStock(),
  });

  // Fetch low stock items
  const { data: lowStockData, isLoading: lowStockLoading } = useQuery({
    queryKey: ['inventory-low-stock'],
    queryFn: () => inventoryApi.getLowStock(),
  });

  // Fetch over stock items
  const { data: overStockData, isLoading: overStockLoading } = useQuery({
    queryKey: ['inventory-over-stock'],
    queryFn: () => inventoryApi.getOverStock(),
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold">仪表盘</h1>
        <p className="text-muted-foreground">欢迎回来，查看您的库存概览</p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-7">
        {statsLoading ? (
          <>
            <StatsSkeleton />
            <StatsSkeleton />
            <StatsSkeleton />
            <StatsSkeleton />
            <StatsSkeleton />
            <StatsSkeleton />
            <StatsSkeleton />
          </>
        ) : (
          <>
            <StatsCard
              title="商品总数"
              value={formatNumber(stats?.productCount || 0)}
              icon={Package}
              color="text-blue-500"
              trend={12}
              trendLabel="较上月"
            />
            <StatsCard
              title="供应商数量"
              value={formatNumber(stats?.supplierCount || 0)}
              icon={Users}
              color="text-green-500"
              trend={5}
              trendLabel="较上月"
            />
            <StatsCard
              title="采购订单数"
              value={formatNumber(stats?.orderCount || 0)}
              icon={ShoppingCart}
              color="text-purple-500"
              trend={-3}
              trendLabel="较上月"
            />
            <StatsCard
              title="库存预警数"
              value={formatNumber(stats?.warningCount || 0)}
              icon={AlertTriangle}
              color="text-orange-500"
            />
            <StatsCard
              title="今日入库量"
              value={formatNumber(stats?.todayInbound || 0)}
              icon={ArrowDownToLine}
              color="text-teal-500"
            />
            <StatsCard
              title="今日出库量"
              value={formatNumber(stats?.todayOutbound || 0)}
              icon={ArrowUpFromLine}
              color="text-rose-500"
            />
            <StatsCard
              title="库存总值"
              value={formatCurrency(stats?.totalInventoryValue || 0)}
              icon={DollarSign}
              color="text-amber-500"
            />
          </>
        )}
      </div>

      {/* Charts Row */}
      <div className="grid gap-4 grid-cols-1 lg:grid-cols-2">
        {/* Trend Chart */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">采购趋势图（最近7天）</CardTitle>
          </CardHeader>
          <CardContent>
            {trendLoading ? (
              <div className="h-[300px] flex items-center justify-center">
                <Skeleton className="h-[250px] w-full" />
              </div>
            ) : (
              <ChartContainer config={trendChartConfig} className="h-[300px]">
                <LineChart
                  data={trendData || []}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => value.slice(5)}
                    className="text-xs"
                  />
                  <YAxis className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <ChartLegend content={<ChartLegendContent />} />
                  <Line
                    type="monotone"
                    dataKey="inbound"
                    stroke="var(--color-inbound)"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="outbound"
                    stroke="var(--color-outbound)"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="purchase"
                    stroke="var(--color-purchase)"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ChartContainer>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Second Charts Row */}
      <div className="grid gap-4 grid-cols-1 lg:grid-cols-2">
        {/* Category Distribution Pie Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">商品分类分布</CardTitle>
          </CardHeader>
          <CardContent>
            {categoryLoading ? (
              <div className="h-[300px] flex items-center justify-center">
                <Skeleton className="h-[250px] w-[250px] rounded-full" />
              </div>
            ) : categoryData && categoryData.length > 0 ? (
              <div className="h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={categoryData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={renderCustomizedLabel}
                      outerRadius={100}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {categoryData.map((_, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={COLORS[index % COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <ChartTooltip
                      content={({ active, payload }) => {
                        if (active && payload && payload.length) {
                          const data = payload[0].payload;
                          return (
                            <div className="bg-background border rounded-lg px-3 py-2 shadow-md">
                              <p className="font-medium">{data.name}</p>
                              <p className="text-sm text-muted-foreground">
                                数量: {data.count}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                占比: {data.value.toFixed(1)}%
                              </p>
                            </div>
                          );
                        }
                        return null;
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
                {/* Legend */}
                <div className="flex flex-wrap justify-center gap-3 mt-4">
                  {categoryData.map((item, index) => (
                    <div key={item.name} className="flex items-center gap-1.5">
                      <div
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: COLORS[index % COLORS.length] }}
                      />
                      <span className="text-sm text-muted-foreground">
                        {item.name}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                暂无数据
              </div>
            )}
          </CardContent>
        </Card>

        {/* Purchase vs Stock Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">采购与库存对比</CardTitle>
          </CardHeader>
          <CardContent>
            {comparisonLoading ? (
              <div className="h-[300px] flex items-center justify-center">
                <Skeleton className="h-[250px] w-full" />
              </div>
            ) : comparisonData && comparisonData.length > 0 ? (
              <ChartContainer config={barChartConfig} className="h-[300px]">
                <BarChart
                  data={comparisonData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="name" className="text-xs" />
                  <YAxis className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <ChartLegend content={<ChartLegendContent />} />
                  <Bar
                    dataKey="purchase"
                    fill="var(--color-purchase)"
                    radius={[4, 4, 0, 0]}
                  />
                  <Bar
                    dataKey="stock"
                    fill="var(--color-stock)"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ChartContainer>
            ) : (
              <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                暂无数据
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Warning Lists */}
      <div className="grid gap-4 grid-cols-1 lg:grid-cols-2">
        {/* Low Stock Warning */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500" />
              低库存预警
            </CardTitle>
          </CardHeader>
          <CardContent>
            {lowStockLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : lowStockData && lowStockData.length > 0 ? (
              <div className="max-h-[300px] overflow-y-auto custom-scrollbar">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>商品名称</TableHead>
                      <TableHead>商品编码</TableHead>
                      <TableHead className="text-right">当前库存</TableHead>
                      <TableHead className="text-right">预警值</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {lowStockData.slice(0, 10).map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">
                          {item.productName}
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {item.productCode}
                        </TableCell>
                        <TableCell className="text-right">
                          <span className="text-red-500 font-medium">
                            {formatNumber(item.quantity)}
                          </span>
                        </TableCell>
                        <TableCell className="text-right text-muted-foreground">
                          {formatNumber(item.warningQuantity)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            ) : (
              <div className="h-[150px] flex items-center justify-center text-muted-foreground">
                暂无低库存商品
              </div>
            )}
          </CardContent>
        </Card>

        {/* Over Stock Warning */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-orange-500" />
              超储预警
            </CardTitle>
          </CardHeader>
          <CardContent>
            {overStockLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : overStockData && overStockData.length > 0 ? (
              <div className="max-h-[300px] overflow-y-auto custom-scrollbar">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>商品名称</TableHead>
                      <TableHead>商品编码</TableHead>
                      <TableHead className="text-right">当前库存</TableHead>
                      <TableHead className="text-right">预警值</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {overStockData.slice(0, 10).map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">
                          {item.productName}
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {item.productCode}
                        </TableCell>
                        <TableCell className="text-right">
                          <span className="text-orange-500 font-medium">
                            {formatNumber(item.quantity)}
                          </span>
                        </TableCell>
                        <TableCell className="text-right text-muted-foreground">
                          {formatNumber(item.warningQuantity)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            ) : (
              <div className="h-[150px] flex items-center justify-center text-muted-foreground">
                暂无超储商品
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
