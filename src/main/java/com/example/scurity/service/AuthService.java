package com.example.scurity.service;

import com.example.scurity.dto.AuthRequest;
import com.example.scurity.dto.AuthResponse;
import com.example.scurity.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);
}
