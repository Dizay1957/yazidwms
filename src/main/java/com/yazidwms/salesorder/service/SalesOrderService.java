package com.yazidwms.salesorder.service;

import com.yazidwms.common.api.PageResponse;
import com.yazidwms.customer.service.CustomerService;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.inventory.service.InventoryService;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.product.service.ProductService;
import com.yazidwms.salesorder.dto.SalesOrderDtos.SalesOrderRequest;
import com.yazidwms.salesorder.dto.SalesOrderDtos.SalesOrderResponse;
import com.yazidwms.salesorder.entity.SalesOrder;
import com.yazidwms.salesorder.entity.SalesOrderItem;
import com.yazidwms.salesorder.entity.SalesOrderStatus;
import com.yazidwms.salesorder.mapper.SalesOrderMapper;
import com.yazidwms.salesorder.repository.SalesOrderRepository;
import com.yazidwms.security.SecurityUtils;
import com.yazidwms.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final SalesOrderMapper salesOrderMapper;
    private final NotificationService notificationService;
    private final String notificationAdminEmail;

    public SalesOrderService(SalesOrderRepository salesOrderRepository, CustomerService customerService,
                             ProductService productService, WarehouseService warehouseService,
                             InventoryService inventoryService, SalesOrderMapper salesOrderMapper,
                             NotificationService notificationService,
                             @Value("${app.notification.admin-email}") String notificationAdminEmail) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.salesOrderMapper = salesOrderMapper;
        this.notificationService = notificationService;
        this.notificationAdminEmail = notificationAdminEmail;
    }

    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public SalesOrderResponse create(SalesOrderRequest request) {
        if (salesOrderRepository.existsByOrderNumberIgnoreCase(request.orderNumber())) {
            throw new BusinessException("Sales order number already exists");
        }
        var order = new SalesOrder();
        order.setOrderNumber(request.orderNumber());
        order.setCustomer(customerService.findActive(request.customerId()));
        order.setCreatedBy(SecurityUtils.currentUserOrNull());
        for (var itemRequest : request.items()) {
            var product = productService.findActive(itemRequest.productId());
            var item = new SalesOrderItem();
            item.setProduct(product);
            item.setBin(warehouseService.findBin(itemRequest.binId()));
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setLineTotal(itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            order.addItem(item);
        }
        recalculate(order);
        return salesOrderMapper.toResponse(salesOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<SalesOrderResponse> list(Pageable pageable) {
        return PageResponse.from(salesOrderRepository.findByDeletedFalse(pageable).map(salesOrderMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse get(Long id) {
        return salesOrderMapper.toResponse(findWithItems(id));
    }

    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public SalesOrderResponse confirm(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BusinessException("Only draft sales orders can be confirmed");
        }
        order.getItems().forEach(item -> {
            if (item.getProduct().getQuantity() < item.getQuantity()) {
                throw new BusinessException("Insufficient product stock for " + item.getProduct().getSku());
            }
        });
        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        return salesOrderMapper.toResponse(order);
    }

    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public SalesOrderResponse ship(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() == SalesOrderStatus.SHIPPED) {
            throw new BusinessException("Sales order has already been shipped");
        }
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw new BusinessException("Cancelled sales orders cannot be shipped");
        }
        for (var item : order.getItems()) {
            inventoryService.issue(item.getProduct(), item.getBin(), item.getQuantity(), order.getOrderNumber());
        }
        order.setStatus(SalesOrderStatus.SHIPPED);
        order.setShippedAt(Instant.now());
        notificationService.salesOrderShipped(notificationAdminEmail, order.getOrderNumber());
        return salesOrderMapper.toResponse(order);
    }

    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public SalesOrderResponse cancel(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() == SalesOrderStatus.SHIPPED) {
            throw new BusinessException("Shipped sales orders cannot be cancelled");
        }
        order.setStatus(SalesOrderStatus.CANCELLED);
        return salesOrderMapper.toResponse(order);
    }

    private SalesOrder findWithItems(Long id) {
        return salesOrderRepository.findWithItemsById(id).orElseThrow(() -> new NotFoundException("SalesOrder", id));
    }

    private void recalculate(SalesOrder order) {
        order.setTotalAmount(order.getItems().stream()
                .map(SalesOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
