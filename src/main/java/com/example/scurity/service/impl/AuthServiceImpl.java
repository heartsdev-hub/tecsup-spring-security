package com.example.scurity.service.impl;

import com.example.scurity.dto.AuthRequest;
import com.example.scurity.dto.AuthResponse;
import com.example.scurity.dto.RegisterResponse;
import com.example.scurity.entity.Usuario;
import com.example.scurity.exception.DuplicateResourceException;
import com.example.scurity.repository.UserRepository;
import com.example.scurity.security.JwtService;
import com.example.scurity.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RegisterResponse register(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Usuario", "email", email);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(UUID.randomUUID(), email, hashedPassword);
        Usuario saved = userRepository.save(usuario);
        log.info("Usuario registrado con id={} y email={}", saved.getId(), saved.getEmail());

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        Usuario usuario = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(usuario.getEmail());
        return new AuthResponse(token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
