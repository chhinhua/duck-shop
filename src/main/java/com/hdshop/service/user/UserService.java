package com.hdshop.service.user;

import com.hdshop.dto.user.ChangePassReq;
import com.hdshop.dto.user.UserDTO;
import com.hdshop.dto.user.UserProfile;
import com.hdshop.dto.user.UserResponse;

import java.security.Principal;
import java.util.List;

public interface UserService {
    UserDTO getUserById(final Long id);

    String changePasswordOfCurrentUser(final ChangePassReq request, final Principal pricipal);

    String forgotPassword(final String email, final String newPassword);

    UserDTO getUserByUsername(final String username);

    UserDTO getUserByUsernameOrEmail(final String usernameOrEmail);

    UserDTO updateProfile(final UserProfile profile, final Principal principal);

    UserDTO updateProfileByUserId(final UserProfile profile, final Long userId);

    UserDTO changeLockedStatus(final Long userId);

    UserResponse getAllUsers(final int pageNo, final int pageSize);
}
