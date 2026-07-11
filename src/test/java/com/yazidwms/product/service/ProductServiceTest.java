package com.yazidwms.product.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.category.service.CategoryService;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.mapper.ProductMapper;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.supplier.service.SupplierService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ProductServiceTest {

    private final ProductService productService = new ProductService(
            mock(ProductRepository.class),
            mock(CategoryService.class),
            mock(SupplierService.class),
            mock(ProductMapper.class),
            mock(AuditService.class),
            mock(NotificationService.class),
            "admin@yazidwms.local"
    );

    @Test
    void decreaseQuantityPreventsNegativeStock() {
        var product = product();
        product.setQuantity(3);

        assertThatThrownBy(() -> productService.decreaseQuantity(product, 4))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient product stock");
    }

    @Test
    void increaseAndDecreaseQuantityMutateStock() {
        var product = product();
        productService.increaseQuantity(product, 10);
        productService.decreaseQuantity(product, 4);

        assertThat(product.getQuantity()).isEqualTo(6);
    }

    private Product product() {
        var product = new Product();
        product.setSku("SKU-TEST");
        product.setMinimumQuantity(1);
        product.setMaximumQuantity(100);
        return product;
    }
}
