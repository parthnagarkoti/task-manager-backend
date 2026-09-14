package com.taskmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Task Management Backend application.
 *
 * @SpringBootApplication combines:
 *   - @Configuration       : marks this as a source of bean definitions
 *   - @EnableAutoConfiguration : tells Spring Boot to auto-configure based on classpath
 *   - @ComponentScan       : scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class TaskManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }

}
