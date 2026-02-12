package com.springSecurity.authify.service;

import com.springSecurity.authify.entity.UserEntity;
import com.springSecurity.authify.io.ProfileRequest;
import com.springSecurity.authify.io.ProfileResponse;
import com.springSecurity.authify.repository.UserRepository;
import com.springSecurity.authify.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if (!userRepository.existsByEmail(request.getEmail())) {
            // save user
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");


    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found: " + email));

        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //calculate expiry time (current time + 24 hr in ms)
        long expiryTime = System.currentTimeMillis()+(15 * 60 * 1000);

        //update profile/user
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpiredAt(expiryTime);

        //save into database
        userRepository.save(existingEntity);
        try
        {
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp))
        {
            throw new RuntimeException("Invaild OTP");
        }
        if (existingUser.getResetOtpExpiredAt() < System.currentTimeMillis())
        {
            throw new RuntimeException("OTP Expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpiredAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found: " + email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified())
        {
            return;
        }
        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //calculate expiry time (current time + 24 hr in ms)
        long expiryTime = System.currentTimeMillis()+(24 * 60 * 60 * 1000);

        //Update user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiredAt(expiryTime);

        //save in db
        userRepository.save(existingUser);

        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp))
        {
            throw new RuntimeException("Invaild OTP");
        }

        if (existingUser.getVerifyOtpExpiredAt() < System.currentTimeMillis())
        {
            throw new RuntimeException("Expired OTP");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpiredAt(0L);
        existingUser.setResetOtp(null); // Cleanup

        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail().trim().toLowerCase())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword())) // 🔥 REQUIRED
                .isAccountVerified(false)
                .resetOtpExpiredAt(0L)
                .verifyOtp(null)
                .verifyOtpExpiredAt(0L)
                .resetOtp(null)
                .build();
    }


}
