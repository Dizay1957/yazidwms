package com.yazidwms.dashboard.service;

import com.yazidwms.dashboard.dto.DashboardDtos.DashboardResponse;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.purchaseorder.repository.PurchaseOrderRepository;
import com.yazidwms.salesorder.repository.SalesOrderRepository;
import com.yazidwms.stockmovement.repository.StockMovementRepository;
import com.yazidwms.supplier.repository.SupplierRepository;
import com.yazidwms.customer.repository.CustomerRepository;
import com.yazidwms.warehouse.repository.WarehouseRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;

    public DashboardService(ProductRepository productRepository, WarehouseRepository warehouseRepository,
                            SupplierRepository supplierRepository, CustomerRepository customerRepository,
                            StockMovementRepository stockMovementRepository, PurchaseOrderRepository purchaseOrderRepository,
                            SalesOrderRepository salesOrderRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Cacheable("dashboard")
    @Transactional(readOnly = true)
    public DashboardResponse overview() {
        return new DashboardResponse(
                productRepository.countByDeletedFalse(),
                warehouseRepository.countByDeletedFalse(),
                supplierRepository.countByDeletedFalse(),
                customerRepository.countByDeletedFalse(),
                productRepository.inventoryValue(),
                productRepository.inventoryCount(),
                productRepository.countByDeletedFalseAndQuantityLessThanEqual(10),
                stockMovementRepository.findTop10ByOrderByTimestampDesc().stream()
                        .map(movement -> Map.of(
                                "id", movement.getId(),
                                "type", movement.getType().name(),
                                "sku", movement.getProduct().getSku(),
                                "quantity", movement.getQuantity(),
                                "timestamp", movement.getTimestamp().toString()))
                        .toList(),
                toMap(purchaseOrderRepository.countByStatusGrouped()),
                toMap(salesOrderRepository.countByStatusGrouped()),
                productRepository.findTop10ByDeletedFalseOrderByUpdatedAtDesc().stream()
                        .map(product -> Map.of("sku", product.getSku(), "name", product.getName(), "quantity", product.getQuantity()))
                        .toList(),
                productRepository.findTop10ByDeletedFalseOrderByUpdatedAtDesc().stream()
                        .map(product -> Map.of("sku", product.getSku(), "name", product.getName(), "quantity", product.getQuantity()))
                        .toList(),
                emptyMonthlyLong(),
                emptyMonthlyMoney(),
                emptyMonthlyMoney()
        );
    }

    private Map<String, Long> toMap(java.util.List<Object[]> rows) {
        var map = new LinkedHashMap<String, Long>();
        rows.forEach(row -> map.put(String.valueOf(row[0]), (Long) row[1]));
        return map;
    }

    private Map<String, Long> emptyMonthlyLong() {
        var map = new LinkedHashMap<String, Long>();
        for (int i = 5; i >= 0; i--) {
            map.put(YearMonth.now().minusMonths(i).toString(), 0L);
        }
        return map;
    }

    private Map<String, BigDecimal> emptyMonthlyMoney() {
        var map = new LinkedHashMap<String, BigDecimal>();
        for (int i = 5; i >= 0; i--) {
            map.put(YearMonth.now().minusMonths(i).toString(), BigDecimal.ZERO);
        }
        return map;
    }
}
