package com.pixelpro.orders.service;

import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.repository.AddressRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.mapper.OrderMapper;
import com.pixelpro.orders.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            CustomerRepository customerRepository,
                            AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public OrderDto create(OrderDto dto) {
        OrderEntity entity = new OrderEntity();
        entity.setCode(dto.code());
        entity.setStatus(OrderStatus.valueOf(dto.status()));
        entity.setDeliveryType(DeliveryType.valueOf(dto.deliveryType()));
        entity.setSubtotal(dto.subtotal());
        entity.setShippingCost(dto.shippingCost());
        entity.setDiscount(dto.discount());
        entity.setTotal(dto.total());

        // Asignar customer
        if (dto.customerId() != null) {
            CustomerEntity customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + dto.customerId()));
            entity.setCustomer(customer);
        }

        // Asignar shippingAddress si existe
        if (dto.shippingAddressId() != null) {
            AddressEntity address = addressRepository.findById(dto.shippingAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + dto.shippingAddressId()));
            entity.setShippingAddress(address);
        }

        // Ignorar items por ahora

        OrderEntity saved = orderRepository.save(entity);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return orderMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public OrderDto update(Long id, OrderDto dto) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        // Actualizar campos simples
        entity.setCode(dto.code());
        entity.setStatus(OrderStatus.valueOf(dto.status()));
        entity.setDeliveryType(DeliveryType.valueOf(dto.deliveryType()));
        entity.setSubtotal(dto.subtotal());
        entity.setShippingCost(dto.shippingCost());
        entity.setDiscount(dto.discount());
        entity.setTotal(dto.total());

        // Actualizar customer
        if (dto.customerId() != null) {
            CustomerEntity customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + dto.customerId()));
            entity.setCustomer(customer);
        }

        // Actualizar shippingAddress
        if (dto.shippingAddressId() != null) {
            AddressEntity address = addressRepository.findById(dto.shippingAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + dto.shippingAddressId()));
            entity.setShippingAddress(address);
        } else {
            entity.setShippingAddress(null);
        }

        // No modificar items aún

        OrderEntity updated = orderRepository.save(entity);
        return orderMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}

