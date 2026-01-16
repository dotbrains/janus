package com.dotbrains.janus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@Slf4j
public class JanusApplication {

    public static void main(String[] args) {
        log.info("Starting Janus - Federated Authentication Service");
        log.info("Named after the Roman god of gates, transitions, and beginnings");
        
        SpringApplication.run(JanusApplication.class, args);
        
        log.info("Janus is now running and ready to authenticate users!");
    }
}
