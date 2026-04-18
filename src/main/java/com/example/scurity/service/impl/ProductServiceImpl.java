package com.example.scurity.service.impl;

import com.example.scurity.dto.ProductRequest;
import com.example.scurity.dto.ProductResponse;
import com.example.scurity.entity.Product;
import com.example.scurity.exception.DuplicateResourceException;
import com.example.scurity.exception.EntityNotFoundException;
import com.example.scurity.mapper.ProductMapper;
import com.example.scurity.repository.ProductRepository;
import com.example.scurity.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductResponse> findAll() {
        log.debug("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse findById(Long id) {
        log.debug("Fetching product with id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creating product with name='{}'", request.getName());
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Producto", "nombre", request.getName());
        }
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        log.info("Product created with id={}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product with id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));

        boolean nameConflict = productRepository.existsByNameIgnoreCase(request.getName())
                && !product.getName().equalsIgnoreCase(request.getName());
        if (nameConflict) {
            throw new DuplicateResourceException("Producto", "nombre", request.getName());
        }

        productMapper.updateEntity(product, request);
        Product saved = productRepository.save(product);
        log.info("Product updated with id={}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting product with id={}", id);
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Producto", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id={}", id);
    }
}
