package com.yazidwms.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardResponse(
            long totalProducts,
            long totalWarehouses,
            long totalSuppliers,
            long totalCustomers,
            BigDecimal inventoryValue,
            long inventoryCount,
            long lowStockProducts,
            List<?> recentMovements,
            Map<String, Long> purchaseOrdersByStatus,
            Map<String, Long> salesOrdersByStatus,
            List<?> topSellingProducts,
            List<?> topPurchasedProducts,
            Map<String, Long> monthlyInventoryActivity,
            Map<String, BigDecimal> monthlySales,
            Map<String, BigDecimal> monthlyPurchases
    ) {
    }
}
