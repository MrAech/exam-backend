package com.aech.auth.exam_backend.auth.Controllers;

import com.aech.auth.exam_backend.DTOs.AdminAuditLogDto;
import com.aech.auth.exam_backend.DTOs.AdminOverviewDto;
import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Config.AppConstants;
import com.aech.auth.exam_backend.auth.Entities.AdminAuditLog;
import com.aech.auth.exam_backend.auth.Entities.Role;
import com.aech.auth.exam_backend.auth.Entities.RoleUpdateReq;
import com.aech.auth.exam_backend.auth.Entities.User;
import com.aech.auth.exam_backend.auth.Repos.AdminAuditLogRepo;
import com.aech.auth.exam_backend.auth.Repos.RoleRepo;
import com.aech.auth.exam_backend.auth.Repos.UserRepo;
import com.aech.auth.exam_backend.auth.Services.UserService;
import com.aech.auth.exam_backend.exam.Repos.ExamRepo;
import com.aech.auth.exam_backend.exam.Repos.SubmissionRepo;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('" + AppConstants.ADMIN_ROLE + "')")
@RequestMapping("/api/v1/admin")
public class AdminController {

  private final UserService UserService;
  private final UserRepo userRepo;
  private final RoleRepo roleRepo;
  private final AdminAuditLogRepo adminAuditLogRepo;
  private ExamRepo examRepo;
  private SubmissionRepo submissionRepo;

  @GetMapping("/users")
  public ResponseEntity<Iterable<UserDto>> getusers() {
    return ResponseEntity.ok(UserService.getAllUsers());
  }

  @PostMapping("/users/{userId}/role")
  public ResponseEntity<UserDto> updateUserRole(
      @PathVariable UUID userId,
      @RequestBody RoleUpdateReq req,
      Authentication authentication) {
    String roleInput = req.roleName() == null ? "" : req.roleName().trim();

    if (roleInput.isBlank()) {
      throw new IllegalArgumentException("RoleName is required");
    }

    String normalized = roleInput.toUpperCase(Locale.ROOT).replace("ROLE_", "");

    Role role = roleRepo.findByName("ROLE_" + normalized)
        .orElseThrow(() -> new IllegalArgumentException("Invalid Role: " + roleInput));

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not Found"));

    user.getRoles().add(role);
    userRepo.save(user);

    audit(authentication, "ROLE_ADD", user, role.getName());

    return ResponseEntity.ok(UserService.getUserById(userId.toString()));
  }

  @DeleteMapping("/users/{userId}/role/{roleName}")
  public ResponseEntity<UserDto> removeUserRole(
      @PathVariable UUID userId,
      @PathVariable String roleName,
      Authentication authentication) {

    String normalized = roleName.toUpperCase(Locale.ROOT).replace("ROLE_", "");
    String roleKey = "ROLE_" + normalized;

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not Found"));

    user.getRoles().removeIf(role -> roleKey.equals(role.getName()));

    if (user.getRoles().isEmpty()) {
      Role stuRole = roleRepo.findByName("ROLE_" + AppConstants.STUDENT_ROLE)
          .orElseThrow(() -> new IllegalArgumentException("Default Student Role missing"));
      user.getRoles().add(stuRole);
    }

    userRepo.save(user);
    audit(authentication, "ROLE_REMOVE", user, roleKey);
    return ResponseEntity.ok(UserService.getUserById(userId.toString()));
  }

  @PostMapping("/users/{userId}/suspend")
  public ResponseEntity<UserDto> suspendUser(@PathVariable UUID userId, Authentication authentication) {
    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User Not Found"));

    user.setEnable(false);
    userRepo.save(user);

    audit(authentication, "USER_SUSPEND", user, "enable=false");

    return ResponseEntity.ok(UserService.getUserById(userId.toString()));
  }

  @PostMapping("/users/{userId}/reactivate")
  public ResponseEntity<UserDto> reactivateUser(@PathVariable UUID userId, Authentication authentication) {

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not Found"));

    user.setEnable(true);
    userRepo.save(user);
    audit(authentication, "USER_REACTIVATE", user, "enable=true");
    return ResponseEntity.ok(UserService.getUserById(userId.toString()));
  }

  @GetMapping("/audit")
  public ResponseEntity<List<AdminAuditLogDto>> getAuditLogs() {
    List<AdminAuditLogDto> logs = adminAuditLogRepo.findTop100ByOrderByCreatedAtDesc()
        .stream()
        .map(
            log -> new AdminAuditLogDto(
                log.getId(),
                log.getActorEmail(),
                log.getAction(),
                log.getTargetUserId(),
                log.getTargetEmail(),
                log.getMetadata(),
                log.getCreatedAt()))
        .toList();

    return ResponseEntity.ok(logs);
  }

  @GetMapping
  public ResponseEntity<AdminOverviewDto> getOverview() {

    List<User> users = userRepo.findAll();

    long students = users.stream()
        .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_STUDENT".equals(r.getName())))
        .count();

    long teachers = users.stream()
        .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_TEACHER".equals(r.getName())))
        .count();

    long admins = users.stream()
        .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())))
        .count();

    AdminOverviewDto dto = new AdminOverviewDto(
        users.size(),
        students,
        teachers,
        admins,
        examRepo.count(),
        submissionRepo.count());

    return ResponseEntity.ok(dto);
  }

  private void audit(Authentication authentication, String action, User targetUser, String metadata) {

    String actor = authentication == null ? "unknown" : authentication.getName();

    adminAuditLogRepo.save(AdminAuditLog.builder()
        .actorEmail(actor)
        .action(action)
        .targetUserId(targetUser.getId())
        .targetEmail(targetUser.getEmail())
        .metadata(metadata)
        .createdAt(Instant.now())
        .build());
  }

}
