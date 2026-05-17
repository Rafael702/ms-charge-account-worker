package com.itau.chargeaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargeAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargeAccountApplication.class, args);
    }
}

