package com.aech.auth.exam_backend.auth.Services;

import com.aech.auth.exam_backend.DTOs.UserDto;

import lombok.NonNull;

public interface UserService {

  UserDto createUser(@NonNull UserDto userDto);

  UserDto getUserByEmail(@NonNull String email);

  UserDto updateUser(@NonNull UserDto userDto, @NonNull String userId);

  void deleteUser(@NonNull String userId);

  UserDto getUserById(@NonNull String userId);

  Iterable<UserDto> getAllUsers();
}
