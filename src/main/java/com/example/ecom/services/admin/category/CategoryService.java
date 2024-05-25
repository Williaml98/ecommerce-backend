package com.example.ecom.services.admin.category;

import com.example.ecom.dto.CategoryDto;
import com.example.ecom.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(CategoryDto categoryDto);

    List<Category> getAllCategories();
}
