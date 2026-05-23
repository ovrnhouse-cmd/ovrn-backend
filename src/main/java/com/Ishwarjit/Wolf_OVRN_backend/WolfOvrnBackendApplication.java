package com.Ishwarjit.Wolf_OVRN_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WolfOvrnBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WolfOvrnBackendApplication.class, args);
	}

}
