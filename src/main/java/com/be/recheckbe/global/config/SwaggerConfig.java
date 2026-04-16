package com.be.recheckbe.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    String securitySchemeName = "bearerAuth";

    Server localServer = new Server();
    localServer.setUrl("http://localhost:8080");
    localServer.setDescription("🛠️ 로컬 서버");

    Server prodServer = new Server();
    prodServer.setUrl("https://api.reajoucheck.site");
    prodServer.setDescription("🚀 운영 서버");

    return new OpenAPI()
        .addServersItem(localServer)
        .addServersItem(prodServer)
        .info(new Info().title("ReCheck API 명세서").version("1.0").description("recheck-api-docs"))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }

  @Bean
  public GroupedOpenApi customGroupedOpenApi() {
    return GroupedOpenApi.builder().group("api").pathsToMatch("/**").build();
  }
}
