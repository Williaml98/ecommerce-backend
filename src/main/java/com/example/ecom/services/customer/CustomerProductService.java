package com.example.ecom.services.customer;

import com.example.ecom.dto.ProductDetailDto;
import com.example.ecom.dto.ProductDto;

import java.util.List;

public interface CustomerProductService {

    List<ProductDto> searchProductByTitle(String title);

    List<ProductDto> getAllProducts();

    ProductDetailDto getProductDetailById(Long productId);
}
