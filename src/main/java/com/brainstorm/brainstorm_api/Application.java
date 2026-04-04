package com.brainstorm.brainstorm_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
	info = @Info(title = "Brainstorm API"),
	security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
	name = "Bearer Authentication",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		// Railway의 DATABASE_URL(postgres://...)을 JDBC 형식(jdbc:postgresql://...)으로 변환
		String databaseUrl = System.getenv("DATABASE_URL");
		if (databaseUrl != null) {
			// postgres:// → postgresql:// 변환 (JDBC는 postgresql만 인식)
			if (databaseUrl.startsWith("postgres://")) {
				databaseUrl = databaseUrl.replaceFirst("postgres://", "postgresql://");
			}
			// jdbc: 접두사 추가
			if (!databaseUrl.startsWith("jdbc:")) {
				databaseUrl = "jdbc:" + databaseUrl;
			}
			System.setProperty("JDBC_DATABASE_URL", databaseUrl);
		}

		SpringApplication.run(Application.class, args);
	}

}
