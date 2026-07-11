package com.yazidwms.inventory.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.inventory.dto.InventoryDtos.InventoryAdjustmentRequest;
import com.yazidwms.inventory.dto.InventoryDtos.InventoryResponse;
import com.yazidwms.inventory.dto.InventoryDtos.StockMovementResponse;
import com.yazidwms.inventory.dto.InventoryDtos.TransferRequest;
import com.yazidwms.inventory.entity.Inventory;
import com.yazidwms.inventory.mapper.InventoryMapper;
import com.yazidwms.inventory.repository.InventoryRepository;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.service.ProductService;
import com.yazidwms.security.SecurityUtils;
import com.yazidwms.stockmovement.entity.StockMovement;
import com.yazidwms.stockmovement.entity.StockMovementType;
import com.yazidwms.stockmovement.repository.StockMovementRepository;
import com.yazidwms.warehouse.entity.WarehouseBin;
import com.yazidwms.warehouse.service.WarehouseService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository movementRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final InventoryMapper inventoryMapper;
    private final AuditService auditService;

    public InventoryService(InventoryRepository inventoryRepository, StockMovementRepository movementRepository,
                            ProductService productService, WarehouseService warehouseService, InventoryMapper inventoryMapper,
                            AuditService auditService) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.inventoryMapper = inventoryMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> list(Pageable pageable) {
        return PageResponse.from(inventoryRepository.findAll(pageable).map(inventoryMapper::toResponse));
    }

    @Transactional
    public InventoryResponse adjust(InventoryAdjustmentRequest request) {
        var product = productService.findActive(request.productId());
        var bin = warehouseService.findBin(request.binId());
        var inventory = getOrCreate(product, bin);
        var previous = inventory.getQuantity();
        var delta = request.newQuantity() - previous;
        inventory.setQuantity(request.newQuantity());
        if (delta >= 0) {
            productService.increaseQuantity(product, delta);
        } else {
            productService.decreaseQuantity(product, Math.abs(delta));
        }
        movement(StockMovementType.ADJUSTMENT, product, null, bin, Math.abs(delta), previous, request.newQuantity(), "ADJ-" + UUID.randomUUID(), request.reason(), request.notes());
        auditService.log("INVENTORY_UPDATED", "Inventory", inventory.getId(), "Adjusted stock", null);
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public void receive(Product product, WarehouseBin bin, int quantity, String reference) {
        var inventory = getOrCreate(product, bin);
        var previous = inventory.getQuantity();
        inventory.setQuantity(previous + quantity);
        productService.increaseQuantity(product, quantity);
        movement(StockMovementType.IN, product, null, bin, quantity, previous, inventory.getQuantity(), reference, "Purchase order received", null);
    }

    @Transactional
    public void issue(Product product, WarehouseBin bin, int quantity, String reference) {
        var inventory = getOrCreate(product, bin);
        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("Insufficient stock in bin " + bin.getCode());
        }
        var previous = inventory.getQuantity();
        inventory.setQuantity(previous - quantity);
        productService.decreaseQuantity(product, quantity);
        movement(StockMovementType.OUT, product, bin, null, quantity, previous, inventory.getQuantity(), reference, "Sales order shipped", null);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        var product = productService.findActive(request.productId());
        var from = warehouseService.findBin(request.fromBinId());
        var to = warehouseService.findBin(request.toBinId());
        if (from.getId().equals(to.getId())) {
            throw new BusinessException("Source and destination bins must be different");
        }
        var source = getOrCreate(product, from);
        if (source.getQuantity() < request.quantity()) {
            throw new BusinessException("Insufficient stock in source bin");
        }
        var destination = getOrCreate(product, to);
        var previousSource = source.getQuantity();
        source.setQuantity(previousSource - request.quantity());
        destination.setQuantity(destination.getQuantity() + request.quantity());
        movement(StockMovementType.TRANSFER, product, from, to, request.quantity(), previousSource, source.getQuantity(), "TRF-" + UUID.randomUUID(), request.reason(), request.notes());
        auditService.log("INVENTORY_TRANSFERRED", "Product", product.getId(), "Transferred inventory between bins", null);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> movements(Long productId, Pageable pageable) {
        var page = productId == null ? movementRepository.findAll(pageable) : movementRepository.findByProductId(productId, pageable);
        return PageResponse.from(page.map(inventoryMapper::toResponse));
    }

    private Inventory getOrCreate(Product product, WarehouseBin bin) {
        return inventoryRepository.findByProductIdAndBinIdAndDeletedFalse(product.getId(), bin.getId())
                .orElseGet(() -> {
                    var inventory = new Inventory();
                    inventory.setProduct(product);
                    inventory.setBin(bin);
                    inventory.setQuantity(0);
                    return inventoryRepository.save(inventory);
                });
    }

    private void movement(StockMovementType type, Product product, WarehouseBin from, WarehouseBin to, int quantity,
                          int previous, int current, String reference, String reason, String notes) {
        var movement = new StockMovement();
        movement.setType(type);
        movement.setProduct(product);
        movement.setFromBin(from);
        movement.setToBin(to);
        movement.setQuantity(quantity);
        movement.setTimestamp(Instant.now());
        movement.setReference(reference);
        movement.setReason(reason);
        movement.setPerformedBy(SecurityUtils.currentUserOrNull());
        movement.setPreviousQuantity(previous);
        movement.setNewQuantity(current);
        movement.setNotes(notes);
        movementRepository.save(movement);
    }
}
