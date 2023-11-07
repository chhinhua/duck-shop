package com.hdshop.service.auth;

import com.hdshop.component.RandomCodeGenerator;
import com.hdshop.dto.auth.*;
import com.hdshop.dto.user.UserDTO;
import com.hdshop.entity.Role;
import com.hdshop.entity.User;
import com.hdshop.exception.APIException;
import com.hdshop.exception.ResourceNotFoundException;
import com.hdshop.repository.RoleRepository;
import com.hdshop.repository.UserRepository;
import com.hdshop.security.JwtTokenProvider;
import com.hdshop.service.opt.OtpService;
import com.hdshop.service.sms.SmsService;
import com.hdshop.service.user.UserService;
import com.hdshop.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chhin Hua
 * @date 29-10-2023
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsService smsService;
    private final OtpService otpService;
    private final UserService userService;

    /**
     * Registers a new user based on the provided registration data.
     *
     * @param registerDTO The data containing user registration information.
     * @return A success message indicating that the user has been registered.
     * @throws APIException If the provided username or email already exist in the database, an exception is thrown with a Bad Request status and a specific message.
     */
    @Override
    public String register(RegisterDTO registerDTO) {
        // check if the username already exists in the database
        if (userRepository.existsUserByUsername(registerDTO.getUsername())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already exists!");
        }

        // check if the email already exists in the database
        if (userRepository.existsUserByEmail(registerDTO.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already exists!");
        }

        // create a new User object and populate it with the provided registration data
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setIsEmailActive(false);
        user.setIsEnabled(false);
        user.setIsPhoneActive(false);

        // set the user's role(s)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER").get();
        roles.add(userRole);
        user.setRoles(roles);

        // save the user to the database
        userRepository.save(user);

        String otp = OtpUtils.generateOTP();

        // send otp
        otpService.sendOTP(user.getEmail(), otp);

        // Return a success message
        return "Vui lòng kiểm tra email để hoàn tất đăng ký tài khoản!";
    }

    /**
     * Handles user login based on the provided login credentials.
     *
     * @param loginDTO The data containing user login information.
     * @return The JWT token generated upon successful authentication.
     */
    @Override
    public LoginResponse login(LoginDTO loginDTO) {
        // Authenticate user using provided credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsernameOrEmail(),
                        loginDTO.getPassword())
        );

        // Set the authenticated user's information in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate a JWT token for the authenticated user
        String token = jwtTokenProvider.generateToken(authentication);

        // Get user from token
        UserDTO user = userService.getUserByToken(token);

        // create jwtResponse object
        JwtAuthResponse jwtResponse = new JwtAuthResponse();
        jwtResponse.setAccessToken(token);

        // create LoginResponse object
        LoginResponse response = new LoginResponse(
                user,
                jwtResponse
        );

        return response;
    }

    @Override
    public String sendCodeByPhoneNumber(String phoneNumber) {
        // Verify sdt theo chuẩn số của việt nam
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }

        String randomCode = RandomCodeGenerator.generateRandomCode();

        // Gửi randomCode đến số điện thoại phoneNumber
        smsService.sendSms(phoneNumber, "Mã xác thực của bạn là: #" + randomCode);

        return randomCode;
    }

    @Override
    public String sendOTP_ByEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ");
        }

        String otp = OtpUtils.generateOTP();

        otpService.sendOTP(email, otp);

        return "Mã OTP đã được gửi, vui lòng kiểm tra email";
    }

    @Override
    public String verifyOTP_ByEmail(VerifyOtpRequest otpRequest) {
        // check if the email already exists in the database
        User user = userRepository
                .findByEmail(otpRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ email không chính xác"));

        user.setIsEnabled(true);
        user.setIsEmailActive(true);
        userRepository.save(user);

        return "Xác thực thành công, bạn đã có thể đăng nhập.";
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        // Regex cho số điện thoại theo chuẩn Việt Nam
        String regex = "^(03[2-9]|05[6-9]|07[0-9]|08[0-9]|09[0-9]|01[2-9])[0-9]{7}$";

        // Kiểm tra sự khớp đúng
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }

    public boolean isValidEmail(String email) {
        // Regex cho định dạng email
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // Kiểm tra sự khớp đúng
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
