package com.example.ecom.config;

import com.example.ecom.entity.User;
import com.example.ecom.filters.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private final JwtRequestFilter authFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers("/authenticate", "/sign-up","/logout","/api/admin/categories",
                        "/api/admin/category" ,"/api/admin/products","/api/admin/product",
                        "/api/admin/search/{name}","/api/admin/product/{productId}",
                        "/api/customer/products","/api/customer/search/{name}",
                        "/api/customer/cart","/api/customer/cart/{userId}",
                        "/api/admin/coupons","/api/customer/coupon/{userId}/{code}","/api/customer/addition",
                        "/api/customer/deduction","/api/customer/placeOrder","/api/admin/placedOrders",
                        "/api/admin/order/{orderId}/{status}","/api/customer/myOrders/{userId}",
                        "/api/admin/product/{productId}","/api/admin/product/{productId}",
                        "/api/customer/product/{productId}","/api/customer/wishlist",
                        "/api/customer/wishlist/{userId}","/order/{trackingId}","/order/**")
                .permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/**")
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

}
