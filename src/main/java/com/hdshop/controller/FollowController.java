package com.hdshop.controller;

import com.hdshop.dto.follow.FollowDTO;
import com.hdshop.dto.follow.FollowPageResponse;
import com.hdshop.service.follow.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@Tag(name = "Folowing")
@RequestMapping("/api/v1/follows")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(summary = "Toggle follow")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "Bear Authentication")
    @PutMapping
    public ResponseEntity<?> follow(@RequestParam(value = "productId") Long productId, Principal principal) {
        FollowDTO follow = followService.follow(productId, principal);
        return ResponseEntity.ok(follow);
    }

    @Operation(summary = "Get your wishlist")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/wishlist")
    public ResponseEntity<FollowPageResponse> getYourWishlist(
            @RequestParam(value = "pageNo", required = false,
                    defaultValue = "${paging.default.page-number}") int pageNo,
            @RequestParam(value = "pageSize", required = false,
                    defaultValue = "${paging.default.page-size}") int pageSize,
            Principal principal
    ) {
        return ResponseEntity.ok(followService.getYourFollow(pageNo, pageSize, principal));
    }

    @Operation(summary = "Get list productID of your wishlist")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/wishlist/product-ids")
    public ResponseEntity<?> getProductIdsOfYourWishlist(Principal principal) {
        return ResponseEntity.ok(followService.findProductIdsFollowedByUser(principal));
    }
}
