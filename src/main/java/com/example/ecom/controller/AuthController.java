package com.example.ecom.controller;

import com.example.ecom.dto.AuthenticationRequest;
import com.example.ecom.dto.SignUpRequest;
import com.example.ecom.dto.UserDto;
import com.example.ecom.entity.User;
import com.example.ecom.repository.UserRepository;
import com.example.ecom.services.auth.AuthService;
import com.example.ecom.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;
    private static final String TOKEN_PREFIX = "Bearer";

    private static final String HEADER_STRING = "Authorization";

    private final AuthService authService;

    @PostMapping("/authenticate")
    public void createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest,
                                          HttpServletResponse response,  HttpServletRequest request) throws IOException, JSONException {
        logger.info("Received authentication request: {}", authenticationRequest);
        try{
            //logger.info("Attempting to authenticate user: {}", authenticationRequest.getUsername());
            logger.info("Attempting to authenticate user: {}", authenticationRequest.getEmail());

            /*authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()));*/
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()));
        }catch (BadCredentialsException e){
           // logger.error("Authentication failed for user: {}", authenticationRequest.getUsername(), e);
            logger.error("Authentication failed for user: {}", authenticationRequest.getEmail(), e);
            throw new BadCredentialsException("Incorrect username or password.");
        }

        //final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());


        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            HttpSession session = request.getSession(true); // Create a new session or get the existing one

            // Set session attributes
            session.setAttribute("userId", user.getId().toString());
            session.setAttribute("userRole", user.getRole().toString());
            session.setAttribute("firstName", user.getName().toString());

            // Prepare the response data
            JSONObject responseData = new JSONObject();
            responseData.put("sessionId", session.getId());
            responseData.put("userRole", user.getRole().toString());
            responseData.put("userId", user.getId().toString());
            responseData.put("firstName", user.getName().toString());

            // Log session and user information
            logger.info("Session ID: {}", session.getId());
            logger.info("Session Attributes: {}", session.getAttributeNames());
            logger.info("Logged-in User ID: {}", user.getId());
            logger.info("Logged-in User Role: {}", user.getRole());
            logger.info("Logged-in User First Name: {}", user.getName());

            response.getWriter().write(responseData.toString());

        }
    }
    @RequestMapping("/sign-up")
    public ResponseEntity<?> signupUser(@RequestBody SignUpRequest signUpRequest){
        if(authService.hasUserWithEmail(signUpRequest.getEmail())){
            return new ResponseEntity<>("user already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        UserDto userDto = authService.createUser(signUpRequest);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,
                                         @RequestHeader("X-Session-Id") String sessionId) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getId().equals(sessionId)) {
            session.invalidate(); // Invalidate the session
        }
        return ResponseEntity.ok("Logout successful");
    }

}
