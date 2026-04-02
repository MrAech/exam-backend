package com.aech.auth.exam_backend.auth.Services.Impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aech.auth.exam_backend.auth.Repos.UserRepo;
import com.aech.auth.exam_backend.auth.Services.UserService;
import com.aech.auth.exam_backend.auth.Entities.User;
import com.aech.auth.exam_backend.auth.Config.AppConstants;
import com.aech.auth.exam_backend.auth.Entities.Provider;
import com.aech.auth.exam_backend.auth.Entities.Role;

import jakarta.transaction.Transactional;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.Exceptions.ResourceNotFoundEx;
import com.aech.auth.exam_backend.auth.Repos.RoleRepo;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepo userRepo;
  private final ModelMapper mapper;

  private final RoleRepo roleRepo;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto createUser(@NonNull UserDto userDto) {

    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    if (userRepo.existsByEmail(userDto.getEmail())) {
      throw new IllegalArgumentException("USer with given email already exists");
    }

    User user = mapper.map(userDto, User.class);

    if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    }

    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

    Role role = roleRepo.findByName("ROLE_" + AppConstants.STUDENT_ROLE)
        .orElseThrow(() -> new IllegalArgumentException("Default student role missing"));

    user.setRoles(new HashSet<>());
    user.getRoles().add(role);

    User savedUser = userRepo.save(user);
    return mapper.map(savedUser, UserDto.class);
  }

  @Override
  public UserDto getUserByEmail(@NonNull String email) {

    User user = userRepo
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundEx("User not found with given email id "));
    return mapper.map(user, UserDto.class);
  }

  @Override
  public UserDto updateUser(@NonNull UserDto userDto, @NonNull String userId) {

    UUID uId = UUID.fromString(userId);

    User existingUser = userRepo
        .findById(uId)
        .orElseThrow(() -> new ResourceNotFoundEx("User not found with given id"));

    if (userDto.getName() != null) {
      existingUser.setName(userDto.getName());
    }
    if (userDto.getProvider() != null)
      existingUser.setProvider(userDto.getProvider());
    if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
      existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
    }

    existingUser.setEnable(userDto.isEnable());
    existingUser.setUpdatedAt(Instant.now());

    User updatedUser = userRepo.save(existingUser);

    return mapper.map(updatedUser, UserDto.class);
  }

  @Override
  public void deleteUser(@NonNull String userId) {
    UUID uId = UUID.fromString(userId);
    User user = userRepo.findById(uId)
        .orElseThrow(() -> new ResourceNotFoundEx("User not found with given id"));
    userRepo.delete(Objects.requireNonNull(user));
  }

  @Override
  public UserDto getUserById(@NonNull String userId) {
    User user = userRepo.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundEx("User not found with given id"));
    return mapper.map(user, UserDto.class);
  }

  @Override
  @Transactional
  public Iterable<UserDto> getAllUsers() {
    return userRepo
        .findAll()
        .stream()
        .map(user -> mapper.map(user, UserDto.class))
        .toList();
  }
}
