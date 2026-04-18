package com.example.scurity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "el nombre es obligatorio")
    @Size(min = 2, max = 100, message = "el nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Size(max = 500, message = "la descripcion no debe exceder 500 caracteres")
    private String description;

    @NotNull(message = "el precio es obligatorio")
    @DecimalMin(value = "0.00", message = "el precio debe ser mayor o igual a cero")
    private BigDecimal price;

    @NotNull(message = "el stock es obligatorio")
    @Min(value = 0, message = "el stock debe ser mayor o igual a cero")
    private Integer stock;

    public ProductRequest() {
    }

    public ProductRequest(String name, String description, BigDecimal price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
