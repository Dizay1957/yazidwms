package com.yazidwms.product.mapper;

import com.yazidwms.product.dto.ProductDtos.ProductResponse;
import com.yazidwms.product.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    default ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getDescription(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSupplier().getId(),
                product.getSupplier().getCompanyName(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getUnit(),
                product.getWeight(),
                product.getQuantity(),
                product.getMinimumQuantity(),
                product.getMaximumQuantity(),
                product.isActive(),
                product.isLowStock()
        );
    }
}
