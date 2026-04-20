package org.craftedcode.backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"de.frachtwerk.essencium.backend", "org.craftedcode.backend"})
@EntityScan(basePackages = {"de.frachtwerk.essencium.backend", "org.craftedcode.backend"})
@ConfigurationPropertiesScan(basePackages = {"de.frachtwerk.essencium.backend", "org.craftedcode.backend"})
@EnableJpaRepositories(basePackages = {"de.frachtwerk.essencium.backend", "org.craftedcode.backend"})
@OpenAPIDefinition(servers = {@Server(url = "${app.url:localhost:8098}")})
public class SpringBootApp {
  public static void main(String[] args) {
    SpringApplication.run(SpringBootApp.class, args);
  }
}