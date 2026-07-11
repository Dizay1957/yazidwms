package com.yazidwms.category.dto;

import jakarta.validation.constraints.NotBlank;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategoryRequest(@NotBlank String name, String description, Long parentId) {
    }

    public record CategoryResponse(Long id, String name, String description, Long parentId, String parentName, boolean active) {
    }
}
