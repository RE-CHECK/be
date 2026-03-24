package com.be.recheckbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RecheckBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(RecheckBeApplication.class, args);
  }

}
