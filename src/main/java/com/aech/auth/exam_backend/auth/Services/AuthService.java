package com.aech.auth.exam_backend.auth.Services;

import com.aech.auth.exam_backend.DTOs.UserDto;

public interface AuthService {

  UserDto registerUser(UserDto userDto);
}
