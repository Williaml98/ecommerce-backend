package com.example.ecom.controller.customer;

import com.example.ecom.controller.admin.AdminProductController;
import com.example.ecom.dto.ProductDetailDto;
import com.example.ecom.dto.ProductDto;
import com.example.ecom.services.customer.CustomerProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProductController {

    private final CustomerProductService customerProductService;

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);


    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts(){
        logger.info("Received request to fetch all products");

        List<ProductDto> productDtos = customerProductService.getAllProducts();
        logger.info("Fetched {} products", productDtos.size());

        return ResponseEntity.ok(productDtos);
    }
    @GetMapping("/search/{name}")
    public ResponseEntity<List<ProductDto>> getAllProductByName(@PathVariable String name) {
        logger.info("Received request to fetch a product by name");

        List<ProductDto> productDtos = customerProductService.searchProductByTitle(name);
        logger.info("Fetched {} product by name", productDtos.size());

        return ResponseEntity.ok(productDtos);
    }
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDetailDto> getProductDetailById(@PathVariable Long productId){
        ProductDetailDto productDetailDto = customerProductService.getProductDetailById(productId);
        if(productDetailDto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productDetailDto);
    }

}
