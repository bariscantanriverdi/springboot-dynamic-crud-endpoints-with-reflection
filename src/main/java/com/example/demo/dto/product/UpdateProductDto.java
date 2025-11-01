package com.example.demo.dto.product;

import lombok.Data;

@Data
public class UpdateProductDto {
    private Long id;
    private String name;
    private Long quantity;
}
