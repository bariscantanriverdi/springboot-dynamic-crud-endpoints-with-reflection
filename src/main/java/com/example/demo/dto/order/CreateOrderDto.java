package com.example.demo.dto.order;

import lombok.Data;

@Data
public class CreateOrderDto {
    private String product;
    private Long ownerId;
}
