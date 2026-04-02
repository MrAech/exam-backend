package com.aech.auth.exam_backend;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.aech.auth.exam_backend.DTOs.RoleDto;
import com.aech.auth.exam_backend.auth.Config.AppConstants;
import com.aech.auth.exam_backend.auth.Entities.Role;
import com.aech.auth.exam_backend.auth.Repos.RoleRepo;

@SpringBootApplication
public class ExamBackendApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ExamBackendApplication.class, args);
  }

  @Autowired
  private RoleRepo roleRepo;

  @Override
  public void run(String... args) throws Exception {
    roleRepo.findByName("ROLE_" + AppConstants.ADMIN_ROLE).ifPresentOrElse(role -> {
      System.out.println("Admin Role alreadt exists: " + role.getName());
    }, () -> {
      Role role = new Role();
      role.setName("ROLE_" + AppConstants.ADMIN_ROLE);
      role.setId(UUID.randomUUID());
      roleRepo.save(role);
    });
    roleRepo.findByName("ROLE_" + AppConstants.STUDENT_ROLE).ifPresentOrElse(role -> {
      System.out.println("Student Role already exists: " + role.getName());
    }, () -> {
      Role role = new Role();
      role.setName("ROLE_" + AppConstants.STUDENT_ROLE);
      role.setId(UUID.randomUUID());
      roleRepo.save(role);
    });

    roleRepo.findByName("ROLE_" + AppConstants.TEACHER_ROLE).ifPresentOrElse(role -> {
      System.out.println("Teacher Role already exists: " + role.getName());
    }, () -> {
      Role role = new Role();
      role.setName("ROLE_" + AppConstants.TEACHER_ROLE);
      role.setId(UUID.randomUUID());
      roleRepo.save(role);
    });

  }
}
