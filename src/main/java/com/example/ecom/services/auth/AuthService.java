package com.example.ecom.services.auth;

import com.example.ecom.dto.SignUpRequest;
import com.example.ecom.dto.UserDto;

public interface AuthService {

    UserDto createUser(SignUpRequest signUpRequest);


    Boolean hasUserWithEmail(String email);
}
