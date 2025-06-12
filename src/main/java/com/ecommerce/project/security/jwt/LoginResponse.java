package com.ecommerce.project.security.jwt;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String jwtToken;

    private String username;
    private List<String> roles;


}


