package com.example.demo.dto.product;

import lombok.Data;

@Data
public class CreateProductDto {
    private Long id;
    private String name;
    private Long quantity;
}
