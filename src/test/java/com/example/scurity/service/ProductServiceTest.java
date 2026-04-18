package com.example.scurity.service;

import com.example.scurity.dto.ProductRequest;
import com.example.scurity.dto.ProductResponse;
import com.example.scurity.entity.Product;
import com.example.scurity.exception.DuplicateResourceException;
import com.example.scurity.exception.EntityNotFoundException;
import com.example.scurity.mapper.ProductMapper;
import com.example.scurity.repository.ProductRepository;
import com.example.scurity.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest validRequest;
    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ProductRequest("Widget", "A fine widget", new BigDecimal("9.99"), 100);
        product = new Product("Widget", "A fine widget", new BigDecimal("9.99"), 100);
        productResponse = new ProductResponse(1L, "Widget", "A fine widget",
                new BigDecimal("9.99"), 100, null, null);
    }

    @Test
    void findAll_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Widget");
    }

    @Test
    void findById_existingId_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingId_throwsEntityNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_newProduct_returnsCreatedProduct() {
        when(productRepository.existsByNameIgnoreCase("Widget")).thenReturn(false);
        when(productMapper.toEntity(validRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.create(validRequest);

        assertThat(result.getName()).isEqualTo("Widget");
        verify(productRepository).save(product);
    }

    @Test
    void create_duplicateName_throwsDuplicateResourceException() {
        when(productRepository.existsByNameIgnoreCase("Widget")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Widget");

        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_existingId_deletesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsEntityNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).deleteById(any());
    }
}
