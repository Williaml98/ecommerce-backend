package com.example.ecom.services.customer.cart;

import com.example.ecom.dto.AddProductInCartDto;
import com.example.ecom.dto.CartItemsDto;
import com.example.ecom.dto.OrderDto;
import com.example.ecom.dto.PlaceOrderDto;
import com.example.ecom.entity.*;
import com.example.ecom.enums.OrderStatus;
import com.example.ecom.exception.ValidationException;
import com.example.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartServiceImp implements CartService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private CouponRepository couponRepository;

    public ResponseEntity<?> addProductToCart(AddProductInCartDto addProductInCartDto) {
        // Check if there's an existing pending order for the user
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);

        if (activeOrder == null) {
            // No active order found, create a new one
            activeOrder = createNewOrder(addProductInCartDto.getUserId());
        }

        // Add the product to the active order's cart items
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());
        Optional<User> optionalUser = userRepository.findById(addProductInCartDto.getUserId());

        if (optionalProduct.isPresent() && optionalUser.isPresent()) {
            Product product = optionalProduct.get();
            User user = optionalUser.get();

            CartItems cartItem = new CartItems();
            cartItem.setProduct(product);
            cartItem.setPrice(product.getPrice());
            cartItem.setQuantity(1L);
            cartItem.setUser(user);
            cartItem.setOrder(activeOrder);

            // Save the cart item
            cartItemsRepository.save(cartItem);

            // Update the total amount in the order
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + cartItem.getPrice());
            activeOrder.setAmount(activeOrder.getAmount() + cartItem.getPrice());
            activeOrder.getCartItems().add(cartItem);

            // Save the order
            orderRepository.save(activeOrder);

            // Return success response
            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
        } else {
            // Product or user not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or product not found");
        }
    }


    private Order createNewOrder(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            Order newOrder = new Order();
            newOrder.setAmount(0L);
            newOrder.setTotalAmount(0L);
            newOrder.setDiscount(0L);
            newOrder.setUser(optionalUser.get());
            newOrder.setOrderStatus(OrderStatus.Pending);
            return orderRepository.save(newOrder);
        }
        return null;
    }

    public OrderDto getCartByUserId(Long usrId) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(usrId, OrderStatus.Pending);

        // If no active order found with OrderStatus.Pending, try to find the latest order with OrderStatus.Pending
        if (activeOrder == null) {
            activeOrder = orderRepository.findTopByUserIdAndOrderStatusOrderByIdDesc(usrId, OrderStatus.Pending);
        }

        if (activeOrder != null) {
            List<CartItemsDto> cartItemsDtoList = activeOrder.getCartItems().stream().map(CartItems::getCartDto).collect(Collectors.toList());

            OrderDto orderDto = new OrderDto();
            orderDto.setAmount(activeOrder.getAmount());
            orderDto.setId(activeOrder.getId());
            orderDto.setOrderStatus(activeOrder.getOrderStatus());
            orderDto.setDiscount(activeOrder.getDiscount());
            orderDto.setTotalAmount(activeOrder.getTotalAmount());
            orderDto.setCartItems(cartItemsDtoList);

            // may delete
            if (activeOrder.getCoupon() != null) {
                orderDto.setCouponName(activeOrder.getCoupon().getName());
            }

            return orderDto;
        }

        // If no active order found, return null or throw an exception as per your requirement
        return null;
    }



   /* public OrderDto applyCoupon(Long usrId, String code){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(usrId, OrderStatus.Pending);
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(()-> new ValidationException("Coupon not found"));

        if(couponIsExpired(coupon)){
            throw new ValidationException("Coupon has expired");
        }

        double discountAmount = ((coupon.getDiscount() / 100.0) * activeOrder.getTotalAmount());
        double netAmount = activeOrder.getTotalAmount() - discountAmount;

        activeOrder.setAmount((long)netAmount);
        activeOrder.setDiscount((long)discountAmount);
        activeOrder.setCoupon(coupon);

        orderRepository.save(activeOrder);
        return activeOrder.getOrderDto();
    }*/

    public OrderDto applyCoupon(Long usrId, String code) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(usrId, OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("Active order not found");
        }

        Coupon coupon = couponRepository.findByCode(code).orElseThrow(() -> new ValidationException("Coupon not found"));

        if (couponIsExpired(coupon)) {
            throw new ValidationException("Coupon has expired");
        }

        double discountAmount = (coupon.getDiscount() / 100.0) * activeOrder.getTotalAmount();
        double netAmount = activeOrder.getTotalAmount() - discountAmount;

        activeOrder.setAmount((long) netAmount);
        activeOrder.setDiscount((long) discountAmount);
        activeOrder.setCoupon(coupon);

        orderRepository.save(activeOrder);
        return activeOrder.getOrderDto();
    }


    private boolean couponIsExpired(Coupon coupon){
        Date currentDate = new Date();
        Date expirationDate = coupon.getExpirationDate();

        return expirationDate != null && currentDate.after(expirationDate);
    }

    /*public OrderDto increaseProductQuantity(AddProductInCartDto addProductInCartDto){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getUserId());

        Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if(optionalProduct.isPresent() && optionalCartItem.isPresent()){
            CartItems cartItem = optionalCartItem.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() + product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() + 1);

            if(activeOrder.getCoupon() != null){
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);
            }
            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;

    }

    public OrderDto decreaseProductQuantity(AddProductInCartDto addProductInCartDto){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getUserId());

        Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if(optionalProduct.isPresent() && optionalCartItem.isPresent()){
            CartItems cartItem = optionalCartItem.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() - product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() - product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() - 1);

            if(activeOrder.getCoupon() != null){
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);
            }
            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;

    }*/


    public OrderDto increaseProductQuantity(AddProductInCartDto addProductInCartDto) {
        try {
            Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
            Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

            if (activeOrder == null || optionalProduct.isEmpty()) {
                throw new ValidationException("Active order or product not found");
            }

            Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                    addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId());

            if (optionalCartItem.isPresent()) {
                CartItems cartItem = optionalCartItem.get();
                Product product = optionalProduct.get();

                activeOrder.setAmount(activeOrder.getAmount() + product.getPrice());
                activeOrder.setTotalAmount(activeOrder.getTotalAmount() + product.getPrice());
                cartItem.setQuantity(cartItem.getQuantity() + 1);

                if (activeOrder.getCoupon() != null) {
                    double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                    double netAmount = activeOrder.getTotalAmount() - discountAmount;
                    activeOrder.setAmount((long) netAmount);
                    activeOrder.setDiscount((long) discountAmount);
                }

                cartItemsRepository.save(cartItem);
                orderRepository.save(activeOrder);
                return activeOrder.getOrderDto();
            } else {
                throw new ValidationException("Cart item not found");
            }
        } catch (Exception e) {
            throw new ValidationException("Failed to increase product quantity: " + e.getMessage());
        }
    }

    public OrderDto decreaseProductQuantity(AddProductInCartDto addProductInCartDto) {
        try {
            Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
            Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

            if (activeOrder == null || optionalProduct.isEmpty()) {
                throw new ValidationException("Active order or product not found");
            }

            Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                    addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId());

            if (optionalCartItem.isPresent()) {
                CartItems cartItem = optionalCartItem.get();
                Product product = optionalProduct.get();

                if (cartItem.getQuantity() <= 1) {
                    throw new ValidationException("Cannot decrease quantity below 1");
                }

                activeOrder.setAmount(activeOrder.getAmount() - product.getPrice());
                activeOrder.setTotalAmount(activeOrder.getTotalAmount() - product.getPrice());
                cartItem.setQuantity(cartItem.getQuantity() - 1);

                if (activeOrder.getCoupon() != null) {
                    double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                    double netAmount = activeOrder.getTotalAmount() - discountAmount;
                    activeOrder.setAmount((long) netAmount);
                    activeOrder.setDiscount((long) discountAmount);
                }

                cartItemsRepository.save(cartItem);
                orderRepository.save(activeOrder);
                return activeOrder.getOrderDto();
            } else {
                throw new ValidationException("Cart item not found");
            }
        } catch (Exception e) {
            throw new ValidationException("Failed to decrease product quantity: " + e.getMessage());
        }
    }


    public OrderDto placeOrder(PlaceOrderDto placeOrderDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(placeOrderDto.getUserId(), OrderStatus.Pending);
        Optional<User> optionalUser = userRepository.findById(placeOrderDto.getUserId());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            activeOrder.setOrderDescription(placeOrderDto.getOrderDescription());
            activeOrder.setAddress(placeOrderDto.getAddress());
            activeOrder.setDate(new Date());
            activeOrder.setOrderStatus(OrderStatus.Placed);
            activeOrder.setTrackingId(UUID.randomUUID());

            orderRepository.save(activeOrder);

            // Create a new Order entity with OrderStatus.Pending for the user's new cart
            Order newOrder = new Order();
            newOrder.setAmount(0L);
            newOrder.setTotalAmount(0L);
            newOrder.setDiscount(0L);
            newOrder.setUser(user);
            newOrder.setOrderStatus(OrderStatus.Pending);
            orderRepository.save(newOrder);

            // Return the new order with OrderStatus.Pending
            return newOrder.getOrderDto();
        }
        return null;
    }

   public List<OrderDto> getMyPlacedOrder(Long userId){
        return orderRepository.findByUserIdAndOrderStatusIn(userId, List.of(OrderStatus.Placed,
                OrderStatus.Shipped,
                OrderStatus.Delivered)).stream().map(Order::getOrderDto)
                .collect(Collectors.toList());
   }

    public OrderDto searchOrderByTrackingId(UUID trackingId){
        Optional<Order> optionalOrder = orderRepository.findByTrackingId(trackingId);
        if(optionalOrder.isPresent()){
            return optionalOrder.get().getOrderDto();
        }
        return null;
    }

}
