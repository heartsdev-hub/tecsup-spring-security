package com.example.scurity.controller;

import com.example.scurity.dto.AuthRequest;
import com.example.scurity.entity.Product;
import com.example.scurity.entity.Usuario;
import com.example.scurity.repository.ProductRepository;
import com.example.scurity.repository.UserRepository;
import com.example.scurity.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String JWT_SECRET_BASE64 =
            "U2N1cml0eS1CYXNlbGluZS1KV1QtU2VjcmV0LUtleS0yMDI2LVNlY3VyZQ==";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_validRequest_returns201AndHashedPassword() throws Exception {
        AuthRequest request = new AuthRequest("student@example.com", "Secret123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("student@example.com"));

        Usuario usuario = userRepository.findByEmailIgnoreCase("student@example.com").orElseThrow();
        assertThat(usuario.getPassword()).isNotEqualTo("Secret123!");
        assertThat(passwordEncoder.matches("Secret123!", usuario.getPassword())).isTrue();
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        userRepository.save(new Usuario(
                UUID.randomUUID(),
                "student@example.com",
                passwordEncoder.encode("Secret123!")));

        AuthRequest request = new AuthRequest("student@example.com", "Secret123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        userRepository.save(new Usuario(
                UUID.randomUUID(),
                "student@example.com",
                passwordEncoder.encode("Secret123!")));

        AuthRequest request = new AuthRequest("student@example.com", "Secret123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        userRepository.save(new Usuario(
                UUID.randomUUID(),
                "student@example.com",
                passwordEncoder.encode("Secret123!")));

        AuthRequest request = new AuthRequest("student@example.com", "WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void products_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void products_withValidToken_returns200() throws Exception {
        productRepository.save(new Product("Widget", "A fine widget", new BigDecimal("9.99"), 100));
        userRepository.save(new Usuario(
                UUID.randomUUID(),
                "student@example.com",
                passwordEncoder.encode("Secret123!")));

        String token = jwtService.generateToken("student@example.com");

        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void products_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void products_withExpiredToken_returns401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET_BASE64));
        String expiredToken = Jwts.builder()
                .subject("student@example.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
