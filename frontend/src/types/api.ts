export type RoleName = "ADMIN" | "MANAGER" | "WAREHOUSE_OPERATOR" | "VIEWER";
export type BusinessStatus = "ACTIVE" | "INACTIVE" | "BLOCKED";
export type CustomerType = "COMPANY" | "INDIVIDUAL";
export type UserStatus = "ACTIVE" | "PENDING_ACTIVATION" | "DISABLED";
export type PurchaseOrderStatus = "DRAFT" | "CONFIRMED" | "RECEIVED" | "CANCELLED";
export type SalesOrderStatus = "DRAFT" | "CONFIRMED" | "SHIPPED" | "CANCELLED";
export type StockMovementType = "IN" | "OUT" | "TRANSFER" | "ADJUSTMENT";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  roles: RoleName[];
}

export interface Product {
  id: number;
  sku: string;
  barcode: string;
  name: string;
  description?: string;
  categoryId: number;
  categoryName: string;
  supplierId: number;
  supplierName: string;
  purchasePrice: number;
  sellingPrice: number;
  unit: string;
  weight: number;
  quantity: number;
  minimumQuantity: number;
  maximumQuantity: number;
  active: boolean;
  lowStock: boolean;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  active: boolean;
}

export interface Supplier {
  id: number;
  companyName: string;
  contactName?: string;
  email: string;
  phone?: string;
  taxNumber?: string;
  country?: string;
  city?: string;
  address?: string;
  status: BusinessStatus;
  active: boolean;
}

export interface Customer {
  id: number;
  customerType: CustomerType;
  companyName?: string;
  fullName: string;
  email: string;
  phone?: string;
  country?: string;
  city?: string;
  address?: string;
  status: BusinessStatus;
  active: boolean;
}

export interface Warehouse {
  id: number;
  code: string;
  name: string;
  country?: string;
  city?: string;
  address?: string;
  active: boolean;
}

export interface LocationNode {
  id: number;
  code: string;
  name?: string;
  parentId: number;
}

export interface Bin {
  id: number;
  code: string;
  capacity: number;
  shelfId: number;
  active: boolean;
}

export interface InventoryItem {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  binId: number;
  binCode: string;
  quantity: number;
}

export interface StockMovement {
  id: number;
  type: StockMovementType;
  productId: number;
  sku: string;
  fromBinId?: number;
  toBinId?: number;
  quantity: number;
  timestamp: string;
  reference: string;
  reason: string;
  previousQuantity: number;
  newQuantity: number;
  notes?: string;
}

export interface OrderItem {
  productId: number;
  sku: string;
  binId: number;
  binCode: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface PurchaseOrder {
  id: number;
  orderNumber: string;
  supplierId: number;
  supplierName: string;
  status: PurchaseOrderStatus;
  totalAmount: number;
  confirmedAt?: string;
  receivedAt?: string;
  items: OrderItem[];
}

export interface SalesOrder {
  id: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  status: SalesOrderStatus;
  totalAmount: number;
  confirmedAt?: string;
  shippedAt?: string;
  items: OrderItem[];
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  status: UserStatus;
  active: boolean;
  lastLoginAt?: string;
  roles: RoleName[];
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  name: RoleName;
  description: string;
}

export interface DashboardData {
  totalProducts: number;
  totalWarehouses: number;
  totalSuppliers: number;
  totalCustomers: number;
  inventoryValue: number;
  inventoryCount: number;
  lowStockProducts: number;
  recentMovements: StockMovement[];
  purchaseOrdersByStatus: Record<string, number>;
  salesOrdersByStatus: Record<string, number>;
  topSellingProducts: unknown[];
  topPurchasedProducts: unknown[];
  monthlyInventoryActivity: Record<string, number>;
  monthlySales: Record<string, number>;
  monthlyPurchases: Record<string, number>;
}

export interface AuditEvent {
  id: number;
  userId: number | "";
  timestamp: string;
  action: string;
  ipAddress: string;
  entity: string;
  entityId: number | "";
  details: string;
}
