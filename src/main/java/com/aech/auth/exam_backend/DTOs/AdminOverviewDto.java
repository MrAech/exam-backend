package com.aech.auth.exam_backend.DTOs;

public record AdminOverviewDto(
    long totalUsers,
    long totalStudents,
    long totalTeachers,
    long totalAdmins,
    long totalExams,
    long totalSubmissions) {
}
