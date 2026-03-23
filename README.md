# StockMaster - 企业库存管理系统

## 项目简介

StockMaster 是一个功能完善的企业级库存管理系统，采用前后端分离架构开发。后端基于 Spring Boot 3.x，前端基于 Next.js 16，提供完整的库存管理、采购管理、系统管理等功能。

## 技术栈

### 后端技术
- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **MySQL 8.0** - 数据库
- **JWT** - 无状态认证
- **Redisson** - 分布式缓存

### 前端技术
- **Next.js 16** - React 框架
- **TypeScript 5** - 类型安全
- **Tailwind CSS 4** - 样式框架
- **shadcn/ui** - UI 组件库
- **TanStack Query** - 服务端状态管理
- **Recharts** - 图表库
- **React Hook Form + Zod** - 表单验证

## 项目结构

```
StockMaster/
├── src/main/java/com/stockmaster/     # 后端源码
│   ├── common/                        # 公共模块
│   │   ├── config/                    # 配置类
│   │   ├── security/                  # 安全认证
│   │   ├── exception/                 # 异常处理
│   │   └── dto/                       # 公共 DTO
│   └── modules/                       # 业务模块
│       ├── dashboard/                 # 仪表盘
│       ├── stock/                     # 库存管理
│       │   ├── product/               # 商品
│       │   ├── category/              # 分类
│       │   ├── inventory/             # 库存
│       │   ├── inbound/               # 入库
│       │   ├── outbound/              # 出库
│       │   └── warehouse/             # 仓库
│       ├── purchase/                  # 采购管理
│       │   ├── supplier/              # 供应商
│       │   ├── evaluation/            # 评价
│       │   └── order/                 # 订单
│       └── system/                    # 系统管理
│           ├── user/                  # 用户
│           ├── role/                  # 角色
│           ├── menu/                  # 菜单
│           └── log/                   # 日志
└── frontend/                          # 前端项目（Next.js）
    └── src/
        ├── app/                       # 页面路由
        ├── components/                # 组件
        ├── lib/                       # 工具库
        ├── hooks/                     # 自定义 Hooks
        ├── stores/                    # 状态管理
        └── types/                     # 类型定义
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

1. **创建数据库**
   ```sql
   CREATE DATABASE stockmaster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **修改配置文件**
   
   编辑 `src/main/resources/application.yml`，修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/stockmaster?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root
       password: your_password
   ```

3. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

### 前端启动

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **配置 API 地址**
   
   编辑 `src/lib/api.ts`，修改 API 基础地址：
   ```typescript
   const API_BASE_URL = 'http://localhost:8080/api';
   ```

3. **启动开发服务器**
   ```bash
   npm run dev
   ```

4. **访问地址**
   - 前端地址: `http://localhost:3000`
   - API地址: `http://localhost:8080/api`

## 功能模块

### 1. 仪表盘
- 统计卡片（商品总数、供应商数量、采购订单、库存预警等）
- 采购趋势图表
- 商品分类分布图
- 库存预警列表

### 2. 商品管理
- **商品列表**: 增删改查、批量操作、上下架、图片上传
- **分类管理**: 树形结构、多级分类

### 3. 库存管理
- **库存查询**: 实时库存、预警状态筛选
- **仓库管理**: 仓库信息维护
- **入库管理**: 入库记录、自动更新库存
- **出库管理**: 出库记录、库存校验

### 4. 采购管理
- **供应商管理**: 供应商信息维护、状态管理
- **供应商评价**: 多维度评分、评价记录
- **采购订单**: 订单创建、审批流程、收货入库

### 5. 系统管理
- **用户管理**: 用户增删改查、角色分配、密码重置
- **角色管理**: 角色权限、菜单分配
- **菜单管理**: 动态菜单、权限控制
- **操作日志**: 用户操作记录、日志查询

## API 接口文档

### 认证接口 `/auth`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/login` | 用户登录 |
| POST | `/logout` | 用户登出 |
| GET | `/info` | 获取当前用户信息 |
| POST | `/register` | 用户注册 |

### 商品管理 `/stock/products`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询商品列表 |
| GET | `/{id}` | 查询商品详情 |
| GET | `/code/{code}` | 根据编码查询 |
| GET | `/barcode/{barcode}` | 根据条码查询 |
| POST | `/` | 创建商品 |
| PUT | `/{id}` | 修改商品 |
| DELETE | `/{id}` | 删除商品 |
| PUT | `/{id}/status` | 修改商品状态 |

### 库存管理 `/stock/inventory`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询库存列表 |
| GET | `/{id}` | 查询库存详情 |
| PUT | `/{id}/quantity` | 修改库存数量 |
| PUT | `/{id}/warning` | 设置库存预警 |
| GET | `/low-stock` | 查询低库存列表 |
| GET | `/over-stock` | 查询超储列表 |

### 仓库管理 `/stock/warehouses`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询仓库列表 |
| GET | `/all` | 获取所有启用的仓库 |
| GET | `/{id}` | 查询仓库详情 |
| POST | `/` | 创建仓库 |
| PUT | `/{id}` | 修改仓库 |
| DELETE | `/{id}` | 删除仓库 |
| PUT | `/{id}/status` | 修改仓库状态 |

### 入库管理 `/stock/inbound`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询入库列表 |
| POST | `/` | 创建入库记录 |
| PUT | `/{id}` | 修改入库记录 |
| DELETE | `/{id}` | 删除入库记录 |

### 出库管理 `/stock/outbound`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询出库列表 |
| POST | `/` | 创建出库记录 |
| PUT | `/{id}` | 修改出库记录 |
| DELETE | `/{id}` | 删除出库记录 |

### 供应商管理 `/purchase/suppliers`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询供应商列表 |
| GET | `/all` | 获取所有供应商 |
| POST | `/` | 创建供应商 |
| PUT | `/{id}` | 修改供应商 |
| DELETE | `/{id}` | 删除供应商 |
| PUT | `/{id}/status` | 修改供应商状态 |

### 采购订单 `/purchase/orders`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 查询订单列表 |
| GET | `/{id}` | 查询订单详情 |
| POST | `/` | 创建采购订单 |
| PUT | `/{id}` | 修改采购订单 |
| DELETE | `/{id}` | 删除采购订单 |
| PUT | `/{id}/submit` | 提交审核 |
| PUT | `/{id}/approve` | 审核通过 |
| PUT | `/{id}/reject` | 审核拒绝 |
| PUT | `/{id}/receive` | 订单收货 |

### 仪表盘 `/dashboard`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/stats` | 获取统计数据 |
| GET | `/trend` | 获取采购趋势 |
| GET | `/category-distribution` | 获取分类分布 |
| GET | `/purchase-vs-stock` | 获取采购库存对比 |

## 默认数据

系统启动时会自动初始化以下数据：

### 默认管理员账户
- 用户名: `admin`
- 密码: `admin123`
- 角色: 系统管理员（拥有所有权限）

### 默认角色
- `ADMIN` - 系统管理员
- `MANAGER` - 仓库管理员
- `USER` - 普通用户

### 默认商品分类
- 电子产品
- 服装鞋帽
- 食品饮料
- 日用百货
- 办公用品

## 特性亮点

1. **前后端分离**: 采用现代化的前后端分离架构，便于开发和部署
2. **响应式设计**: 完美适配桌面端和移动端
3. **权限控制**: 基于 RBAC 的细粒度权限控制
4. **实时预警**: 库存低库存和超储自动预警
5. **操作日志**: 完整的操作日志记录
6. **数据可视化**: 丰富的图表展示

## 许可证

MIT License

---

© 2024 StockMaster. All rights reserved.
