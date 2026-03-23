import type { ApiResponse, PageResult } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getAccessToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('accessToken');
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = this.getAccessToken();
    
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Token expired or invalid, redirect to login
        if (typeof window !== 'undefined') {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('userInfo');
          window.location.href = '/login';
        }
        throw new Error('Unauthorized');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result: ApiResponse<T> = await response.json();
    
    if (result.code !== 200) {
      throw new Error(result.message || 'Request failed');
    }

    return result.data;
  }

  async get<T>(endpoint: string, params?: Record<string, unknown>): Promise<T> {
    let url = endpoint;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          searchParams.append(key, String(value));
        }
      });
      const queryString = searchParams.toString();
      if (queryString) {
        url += `?${queryString}`;
      }
    }
    return this.request<T>(url, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'DELETE',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async upload<T>(endpoint: string, formData: FormData): Promise<T> {
    const token = this.getAccessToken();
    
    const headers: HeadersInit = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result: ApiResponse<T> = await response.json();
    
    if (result.code !== 200) {
      throw new Error(result.message || 'Upload failed');
    }

    return result.data;
  }
}

export const api = new ApiClient(API_BASE_URL);

// Auth API
export const authApi = {
  login: (data: { username: string; password: string }) => 
    api.post<{ token: string; tokenType: string; expiresIn: number }>('/auth/login', data),
  
  logout: () => api.post('/auth/logout'),
  
  getInfo: () => api.get<{
    id: number;
    username: string;
    nickname: string;
    email: string;
    phone: string;
    avatar: string;
    status: number;
    roles: { id: number; name: string; code: string }[];
    permissions: string[];
  }>('/auth/info'),
  
  register: (data: { username: string; password: string; nickname: string; email: string }) =>
    api.post('/auth/register', data),
};

// User API
export const userApi = {
  getList: (params: { page?: number; size?: number; keyword?: string; status?: number }) =>
    api.get<PageResult<{
      id: number;
      username: string;
      nickname: string;
      email: string;
      phone: string;
      avatar: string;
      status: number;
      roles: { id: number; name: string; code: string }[];
      createdAt: string;
    }>>('/system/users', params),
  
  getById: (id: number) => 
    api.get<{
      id: number;
      username: string;
      nickname: string;
      email: string;
      phone: string;
      avatar: string;
      status: number;
      roles: { id: number; name: string; code: string }[];
    }>(`/system/users/${id}`),
  
  create: (data: {
    username: string;
    password: string;
    nickname: string;
    email: string;
    phone: string;
    status: number;
    roleIds: number[];
  }) => api.post('/system/users', data),
  
  update: (id: number, data: {
    nickname: string;
    email: string;
    phone: string;
    status: number;
    roleIds: number[];
  }) => api.put(`/system/users/${id}`, data),
  
  delete: (id: number) => api.delete(`/system/users/${id}`),
  
  batchDelete: (ids: number[]) => api.delete('/system/users/batch', ids),
  
  updatePassword: (id: number, data: { oldPassword: string; newPassword: string }) =>
    api.put(`/system/users/${id}/password`, data),
  
  resetPassword: (id: number) => api.put(`/system/users/${id}/reset-password`),
  
  updateStatus: (id: number, status: number) =>
    api.put(`/system/users/${id}/status`, { status }),
  
  updateRoles: (id: number, roleIds: number[]) =>
    api.put(`/system/users/${id}/roles`, { roleIds }),
  
  uploadAvatar: (id: number, formData: FormData) =>
    api.upload<{ url: string }>(`/system/users/${id}/avatar`, formData),
};

// Role API
export const roleApi = {
  getList: (params: { page?: number; size?: number; keyword?: string; status?: number }) =>
    api.get<PageResult<{
      id: number;
      name: string;
      code: string;
      description: string;
      status: number;
      createdAt: string;
    }>>('/system/roles', params),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      name: string;
      code: string;
      description: string;
      status: number;
      menus: { id: number }[];
    }>(`/system/roles/${id}`),
  
  create: (data: {
    name: string;
    code: string;
    description: string;
    status: number;
    menuIds: number[];
  }) => api.post('/system/roles', data),
  
  update: (id: number, data: {
    name: string;
    code: string;
    description: string;
    status: number;
    menuIds: number[];
  }) => api.put(`/system/roles/${id}`, data),
  
  delete: (id: number) => api.delete(`/system/roles/${id}`),
  
  updateMenus: (id: number, menuIds: number[]) =>
    api.put(`/system/roles/${id}/menus`, { menuIds }),
};

