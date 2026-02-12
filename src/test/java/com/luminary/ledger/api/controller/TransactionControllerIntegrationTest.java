package com.luminary.ledger.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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
class TransactionControllerIntegrationTest {

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

    private Long assetAccountId;
    private Long revenueAccountId;

    @BeforeEach
    void setUp() throws Exception {
        String assetResp = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxAsset-%d", "type": "ASSET"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assetAccountId = JsonPath.parse(assetResp).read("$.id", Long.class);

        String revenueResp = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxRevenue-%d", "type": "REVENUE"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        revenueAccountId = JsonPath.parse(revenueResp).read("$.id", Long.class);
    }

    @Test
    void createTransaction_balanced_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Service revenue",
                                  "date": "2025-03-01T09:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 250.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 250.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Service revenue"))
                .andExpect(jsonPath("$.date").value("2025-03-01T09:00:00"))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(250.00))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getTransaction_returnsOk() throws Exception {
        String txResp = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Get test",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long txId = JsonPath.parse(txResp).read("$.id", Long.class);

        mockMvc.perform(get("/api/transactions/{id}", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.description").value("Get test"))
                .andExpect(jsonPath("$.entries", hasSize(2)));
    }

    @Test
    void getTransaction_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTransaction_unbalanced_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Unbalanced",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 50.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTransaction_singleEntry_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Single entry",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(assetAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_emptyEntries_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "No entries",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_missingDescription_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_missingDate_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "No date",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_nonExistentAccount_returns404() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Bad account",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": 999999, "type": "DEBIT", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(revenueAccountId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_invalidEntryType_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Invalid type",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "INVALID", "amount": 100.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 100.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_zeroAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Zero amount",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 0},
                                    {"accountId": %d, "type": "CREDIT", "amount": 0}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_updatesAccountBalances() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Balance check",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 500.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 500.00}
                                  ]
                                }
                                """.formatted(assetAccountId, revenueAccountId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/accounts/{id}", assetAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));

        mockMvc.perform(get("/api/accounts/{id}", revenueAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));
    }

    @Test
    void createTransaction_multipleEntries_balanced() throws Exception {
        String expenseResp = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TxExpense-%d", "type": "EXPENSE"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long expenseAccountId = JsonPath.parse(expenseResp).read("$.id", Long.class);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Split payment",
                                  "date": "2025-03-01T10:00:00",
                                  "entries": [
                                    {"accountId": %d, "type": "DEBIT", "amount": 300.00},
                                    {"accountId": %d, "type": "DEBIT", "amount": 200.00},
                                    {"accountId": %d, "type": "CREDIT", "amount": 500.00}
                                  ]
                                }
                                """.formatted(assetAccountId, expenseAccountId, revenueAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entries", hasSize(3)))
                .andExpect(jsonPath("$.totalAmount").value(500.00));
    }
}
