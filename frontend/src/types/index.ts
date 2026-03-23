// API Response Types
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Auth Types
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  status: number;
  roles: Role[];
  permissions: string[];
}

// User Types
export interface User {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  status: number;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface UserFormData {
  username: string;
  nickname: string;
  email: string;
  phone: string;
  password?: string;
  status: number;
  roleIds: number[];
}

// Role Types
export interface Role {
  id: number;
  name: string;
  code: string;
  description: string;
  status: number;
  menus?: Menu[];
  createdAt: string;
  updatedAt: string;
}

export interface RoleFormData {
  name: string;
  code: string;
  description: string;
  status: number;
  menuIds: number[];
}

// Menu Types
export interface Menu {
  id: number;
  parentId: number | null;
  name: string;
  path: string;
  icon: string;
  sort: number;
  type: 'directory' | 'menu' | 'button';
  permission: string;
  visible: number;
  status: number;
  children?: Menu[];
  createdAt: string;
  updatedAt: string;
}

export interface MenuFormData {
  parentId: number | null;
  name: string;
  path: string;
  icon: string;
  sort: number;
  type: 'directory' | 'menu' | 'button';
  permission: string;
  visible: number;
  status: number;
}

// Category Types
export interface Category {
  id: number;
  parentId: number | null;
  name: string;
  code: string;
  sort: number;
  status: number;
  children?: Category[];
  createdAt: string;
  updatedAt: string;
}

export interface CategoryFormData {
  parentId: number | null;
  name: string;
  code: string;
  sort: number;
  status: number;
}

// Product Types
export interface Product {
  id: number;
  name: string;
  code: string;
  barcode: string;
  categoryId: number;
  categoryName: string;
  specification: string;
  unit: string;
  purchasePrice: number;
  salePrice: number;
  status: number;
  image: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductFormData {
  name: string;
  code: string;
  barcode: string;
  categoryId: number;
  specification: string;
  unit: string;
  purchasePrice: number;
  salePrice: number;
  status: number;
  description: string;
}

export interface ProductSelect {
  id: number;
  name: string;
  code: string;
  barcode: string;
  specification: string;
  unit: string;
  salePrice: number;
}

// Inventory Types
export interface Inventory {
  id: number;
  productId: number;
  productName: string;
  productCode: string;
  categoryName: string;
  quantity: number;
  warningQuantity: number;
  warningStatus: 'normal' | 'low' | 'over';
  specification: string;
  unit: string;
  updatedAt: string;
}

export interface InventoryFormData {
  productId: number;
  quantity: number;
  warningQuantity: number;
}

// Inbound Types
export interface Inbound {
  id: number;
  inboundNo: string;
  productId: number;
  productName: string;
  productCode: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  supplierId: number;
  supplierName: string;
  operator: string;
  remark: string;
  createdAt: string;
}

export interface InboundFormData {
  productId: number;
  quantity: number;
  unitPrice: number;
  supplierId: number;
  remark: string;
}

// Outbound Types
export interface Outbound {
  id: number;
  outboundNo: string;
  productId: number;
  productName: string;
  productCode: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  operator: string;
  remark: string;
  createdAt: string;
}

export interface OutboundFormData {
  productId: number;
  quantity: number;
  unitPrice: number;
  remark: string;
}

// Supplier Types
export interface Supplier {
  id: number;
  name: string;
  code: string;
  contact: string;
  phone: string;
  email: string;
  address: string;
  status: number;
  remark: string;
  createdAt: string;
  updatedAt: string;
}

export interface SupplierFormData {
  name: string;
  code: string;
  contact: string;
  phone: string;
  email: string;
  address: string;
  status: number;
  remark: string;
}

// Supplier Evaluation Types
export interface SupplierEvaluation {
  id: number;
  supplierId: number;
  supplierName: string;
  score: number;
  content: string;
  evaluator: string;
  createdAt: string;
}

export interface SupplierEvaluationFormData {
  supplierId: number;
  score: number;
  content: string;
}

// Purchase Order Types
export type OrderStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface PurchaseOrderItem {
  id: number;
  productId: number;
  productName: string;
  productCode: string;
  specification: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
}

export interface PurchaseOrder {
  id: number;
  orderNo: string;
  supplierId: number;
  supplierName: string;
  totalAmount: number;
  status: OrderStatus;
  remark: string;
  items: PurchaseOrderItem[];
  creator: string;
  approver: string;
  approveTime: string;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderFormData {
  supplierId: number;
  remark: string;
  items: {
    productId: number;
    quantity: number;
    unitPrice: number;
  }[];
}

// Dashboard Types
export interface DashboardStats {
  productCount: number;
  supplierCount: number;
  orderCount: number;
  warningCount: number;
  todayInbound: number;
  todayOutbound: number;
  totalInventoryValue: number;
}

export interface TrendData {
  date: string;
  inbound: number;
  outbound: number;
  purchase: number;
}

export interface CategoryDistribution {
  name: string;
  value: number;
  count: number;
}

export interface PurchaseVsStock {
  name: string;
  purchase: number;
  stock: number;
}

// Log Types
export interface SysLog {
  id: number;
  operator: string;
  operation: string;
  method: string;
  params: string;
  ip: string;
  createTime: string;
  time: number;
}

// Query Params Types
export interface PageParams {
  page?: number;
  size?: number;
}

export interface UserQueryParams extends PageParams {
  keyword?: string;
  status?: number;
}

export interface ProductQueryParams extends PageParams {
  keyword?: string;
  categoryId?: number;
  status?: number;
}

export interface InventoryQueryParams extends PageParams {
  keyword?: string;
  warningStatus?: string;
}

export interface InboundQueryParams extends PageParams {
  keyword?: string;
  supplierId?: number;
  startDate?: string;
  endDate?: string;
}

export interface OutboundQueryParams extends PageParams {
  keyword?: string;
  startDate?: string;
  endDate?: string;
}

export interface SupplierQueryParams extends PageParams {
  keyword?: string;
  status?: number;
}

export interface PurchaseOrderQueryParams extends PageParams {
  keyword?: string;
  supplierId?: number;
  status?: OrderStatus;
  startDate?: string;
  endDate?: string;
}

export interface EvaluationQueryParams extends PageParams {
  supplierId?: number;
}

export interface LogQueryParams extends PageParams {
  keyword?: string;
  operator?: string;
  operation?: string;
  startDate?: string;
  endDate?: string;
}

export interface RoleQueryParams extends PageParams {
  keyword?: string;
  status?: number;
}

export interface MenuQueryParams {
  status?: number;
}

// Sidebar Types
export interface SidebarMenuItem {
  id: number;
  name: string;
  path: string;
  icon: string;
  children?: SidebarMenuItem[];
}
