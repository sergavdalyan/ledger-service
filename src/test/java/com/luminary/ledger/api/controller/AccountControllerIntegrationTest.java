package com.luminary.ledger.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AccountControllerIntegrationTest {


    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccount_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Cash", "type": "ASSET"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Cash"))
                .andExpect(jsonPath("$.type").value("ASSET"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createAccount_duplicateName_returnsConflict() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "DuplicateTest", "type": "ASSET"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "DuplicateTest", "type": "ASSET"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createAccount_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "type": "ASSET"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_missingType_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "SomeAccount"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_invalidType_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "BadType", "type": "INVALID"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid account type")));
    }

    @Test
    void createAccount_caseInsensitiveType() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Revenue Account", "type": "revenue"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("REVENUE"));
    }

    @Test
    void getAccount_returnsOk() throws Exception {
        String response = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "GetTest", "type": "LIABILITY"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("GetTest"))
                .andExpect(jsonPath("$.type").value("LIABILITY"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }


    @Test
    void getAllAccounts_returnsPaginatedList() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "ListTest", "type": "EXPENSE"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }


    @Test
    void getAccountTransactions_emptyList() throws Exception {
        String response = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxnTestEmpty", "type": "ASSET"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/accounts/{id}/transactions", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAccountTransactions_returnsTransactions() throws Exception {
        // Create two accounts
        String cashResp = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxnCash", "type": "ASSET"}
                                """))
                .andReturn().getResponse().getContentAsString();
        Long cashId = com.jayway.jsonpath.JsonPath.parse(cashResp).read("$.id", Long.class);

        String revenueResp = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxnRevenue", "type": "REVENUE"}
                                """))
                .andReturn().getResponse().getContentAsString();
        Long revenueId = com.jayway.jsonpath.JsonPath.parse(revenueResp).read("$.id", Long.class);

        // Create a transaction
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Sale",
                                  "date": "2025-01-15T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(cashId, revenueId)))
                .andExpect(status().isCreated());

        // Both accounts should see the transaction
        mockMvc.perform(get("/api/accounts/{id}/transactions", cashId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description").value("Sale"))
                .andExpect(jsonPath("$.content[0].entries", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/accounts/{id}/transactions", revenueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getAccountTransactions_nonExistentAccount_returns404() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}/transactions", 999999))
                .andExpect(status().isNotFound());
    }
}
