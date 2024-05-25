package com.example.ecom.controller.admin;

import com.example.ecom.dto.ProductDto;
import com.example.ecom.entity.Product;
import com.example.ecom.services.admin.adminproduct.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    @PostMapping("product")
    public ResponseEntity<ProductDto> addProduct(@ModelAttribute ProductDto productDto) throws IOException {
        logger.info("Received request to add product: {}", productDto);
        ProductDto productDto1 = adminProductService.addProduct(productDto);
        logger.info("Product created with ID: {}", productDto1.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productDto1);
    }
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts(){
        logger.info("Received request to fetch all products");

        List<ProductDto> productDtos = adminProductService.getAllProducts();
        logger.info("Fetched {} products", productDtos.size());

        return ResponseEntity.ok(productDtos);
    }
    @GetMapping("/search/{name}")
    public ResponseEntity<List<ProductDto>> getAllProductByName(@PathVariable String name) {
        logger.info("Received request to fetch a product by name");

        List<ProductDto> productDtos = adminProductService.getAllProductsByName(name);
        logger.info("Fetched {} product by name", productDtos.size());

        return ResponseEntity.ok(productDtos);
    }
    @RequestMapping("/product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId){
        boolean deleted = adminProductService.deleteProduct(productId);
        if(deleted){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId){
        ProductDto productDto = adminProductService.getProductById(productId);
        if(productDto != null){
            return ResponseEntity.ok(productDto);
        }else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/product/{productId}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long productId, @ModelAttribute ProductDto productDto) throws IOException {
        ProductDto updatedProduct = adminProductService.updateProduct(productId, productDto);
        if(updatedProduct != null){
            return ResponseEntity.ok(updatedProduct);
        }else {
            return ResponseEntity.notFound().build();
        }
    }

}
