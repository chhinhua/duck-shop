package com.hdshop.controller;

import com.hdshop.dto.order.CheckOutDTO;
import com.hdshop.dto.order.OrderDTO;
import com.hdshop.dto.order.OrderPageResponse;
import com.hdshop.dto.order.OrderResponse;
import com.hdshop.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Order")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Create new order by list cartItem")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @SecurityRequirement(name = "Bear Authentication")
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderDTO orderDTO, Principal principal) {
        OrderResponse newOrder = orderService.addOrder(orderDTO, principal);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Create follow order from user cart")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrderFromCart(@Valid @RequestBody OrderDTO orderDTO, Principal principal) {
        OrderResponse newOrder = orderService.createOrderFromUserCart(orderDTO, principal);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "User delete order by orderId")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/isdeleted/{id}")
    public ResponseEntity<String> isDeletedOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.isDeletedOrderById(id));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Admin delete order by orderId")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deleteOrderById(id));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Get single order by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(
            summary = "Update order status by id",
            description = "Update order status by one of values {CANCELED, DELIVERED, ,CANCELED, PROCESSING, ORDERED}"
    )
    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @Operation(summary = "Get list order of user by username")
    @GetMapping("/user")
    public ResponseEntity<List<?>> getOrdersByUsername(@RequestParam String username) {
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }

    @Operation(summary = "Get list order of user by token")
    @GetMapping("/token")
    public ResponseEntity<List<OrderResponse>> getOrdersByToken(Principal principal) {
        return ResponseEntity.ok(orderService.getListOrderByCurrentUser(principal));
    }

    @Operation(summary = "Get list order of user by userId")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<?>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Get checkout data for page", description = "From user information (adderss, total price of cart)")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/checkout-data")
    public ResponseEntity<CheckOutDTO> getCheckoutData(Principal principal) {
        return ResponseEntity.ok(orderService.getDataFromUserInfor(principal));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(
            summary = "Get all orders",
            description = "Get all orders via REST API with pagination"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<OrderPageResponse> getAllOrders(
            @RequestParam(value = "pageNo", required = false,
                    defaultValue = "${paging.default.page-number}") int pageNo,
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${paging.default.page-size}") int pageSize
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(pageNo, pageSize));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Search your order by status")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchYourOrder(
            @RequestParam(value = "status", required = false) String statusValue, Principal principal) {
        return ResponseEntity.ok(orderService.findForUserByStatus(statusValue, principal));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Search, sort, filter orders for admin")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/search")
    public ResponseEntity<OrderPageResponse> search(
            @RequestParam(name = "status", required = false) String statusValue,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "sort", required = false) List<String> sortCriteria,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize
    ) {
        OrderPageResponse searchResponse = orderService.filter(
                statusValue, key, sortCriteria, pageNo, pageSize
        );
        return ResponseEntity.ok(searchResponse);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @Operation(summary = "Make payment with COD")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/pay-cod")
    public ResponseEntity<OrderResponse> makePaymentForCOD(@RequestBody OrderDTO order, @RequestParam("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.makePaymentForCOD(order, orderId));
    }
}
