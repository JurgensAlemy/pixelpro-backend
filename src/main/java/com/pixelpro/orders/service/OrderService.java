package com.pixelpro.orders.service;

import com.pixelpro.orders.dto.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto create(OrderDto dto);

    OrderDto findById(Long id);

    List<OrderDto> findAll();

    OrderDto update(Long id, OrderDto dto);

    void delete(Long id);
}

