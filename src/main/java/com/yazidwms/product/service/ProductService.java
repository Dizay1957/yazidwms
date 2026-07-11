package com.yazidwms.product.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.category.service.CategoryService;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.product.dto.ProductDtos.ProductRequest;
import com.yazidwms.product.dto.ProductDtos.ProductResponse;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.mapper.ProductMapper;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.supplier.service.SupplierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ProductMapper productMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final String notificationAdminEmail;

    public ProductService(ProductRepository productRepository, CategoryService categoryService, SupplierService supplierService,
                          ProductMapper productMapper, AuditService auditService, NotificationService notificationService,
                          @Value("${app.notification.admin-email}") String notificationAdminEmail) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.productMapper = productMapper;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.notificationAdminEmail = notificationAdminEmail;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessException("SKU already exists");
        }
        if (productRepository.existsByBarcodeIgnoreCase(request.barcode())) {
            throw new BusinessException("Barcode already exists");
        }
        var product = new Product();
        apply(request, product);
        var saved = productRepository.save(product);
        auditService.log("PRODUCT_CREATED", "Product", saved.getId(), saved.getSku(), null);
        return productMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, Pageable pageable) {
        var page = q == null || q.isBlank()
                ? productRepository.findByDeletedFalse(pageable)
                : productRepository.findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndSkuContainingIgnoreCaseOrDeletedFalseAndBarcodeContainingIgnoreCase(q, q, q, pageable);
        return PageResponse.from(page.map(productMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return productMapper.toResponse(findActive(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        var product = findActive(id);
        apply(request, product);
        auditService.log("PRODUCT_UPDATED", "Product", id, product.getSku(), null);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        var product = findActive(id);
        if (product.getQuantity() > 0) {
            throw new BusinessException("Cannot delete referenced products with stock. Set inactive instead.");
        }
        product.setDeleted(true);
        product.setActive(false);
        auditService.log("PRODUCT_DELETED", "Product", id, product.getSku(), null);
    }

    @Transactional
    public void increaseQuantity(Product product, int quantity) {
        product.setQuantity(product.getQuantity() + quantity);
    }

    @Transactional
    public void decreaseQuantity(Product product, int quantity) {
        if (product.getQuantity() < quantity) {
            throw new BusinessException("Insufficient product stock for " + product.getSku());
        }
        product.setQuantity(product.getQuantity() - quantity);
        if (product.isLowStock()) {
            notificationService.lowStock(notificationAdminEmail, product.getSku(), product.getQuantity());
        }
    }

    public Product findActive(Long id) {
        var product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
        if (product.isDeleted()) {
            throw new NotFoundException("Product", id);
        }
        return product;
    }

    private void apply(ProductRequest request, Product product) {
        if (request.maximumQuantity() > 0 && request.minimumQuantity() > request.maximumQuantity()) {
            throw new BusinessException("Minimum quantity cannot be greater than maximum quantity");
        }
        product.setSku(request.sku());
        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(categoryService.findActive(request.categoryId()));
        product.setSupplier(supplierService.findActive(request.supplierId()));
        product.setPurchasePrice(request.purchasePrice() == null ? BigDecimal.ZERO : request.purchasePrice());
        product.setSellingPrice(request.sellingPrice() == null ? BigDecimal.ZERO : request.sellingPrice());
        product.setUnit(request.unit());
        product.setWeight(request.weight() == null ? BigDecimal.ZERO : request.weight());
        product.setMinimumQuantity(request.minimumQuantity());
        product.setMaximumQuantity(request.maximumQuantity());
        product.setActive(request.active() == null || request.active());
    }
}
