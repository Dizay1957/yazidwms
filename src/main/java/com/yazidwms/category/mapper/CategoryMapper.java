package com.yazidwms.category.mapper;

import com.yazidwms.category.dto.CategoryDtos.CategoryResponse;
import com.yazidwms.category.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    default CategoryResponse toResponse(Category category) {
        var parent = category.getParent();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                parent == null ? null : parent.getId(),
                parent == null ? null : parent.getName(),
                category.isActive()
        );
    }
}
