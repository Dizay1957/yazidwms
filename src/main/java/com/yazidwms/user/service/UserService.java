package com.yazidwms.user.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.role.entity.Role;
import com.yazidwms.role.entity.RoleName;
import com.yazidwms.role.repository.RoleRepository;
import com.yazidwms.security.SecurityUtils;
import com.yazidwms.user.dto.UserDtos.PasswordUpdateRequest;
import com.yazidwms.user.dto.UserDtos.ProfileUpdateRequest;
import com.yazidwms.user.dto.UserDtos.UserRequest;
import com.yazidwms.user.dto.UserDtos.UserResponse;
import com.yazidwms.user.dto.UserDtos.UserUpdateRequest;
import com.yazidwms.user.entity.User;
import com.yazidwms.user.entity.UserStatus;
import com.yazidwms.user.mapper.UserMapper;
import com.yazidwms.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        ensureEmailAvailable(request.email());
        validatePassword(request.password());
        var user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(resolveRoles(request.roles()));
        var saved = userRepository.save(user);
        auditService.log("USER_CREATED", "User", saved.getId(), "User created by admin", null);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String q, Pageable pageable) {
        var page = (q == null || q.isBlank())
                ? userRepository.findByDeletedFalse(pageable)
                : userRepository.findByDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return userMapper.toResponse(findActive(id));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        var user = findActive(id);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRoles(resolveRoles(request.roles()));
        if (request.status() != null) {
            user.setStatus(request.status());
            user.setActive(request.status() == UserStatus.ACTIVE);
        }
        auditService.log("USER_UPDATED", "User", user.getId(), "User details updated", null);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void disable(Long id) {
        var user = findActive(id);
        user.setStatus(UserStatus.DISABLED);
        user.setActive(false);
        auditService.log("USER_DISABLED", "User", id, "User disabled", null);
    }

    @Transactional
    public void enable(Long id) {
        var user = findExisting(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        user.setDeleted(false);
        auditService.log("USER_ENABLED", "User", id, "User enabled", null);
    }

    @Transactional
    public void softDelete(Long id) {
        var user = findExisting(id);
        user.setDeleted(true);
        user.setActive(false);
        user.setStatus(UserStatus.DISABLED);
        auditService.log("USER_DELETED", "User", id, "User soft deleted", null);
    }

    @Transactional
    public UserResponse updateProfile(ProfileUpdateRequest request) {
        var current = currentUser();
        current.setFullName(request.fullName());
        current.setPhone(request.phone());
        auditService.log("PROFILE_UPDATED", "User", current.getId(), "Profile updated", null);
        return userMapper.toResponse(current);
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        var current = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), current.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        validatePassword(request.newPassword());
        current.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        auditService.log("PASSWORD_UPDATED", "User", current.getId(), "Password changed", null);
    }

    public User findActive(Long id) {
        var user = findExisting(id);
        if (user.isDeleted()) {
            throw new NotFoundException("User", id);
        }
        return user;
    }

    public User currentUser() {
        var user = SecurityUtils.currentUserOrNull();
        if (user == null) {
            throw new BusinessException("Authenticated user is required");
        }
        return findActive(user.getId());
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < 10 || password.chars().noneMatch(Character::isDigit) || password.chars().noneMatch(Character::isUpperCase)) {
            throw new BusinessException("Password must be at least 10 characters and contain an uppercase letter and a digit");
        }
    }

    private User findExisting(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email is already used");
        }
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName).orElseThrow(() -> new BusinessException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }
}
