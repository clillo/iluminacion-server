package cl.clillo.lighting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"cl.clillo.lighting.controller"})
public class LightingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightingServerApplication.class, args);
	}

}
