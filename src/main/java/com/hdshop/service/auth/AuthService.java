package com.hdshop.service.auth;

import com.hdshop.dto.auth.RegisterDTO;

public interface AuthService {
    String register(final RegisterDTO registerDTO);
}