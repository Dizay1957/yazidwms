import { Outlet, Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "../auth/ProtectedRoute";
import { AppLayout } from "../layouts/AppLayout";
import { CategoriesPage } from "../pages/CategoriesPage";
import { CustomersPage } from "../pages/CustomersPage";
import { DashboardPage } from "../pages/DashboardPage";
import { InventoryPage } from "../pages/InventoryPage";
import { LoginPage } from "../pages/LoginPage";
import { MovementsPage } from "../pages/MovementsPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { ProductsPage } from "../pages/ProductsPage";
import { PurchaseOrdersPage } from "../pages/PurchaseOrdersPage";
import { ReportsPage } from "../pages/ReportsPage";
import { SalesOrdersPage } from "../pages/SalesOrdersPage";
import { SettingsPage } from "../pages/SettingsPage";
import { SuppliersPage } from "../pages/SuppliersPage";
import { UsersPage } from "../pages/UsersPage";
import { WarehousesPage } from "../pages/WarehousesPage";

export function AppRoutes({ mode, onToggleMode }: { mode: "light" | "dark"; onToggleMode: () => void }) {
  const Shell = () => (
    <AppLayout mode={mode} onToggleMode={onToggleMode}>
      <Outlet />
    </AppLayout>
  );

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Shell />}>
          <Route index element={<DashboardPage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="categories" element={<CategoriesPage />} />
          <Route path="suppliers" element={<SuppliersPage />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="warehouses" element={<WarehousesPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="movements" element={<MovementsPage />} />
          <Route path="purchase-orders" element={<PurchaseOrdersPage />} />
          <Route path="sales-orders" element={<SalesOrdersPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute roles={["ADMIN", "MANAGER"]} />}>
        <Route element={<Shell />}>
          <Route path="reports" element={<ReportsPage />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
        <Route element={<Shell />}>
          <Route path="users" element={<UsersPage />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
