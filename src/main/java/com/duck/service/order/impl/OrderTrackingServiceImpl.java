package com.duck.service.order.impl;

import com.duck.config.DateTimeConfig;
import com.duck.dto.order.OrderTrackingDTO;
import com.duck.entity.Order;
import com.duck.entity.OrderTracking;
import com.duck.exception.APIException;
import com.duck.exception.ResourceNotFoundException;
import com.duck.repository.OrderRepository;
import com.duck.repository.OrderTrackingRepository;
import com.duck.service.order.OrderTrackingService;
import com.duck.utils.enums.EOrderTrackingStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTrackingServiceImpl implements OrderTrackingService {
    private final OrderTrackingRepository trackingRepository;
    private final OrderRepository orderRepository;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(OrderTrackingServiceImpl.class);

    @Override
    public void create(OrderTrackingDTO dto) {
        Order order = getOrder(dto.getOrderId());
        EOrderTrackingStatus trackingStatus = EOrderTrackingStatus.fromStatus(dto.getStatus());
        OrderTracking tracking = new OrderTracking();
        tracking.setTime(DateTimeConfig.parseDateTime(dto.getTime()));
        tracking.setStatus(trackingStatus.getStatus());
        tracking.setDescription(trackingStatus.getDescription());
        tracking.setOrder(order);
        try {
            trackingRepository.save(tracking);
            logger.info("order_tracking created, order_id=", dto.getOrderId());
        } catch (Exception e) {
            logger.error("fail to create order_tracking, order_id=", dto.getOrderId());
            throw new APIException(String.format("%s, detail: %s", getMessage("create-order_tracking-failed"), e.getStackTrace()));
        }
    }

    @Override
    public void afterCreatedOrder(Long orderId) {
        Order order = getOrder(orderId);
        OrderTracking tracking = new OrderTracking();
        tracking.setStatus(EOrderTrackingStatus.READY_TO_PICK.getStatus());
        tracking.setDescription(EOrderTrackingStatus.READY_TO_PICK.getDescription());
        tracking.setTime(DateTimeConfig.getCurrentDateTimeInTimeZone());
        tracking.setOrder(order);
        try {
            trackingRepository.save(tracking);
            logger.info("order_tracking created, order_id=", orderId);
        } catch (Exception e) {
            logger.error("fail to create order_tracking, order_id=", orderId);
            throw new APIException(String.format("%s, detail: %s", getMessage("create-order_tracking-failed"), e.getStackTrace()));
        }
    }

    @Override
    public List<OrderTrackingDTO> getAll(Long orderId) {
        List<OrderTracking> orderTrackings = trackingRepository.findAllByOrderId(orderId);
        return orderTrackings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException(getMessage("order-not-found"))
        );
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private OrderTrackingDTO mapToDTO(OrderTracking entity) {
        return modelMapper.map(entity, OrderTrackingDTO.class);
    }
}
