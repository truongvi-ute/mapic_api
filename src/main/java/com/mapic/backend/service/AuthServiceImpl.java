package com.mapic.backend.service;

import com.mapic.backend.dto.*;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.entity.AccountStatus;
import com.mapic.backend.entity.OtpType;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IOtpService otpService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        // 1. Kiểm tra tồn tại trong DB
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists");
        }

        // 2. Hash password trước khi lưu tạm vào Redis
        request.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. Lưu thông tin đăng ký vào Redis (TTL 15p)
        otpService.savePendingRegistration(request.getEmail(), request);

        // 4. Tạo và gửi OTP (TTL 5p)
        String otp = otpService.generateAndStoreOtp(request.getEmail(), OtpType.REGISTRATION);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.success("OTP sent to your email", request.getEmail());
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> verifyRegistration(VerifyOtpRequest request) {
        // Mặc định là REGISTRATION nếu không có type
        OtpType type = request.getType() != null ? request.getType() : OtpType.REGISTRATION;

        // 1. Kiểm tra OTP
        if (!otpService.validateOtp(request.getEmail(), request.getCode(), type)) {
            throw new AppException("Invalid or expired OTP");
        }

        // 2. Lấy thông tin đăng ký tạm thời từ Redis
        Optional<RegisterRequest> pendingReg = otpService.getPendingRegistration(request.getEmail());
        if (pendingReg.isEmpty()) {
            throw new AppException("Registration session expired. Please register again.");
        }

        RegisterRequest regData = pendingReg.get();

        // 3. Tạo User chính thức trong DB
        User user = User.builder()
                .name(regData.getName())
                .email(regData.getEmail())
                .phone(null) // Cập nhật sau nếu cần
                .failedLoginAttempts(0)
                .build();
        
        user.setUsername(regData.getUsername());
        user.setPassword(regData.getPassword());
        user.setStatus(AccountStatus.ACTIVE);

        System.out.println("Saving user to DB: username=" + user.getUsername() + ", email=" + user.getEmail());
        User savedUser = userRepository.saveAndFlush(user);

        // 4. Dọn dẹp Redis
        otpService.deleteOtp(request.getEmail(), OtpType.REGISTRATION);
        otpService.deletePendingRegistration(request.getEmail());

        // 5. Tạo Token và trả về kết quả
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();

        return ApiResponse.success("Registration successful", response);
    }

    @Override
    public ApiResponse<String> resendOtp(ResendOtpRequest request) {
        // Mặc định là REGISTRATION nếu không có type
        OtpType type = request.getType() != null ? request.getType() : OtpType.REGISTRATION;

        // Đăng ký mới cần kiểm tra session có còn không
        if (type == OtpType.REGISTRATION) {
            if (otpService.getPendingRegistration(request.getEmail()).isEmpty()) {
                throw new AppException("Registration session expired. Please register again.");
            }
        } else {
            // Quên mật khẩu cần kiểm tra email có tồn tại trong DB không
            if (!userRepository.existsByEmail(request.getEmail())) {
                throw new AppException("User with this email does not exist");
            }
        }

        String otp = otpService.generateAndStoreOtp(request.getEmail(), type);
        emailService.sendOtpEmail(request.getEmail(), otp);
        return ApiResponse.success("New OTP sent to your email", null);
    }

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        // 1. Authenticate bằng AuthenticationManager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new AppException("Invalid username/email or password");
        }

        // 2. Tìm User bằng Username hoặc Email để lấy thông tin tạo Token
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        if (user.getStatus() == AccountStatus.BLOCK) {
            throw new AppException("Account is blocked. Please contact admin.");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ApiResponse.success("Login successful", response);
    }

    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (!userRepository.existsByEmail(email)) {
            throw new AppException("Email not found");
        }
        
        String otp = otpService.generateAndStoreOtp(email, OtpType.FORGOT_PASSWORD);
        emailService.sendOtpEmail(email, otp);
        return ApiResponse.success("OTP sent to your email", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        // 1. Kiểm tra OTP - hỗ trợ cả FORGOT_PASSWORD và CHANGE_PASSWORD
        boolean isValidForgot = otpService.validateOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD);
        boolean isValidChange = otpService.validateOtp(request.getEmail(), request.getOtp(), OtpType.CHANGE_PASSWORD);
        
        if (!isValidForgot && !isValidChange) {
            throw new AppException("Invalid or expired OTP");
        }

        // 2. Tìm User và cập nhật mật khẩu
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) throw new AppException("Email not found");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 3. Xóa OTP (cả 2 loại nếu có)
        if (isValidForgot) {
            otpService.deleteOtp(request.getEmail(), OtpType.FORGOT_PASSWORD);
        }
        if (isValidChange) {
            otpService.deleteOtp(request.getEmail(), OtpType.CHANGE_PASSWORD);
        }

        return ApiResponse.success("Password reset successful", null);
    }

    @Override
    public ApiResponse<String> changePassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (!userRepository.existsByEmail(email)) {
            throw new AppException("Email not found");
        }
        
        String otp = otpService.generateAndStoreOtp(email, OtpType.CHANGE_PASSWORD);
        emailService.sendOtpEmail(email, otp);
        return ApiResponse.success("OTP sent to your email", null);
    }
}
