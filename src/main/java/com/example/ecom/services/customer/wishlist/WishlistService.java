package com.example.ecom.services.customer.wishlist;

import com.example.ecom.dto.WishlistDto;

import java.util.List;

public interface WishlistService {

    WishlistDto addProductToWishList(WishlistDto wishlistDto);

    List<WishlistDto> getWishlistByUserId(Long userId);
}
