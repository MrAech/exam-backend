package com.aech.auth.exam_backend.DTOs;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoleDto {

  private UUID id;

  private String name;

}