// Menu API
export const menuApi = {
  getTree: (params?: { status?: number }) =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      path: string;
      icon: string;
      sort: number;
      type: string;
      permission: string;
      visible: number;
      status: number;
      children: {
        id: number;
        parentId: number;
        name: string;
        path: string;
        icon: string;
        sort: number;
        type: string;
        permission: string;
        visible: number;
        status: number;
      }[];
    }[]>('/system/menus', params),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      path: string;
      icon: string;
      sort: number;
      type: string;
      permission: string;
      visible: number;
      status: number;
    }>(`/system/menus/${id}`),
  
  create: (data: {
    parentId: number | null;
    name: string;
    path: string;
    icon: string;
    sort: number;
    type: string;
    permission: string;
    visible: number;
    status: number;
  }) => api.post('/system/menus', data),
  
  update: (id: number, data: {
    parentId: number | null;
    name: string;
    path: string;
    icon: string;
    sort: number;
    type: string;
    permission: string;
    visible: number;
    status: number;
  }) => api.put(`/system/menus/${id}`, data),
  
  delete: (id: number) => api.delete(`/system/menus/${id}`),
  
  getUserMenus: () =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      path: string;
      icon: string;
      sort: number;
      children: {
        id: number;
        parentId: number;
        name: string;
        path: string;
        icon: string;
        sort: number;
      }[];
    }[]>('/system/menus/user-menus'),
};

// Log API
export const logApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    operator?: string;
    operation?: string;
    startDate?: string;
    endDate?: string;
  }) =>
    api.get<PageResult<{
      id: number;
      operator: string;
      operation: string;
      method: string;
      params: string;
      ip: string;
      createTime: string;
      time: number;
    }>>('/system/logs', params),
  
  clear: () => api.delete('/system/logs/clear'),
};

// Product API
export const productApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    categoryId?: number;
    status?: number;
  }) =>
    api.get<PageResult<{
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
    }>>('/stock/products', params),
  
  getById: (id: number) =>
    api.get<{
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
    }>(`/stock/products/${id}`),
  
  getByCode: (code: string) =>
    api.get<{
      id: number;
      name: string;
      code: string;
      barcode: string;
      categoryId: number;
      specification: string;
      unit: string;
      purchasePrice: number;
      salePrice: number;
      status: number;
      image: string;
    }>(`/stock/products/code/${code}`),
  
  getByBarcode: (barcode: string) =>
    api.get<{
      id: number;
      name: string;
      code: string;
      barcode: string;
      categoryId: number;
      specification: string;
      unit: string;
      purchasePrice: number;
      salePrice: number;
      status: number;
      image: string;
    }>(`/stock/products/barcode/${barcode}`),
  
  create: (data: {
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
  }) => api.post('/stock/products', data),
  
  update: (id: number, data: {
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
  }) => api.put(`/stock/products/${id}`, data),
  
  delete: (id: number) => api.delete(`/stock/products/${id}`),
  
  batchDelete: (ids: number[]) => api.delete('/stock/products/batch', ids),
  
  updateStatus: (id: number, status: number) =>
    api.put(`/stock/products/${id}/status`, { status }),
  
  uploadImage: (id: number, formData: FormData) =>
    api.upload<{ url: string }>(`/stock/products/${id}/image`, formData),
  
  getLowStock: () =>
    api.get<{
      id: number;
      name: string;
      code: string;
      quantity: number;
      warningQuantity: number;
    }[]>('/stock/products/low-stock'),
  
  getActive: () =>
    api.get<{
      id: number;
      name: string;
      code: string;
      salePrice: number;
    }[]>('/stock/products/active'),
  
  getSelect: () =>
    api.get<{
      id: number;
      name: string;
      code: string;
      barcode: string;
      specification: string;
      unit: string;
      salePrice: number;
    }[]>('/stock/products/select'),
};

// Category API
export const categoryApi = {
  getTree: () =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      code: string;
      sort: number;
      status: number;
      children: {
        id: number;
        parentId: number;
        name: string;
        code: string;
        sort: number;
        status: number;
      }[];
    }[]>('/stock/categories'),
  
  getAll: () =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      code: string;
      sort: number;
      status: number;
    }[]>('/stock/categories/all'),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      parentId: number | null;
      name: string;
      code: string;
      sort: number;
      status: number;
    }>(`/stock/categories/${id}`),
  
  create: (data: {
    parentId: number | null;
    name: string;
    code: string;
    sort: number;
    status: number;
  }) => api.post('/stock/categories', data),
  
  update: (id: number, data: {
    parentId: number | null;
    name: string;
    code: string;
    sort: number;
    status: number;
  }) => api.put(`/stock/categories/${id}`, data),
  
  delete: (id: number) => api.delete(`/stock/categories/${id}`),
};

