package com.secure.notes;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotesServiceTests {

    
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-bullseye");
}
