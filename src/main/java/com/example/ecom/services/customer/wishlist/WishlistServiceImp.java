package com.example.ecom.services.customer.wishlist;

import com.example.ecom.dto.OrderDto;
import com.example.ecom.dto.WishlistDto;
import com.example.ecom.entity.Product;
import com.example.ecom.entity.User;
import com.example.ecom.entity.Wishlist;
import com.example.ecom.repository.ProductRepository;
import com.example.ecom.repository.UserRepository;
import com.example.ecom.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImp implements WishlistService {

    private final UserRepository userRepository;

    private final WishlistRepository wishlistRepository;

    private final ProductRepository productRepository;

    public WishlistDto addProductToWishList(WishlistDto wishlistDto){
        Optional<Product> optionalProduct = productRepository.findById(wishlistDto.getProductId());
        Optional<User> optionalUser = userRepository.findById(wishlistDto.getUserId());

        if(optionalProduct.isPresent() && optionalUser.isPresent()){
            Wishlist wishlist = new Wishlist();
            wishlist.setProduct(optionalProduct.get());
            wishlist.setUser(optionalUser.get());

            return wishlistRepository.save(wishlist).getWishlistDto();
        }
        return null;
    }

    public List<WishlistDto> getWishlistByUserId(Long userId){
        return wishlistRepository.findAllByUserId(userId).stream().map(Wishlist::getWishlistDto).collect(Collectors.toList());

    }





}