// Inventory API
export const inventoryApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    warningStatus?: string;
  }) =>
    api.get<PageResult<{
      id: number;
      productId: number;
      productName: string;
      productCode: string;
      categoryName: string;
      quantity: number;
      warningQuantity: number;
      warningStatus: string;
      specification: string;
      unit: string;
      updatedAt: string;
    }>>('/stock/inventory', params),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      productId: number;
      productName: string;
      productCode: string;
      categoryName: string;
      quantity: number;
      warningQuantity: number;
      warningStatus: string;
      specification: string;
      unit: string;
    }>(`/stock/inventory/${id}`),
  
  getByProductId: (productId: number) =>
    api.get<{
      id: number;
      productId: number;
      quantity: number;
      warningQuantity: number;
      warningStatus: string;
    }>(`/stock/inventory/product/${productId}`),
  
  updateQuantity: (id: number, data: { quantity: number }) =>
    api.put(`/stock/inventory/${id}/quantity`, data),
  
  updateWarning: (id: number, data: { warningQuantity: number }) =>
    api.put(`/stock/inventory/${id}/warning`, data),
  
  getLowStock: () =>
    api.get<{
      id: number;
      productId: number;
      productName: string;
      productCode: string;
      quantity: number;
      warningQuantity: number;
    }[]>('/stock/inventory/low-stock'),
  
  getOverStock: () =>
    api.get<{
      id: number;
      productId: number;
      productName: string;
      productCode: string;
      quantity: number;
      warningQuantity: number;
    }[]>('/stock/inventory/over-stock'),
};

// Inbound API
export const inboundApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    supplierId?: number;
    startDate?: string;
    endDate?: string;
  }) =>
    api.get<PageResult<{
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
    }>>('/stock/inbound', params),
  
  getById: (id: number) =>
    api.get<{
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
    }>(`/stock/inbound/${id}`),
  
  create: (data: {
    productId: number;
    quantity: number;
    unitPrice: number;
    supplierId: number;
    remark: string;
  }) => api.post('/stock/inbound', data),
  
  update: (id: number, data: {
    productId: number;
    quantity: number;
    unitPrice: number;
    supplierId: number;
    remark: string;
  }) => api.put(`/stock/inbound/${id}`, data),
  
  delete: (id: number) => api.delete(`/stock/inbound/${id}`),
  
  batchDelete: (ids: number[]) => api.delete('/stock/inbound/batch', ids),
};

// Outbound API
export const outboundApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    startDate?: string;
    endDate?: string;
  }) =>
    api.get<PageResult<{
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
    }>>('/stock/outbound', params),
  
  getById: (id: number) =>
    api.get<{
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
    }>(`/stock/outbound/${id}`),
  
  create: (data: {
    productId: number;
    quantity: number;
    unitPrice: number;
    remark: string;
  }) => api.post('/stock/outbound', data),
  
  update: (id: number, data: {
    productId: number;
    quantity: number;
    unitPrice: number;
    remark: string;
  }) => api.put(`/stock/outbound/${id}`, data),
  
  delete: (id: number) => api.delete(`/stock/outbound/${id}`),
  
  batchDelete: (ids: number[]) => api.delete('/stock/outbound/batch', ids),
};

// Supplier API
export const supplierApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    status?: number;
  }) =>
    api.get<PageResult<{
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
    }>>('/purchase/suppliers', params),
  
  getAll: () =>
    api.get<{
      id: number;
      name: string;
      code: string;
      contact: string;
      phone: string;
    }[]>('/purchase/suppliers/all'),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      name: string;
      code: string;
      contact: string;
      phone: string;
      email: string;
      address: string;
      status: number;
      remark: string;
    }>(`/purchase/suppliers/${id}`),
  
  create: (data: {
    name: string;
    code: string;
    contact: string;
    phone: string;
    email: string;
    address: string;
    status: number;
    remark: string;
  }) => api.post('/purchase/suppliers', data),
  
  update: (id: number, data: {
    name: string;
    code: string;
    contact: string;
    phone: string;
    email: string;
    address: string;
    status: number;
    remark: string;
  }) => api.put(`/purchase/suppliers/${id}`, data),
  
  delete: (id: number) => api.delete(`/purchase/suppliers/${id}`),
  
  updateStatus: (id: number, status: number) =>
    api.put(`/purchase/suppliers/${id}/status`, { status }),
};

// Supplier Evaluation API
export const evaluationApi = {
  getList: (params: { page?: number; size?: number; supplierId?: number }) =>
    api.get<PageResult<{
      id: number;
      supplierId: number;
      supplierName: string;
      score: number;
      content: string;
      evaluator: string;
      createdAt: string;
    }>>('/purchase/evaluations', params),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      supplierId: number;
      supplierName: string;
      score: number;
      content: string;
      evaluator: string;
      createdAt: string;
    }>(`/purchase/evaluations/${id}`),
  
  create: (data: {
    supplierId: number;
    score: number;
    content: string;
  }) => api.post('/purchase/evaluations', data),
  
  update: (id: number, data: {
    supplierId: number;
    score: number;
    content: string;
  }) => api.put(`/purchase/evaluations/${id}`, data),
  
  delete: (id: number) => api.delete(`/purchase/evaluations/${id}`),
};

