package com.secure.notes.services;

import com.secure.notes.dtos.UserDTO;
import com.secure.notes.entities.Role;
import com.secure.notes.entities.UserEntity;
import com.secure.notes.reponse.UserResponse;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void updateUserRole(Long userId, String roleName);

    List<UserResponse> getAllUsers();

    UserDTO getUserById(Long id);

    UserEntity findByUsername(String username);

    void updateAccountLockStatus(Long userId, boolean lock);

    List<Role> getAllRoles();

    void updateAccountExpiryStatus(Long userId, boolean expire);

    void updateAccountEnabledStatus(Long userId, boolean enabled);

    void updateCredentialsExpiryStatus(Long userId, boolean expire);

    void updatePassword(Long userId, String password);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

    Optional<UserEntity> findByEmail(String email);

    UserEntity registerUser(UserEntity user);

    GoogleAuthenticatorKey generate2FASecret(Long userId);

    boolean validate2FACode(Long userId, int code);

    void enable2FA(Long userId);

    void disable2FA(Long userId);
}
