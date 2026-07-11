package com.yazidwms.purchaseorder.service;

import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.inventory.service.InventoryService;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.product.service.ProductService;
import com.yazidwms.purchaseorder.dto.PurchaseOrderDtos.PurchaseOrderRequest;
import com.yazidwms.purchaseorder.dto.PurchaseOrderDtos.PurchaseOrderResponse;
import com.yazidwms.purchaseorder.entity.PurchaseOrder;
import com.yazidwms.purchaseorder.entity.PurchaseOrderItem;
import com.yazidwms.purchaseorder.entity.PurchaseOrderStatus;
import com.yazidwms.purchaseorder.mapper.PurchaseOrderMapper;
import com.yazidwms.purchaseorder.repository.PurchaseOrderRepository;
import com.yazidwms.security.SecurityUtils;
import com.yazidwms.supplier.service.SupplierService;
import com.yazidwms.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final NotificationService notificationService;
    private final String notificationAdminEmail;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, SupplierService supplierService,
                                ProductService productService, WarehouseService warehouseService,
                                InventoryService inventoryService, PurchaseOrderMapper purchaseOrderMapper,
                                NotificationService notificationService,
                                @Value("${app.notification.admin-email}") String notificationAdminEmail) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierService = supplierService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.notificationService = notificationService;
        this.notificationAdminEmail = notificationAdminEmail;
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        if (purchaseOrderRepository.existsByOrderNumberIgnoreCase(request.orderNumber())) {
            throw new BusinessException("Purchase order number already exists");
        }
        var order = new PurchaseOrder();
        order.setOrderNumber(request.orderNumber());
        order.setSupplier(supplierService.findActive(request.supplierId()));
        order.setCreatedBy(SecurityUtils.currentUserOrNull());
        for (var itemRequest : request.items()) {
            var product = productService.findActive(itemRequest.productId());
            var item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setBin(warehouseService.findBin(itemRequest.binId()));
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setLineTotal(itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            order.addItem(item);
        }
        recalculate(order);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> list(Pageable pageable) {
        return PageResponse.from(purchaseOrderRepository.findByDeletedFalse(pageable).map(purchaseOrderMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return purchaseOrderMapper.toResponse(findWithItems(id));
    }

    @Transactional
    public PurchaseOrderResponse confirm(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Only draft purchase orders can be confirmed");
        }
        order.setStatus(PurchaseOrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        return purchaseOrderMapper.toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessException("Purchase order has already been received");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("Cancelled purchase orders cannot be received");
        }
        for (var item : order.getItems()) {
            inventoryService.receive(item.getProduct(), item.getBin(), item.getQuantity(), order.getOrderNumber());
        }
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setReceivedAt(Instant.now());
        notificationService.purchaseOrderReceived(notificationAdminEmail, order.getOrderNumber());
        return purchaseOrderMapper.toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        var order = findWithItems(id);
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessException("Received purchase orders cannot be cancelled");
        }
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        return purchaseOrderMapper.toResponse(order);
    }

    private PurchaseOrder findWithItems(Long id) {
        return purchaseOrderRepository.findWithItemsById(id).orElseThrow(() -> new NotFoundException("PurchaseOrder", id));
    }

    private void recalculate(PurchaseOrder order) {
        order.setTotalAmount(order.getItems().stream()
                .map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
