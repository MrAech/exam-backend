package com.aech.auth.exam_backend.auth.Controllers;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Config.AppConstants;
import com.aech.auth.exam_backend.auth.Entities.User;

import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.Apiversion.Use;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aech.auth.exam_backend.auth.Services.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@PreAuthorize("hasRole('" + AppConstants.ADMIN_ROLE + "')")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
  }

  @GetMapping
  public ResponseEntity<Iterable<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @DeleteMapping("/{userId}")
  public void deleteuser(@PathVariable String userId) {
    userService.deleteUser(userId);
  }

  @PutMapping("/{userId}")
  public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUser(userDto, userId));
  }

  @PreAuthorize("hasRole('" + AppConstants.ADMIN_ROLE + "')")
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUserId(@PathVariable String userId) {
    return ResponseEntity.ok(userService.getUserById(userId));
  }

}
