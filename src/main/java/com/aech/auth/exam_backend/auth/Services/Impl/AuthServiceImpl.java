package com.aech.auth.exam_backend.auth.Services.Impl;

import org.springframework.stereotype.Service;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Services.AuthService;
import com.aech.auth.exam_backend.auth.Services.UserService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;

  @Override
  public UserDto registerUser(UserDto userDto) {

    return userService.createUser(userDto);
  }
}
