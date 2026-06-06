package org.example.consultation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.example.common.feign")
public class ConsultationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultationServiceApplication.class, args);
    }
}
