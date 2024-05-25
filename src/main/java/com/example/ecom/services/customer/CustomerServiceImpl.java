package com.example.ecom.services.customer;

import com.example.ecom.dto.ProductDetailDto;
import com.example.ecom.dto.ProductDto;
import com.example.ecom.entity.Product;
import com.example.ecom.repository.ProductRepository;
import com.example.ecom.services.admin.adminproduct.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerProductService{

    private final ProductRepository productRepository;

    private static final Logger logger = LoggerFactory.getLogger(AdminProductService.class);

    public List<ProductDto> getAllProducts(){
        List<Product> products = productRepository.findAll();
        logger.info("Fetched all products, count: {}", products.size());
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    public List<ProductDto> searchProductByTitle(String name){
        List<Product> products = productRepository.findAllByNameContaining(name);
        logger.info("Fetched all products, count: {}", products.size());
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    public ProductDetailDto getProductDetailById(Long productId){
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if(optionalProduct.isPresent()){
            ProductDetailDto productDetailDto = new ProductDetailDto();

            productDetailDto.setProductDto(optionalProduct.get().getDto());

            return productDetailDto;
        }
        return null;
    }
}
