package com.aech.auth.exam_backend.DTOs;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class RoleDto {

  private UUID id;

  private String name;

}