// Purchase Order API
export const purchaseOrderApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    supplierId?: number;
    status?: string;
    startDate?: string;
    endDate?: string;
  }) =>
    api.get<PageResult<{
      id: number;
      orderNo: string;
      supplierId: number;
      supplierName: string;
      totalAmount: number;
      status: string;
      remark: string;
      creator: string;
      approver: string;
      approveTime: string;
      createdAt: string;
    }>>('/purchase/orders', params),
  
  getById: (id: number) =>
    api.get<{
      id: number;
      orderNo: string;
      supplierId: number;
      supplierName: string;
      totalAmount: number;
      status: string;
      remark: string;
      items: {
        id: number;
        productId: number;
        productName: string;
        productCode: string;
        specification: string;
        unit: string;
        quantity: number;
        unitPrice: number;
        totalAmount: number;
      }[];
      creator: string;
      approver: string;
      approveTime: string;
      createdAt: string;
    }>(`/purchase/orders/${id}`),
  
  create: (data: {
    supplierId: number;
    remark: string;
    items: {
      productId: number;
      quantity: number;
      unitPrice: number;
    }[];
  }) => api.post('/purchase/orders', data),
  
  update: (id: number, data: {
    supplierId: number;
    remark: string;
    items: {
      productId: number;
      quantity: number;
      unitPrice: number;
    }[];
  }) => api.put(`/purchase/orders/${id}`, data),
  
  delete: (id: number) => api.delete(`/purchase/orders/${id}`),
  
  submit: (id: number) => api.put(`/purchase/orders/${id}/submit`),
  
  approve: (id: number) => api.put(`/purchase/orders/${id}/approve`),
  
  reject: (id: number, reason: string) => api.put(`/purchase/orders/${id}/reject`, { reason }),
  
  cancel: (id: number) => api.put(`/purchase/orders/${id}/cancel`),
  
  receive: (id: number) => api.put(`/purchase/orders/${id}/receive`),
};

// Dashboard API
export const dashboardApi = {
  getStats: () =>
    api.get<{
      productCount: number;
      supplierCount: number;
      orderCount: number;
      warningCount: number;
      todayInbound: number;
      todayOutbound: number;
      totalInventoryValue: number;
    }>('/dashboard/stats'),
  
  getTrend: (days: number = 7) =>
    api.get<{
      date: string;
      inbound: number;
      outbound: number;
      purchase: number;
    }[]>('/dashboard/trend', { days }),
  
  getCategoryDistribution: () =>
    api.get<{
      name: string;
      value: number;
      count: number;
    }[]>('/dashboard/category-distribution'),
  
  getPurchaseVsStock: () =>
    api.get<{
      name: string;
      purchase: number;
      stock: number;
    }[]>('/dashboard/purchase-vs-stock'),
};

// Warehouse API
export const warehouseApi = {
  getList: (params: {
    page?: number;
    size?: number;
    keyword?: string;
    status?: number;
  }) =>
    api.get<PageResult<{
      id: number;
      warehouseCode: string;
      warehouseName: string;
      address: string;
      contactPerson: string;
      contactPhone: string;
      status: number;
      capacity: number;
      description: string;
      createdAt: string;
      updatedAt: string;
    }>>('/stock/warehouses', params),

  getAll: () =>
    api.get<{
      id: number;
      warehouseCode: string;
      warehouseName: string;
      address: string;
    }[]>('/stock/warehouses/all'),

  getById: (id: number) =>
    api.get<{
      id: number;
      warehouseCode: string;
      warehouseName: string;
      address: string;
      contactPerson: string;
      contactPhone: string;
      status: number;
      capacity: number;
      description: string;
    }>(`/stock/warehouses/${id}`),

  getByCode: (code: string) =>
    api.get<{
      id: number;
      warehouseCode: string;
      warehouseName: string;
      address: string;
      contactPerson: string;
      contactPhone: string;
      status: number;
      capacity: number;
      description: string;
    }>(`/stock/warehouses/code/${code}`),

  create: (data: {
    warehouseCode: string;
    warehouseName: string;
    address: string;
    contactPerson: string;
    contactPhone: string;
    status: number;
    capacity: number;
    description: string;
  }) => api.post('/stock/warehouses', data),

  update: (id: number, data: {
    warehouseCode: string;
    warehouseName: string;
    address: string;
    contactPerson: string;
    contactPhone: string;
    capacity: number;
    description: string;
  }) => api.put(`/stock/warehouses/${id}`, data),

  delete: (id: number) => api.delete(`/stock/warehouses/${id}`),

  updateStatus: (id: number, status: number) =>
    api.put(`/stock/warehouses/${id}/status`, { status }),

  count: () => api.get<number>('/stock/warehouses/count'),
};
