package com.example.scurity.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " no encontrado con id: " + id);
    }
}
