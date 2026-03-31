package com.aech.auth.exam_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "security.jwt.secret=test-you-shall-not-for-pass-boi-loads-1234567890123456789q12124567890",
    "spring.security.oauth2.client.registration.google.client-id=test-google-client-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-google-client-secret"

})
class ExamBackendApplicationTests {

  @Test
  void contextLoads() {
  }

}
