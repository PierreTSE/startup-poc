package fr.tse.poc.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class FillDatabase {

    @Bean
    @Profile("!test")
    CommandLineRunner initDatabase() {
        return args -> {
        };
    }

    @Bean
    @Profile("test")
    CommandLineRunner initTestDatabase() {
        return args -> {

        };
    }
}
