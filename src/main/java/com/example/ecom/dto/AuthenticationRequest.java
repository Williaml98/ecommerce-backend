package com.example.ecom.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {

   // private String username;
    private String email;

    private String password;
}
