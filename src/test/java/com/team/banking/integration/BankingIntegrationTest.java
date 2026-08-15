package com.team.banking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BankingIntegrationTest {

    // ============================================================
    // TEST DATABASE
    // ============================================================

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("banking_test")
                    .withUsername("postgres")
                    .withPassword("root");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String accountsUrl() {
        return baseUrl() + "/api/accounts";
    }

    /**
     * Creates an account through the actual REST API
     * and extracts the generated account ID from the response.
     */
    private String createAccount(double initialBalance) {

        String customerName =
                "Test User " + UUID.randomUUID();

        Map<String, Object> request = Map.of(
                "customerName", customerName,
                "accountType", "SAVINGS",
                "initialBalance", initialBalance
        );

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        accountsUrl(),
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        String body = response.getBody();

        /*
         * Expected response:
         *
         * Account created successfully.
         * Account ID: <uuid>
         */

        String prefix = "Account ID: ";

        int index = body.indexOf(prefix);

        assertTrue(
                index >= 0,
                "Account ID should be present in response"
        );

        return body.substring(index + prefix.length()).trim();
    }


    /**
     * Waits until the query side contains the account.
     *
     * Axon projections can be processed asynchronously,
     * so the read model may need a short amount of time
     * to catch up with the command side.
     */
    private ResponseEntity<Map> waitForAccount(
            String accountId) throws InterruptedException {

        String url =
                accountsUrl() + "/" + accountId;

        for (int i = 0; i < 20; i++) {

            ResponseEntity<Map> response =
                    restTemplate.getForEntity(
                            url,
                            Map.class
                    );

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {

                return response;
            }

            Thread.sleep(500);
        }

        return restTemplate.getForEntity(
                url,
                Map.class
        );
    }


    private void deposit(
            String accountId,
            double amount) {

        Map<String, Object> request =
                Map.of("amount", amount);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/deposit",
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );
    }


    private void withdraw(
            String accountId,
            double amount) {

        Map<String, Object> request =
                Map.of("amount", amount);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        request,
                        jsonHeaders()
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/withdraw",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );
    }


    private HttpHeaders jsonHeaders() {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        return headers;
    }


    // ============================================================
    // 1. TESTCONTAINER
    // ============================================================

    @Test
    void applicationStartsWithTestcontainerDatabase() {

        assertTrue(
                postgres.isRunning(),
                "PostgreSQL Testcontainer should be running"
        );
    }


    // ============================================================
    // 2. OPEN ACCOUNT
    // ============================================================

    @Test
    void shouldCreateAccount() {

        String accountId =
                createAccount(5000);

        assertNotNull(accountId);
        assertFalse(accountId.isBlank());
    }


    @Test
    void shouldCreateAccountWithZeroBalance()
            throws InterruptedException {

        String accountId =
                createAccount(0);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                0.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );
    }


    @Test
    void shouldCreateAccountWithInitialBalance()
            throws InterruptedException {

        String accountId =
                createAccount(10000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                10000.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );

        assertEquals(
                "ACTIVE",
                response.getBody().get("status")
        );
    }


    @Test
    void shouldRejectNegativeInitialBalance() {

        Map<String, Object> request =
                Map.of(
                        "customerName",
                        "Invalid User",
                        "accountType",
                        "SAVINGS",
                        "initialBalance",
                        -1000
                );

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        accountsUrl(),
                        request,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    // ============================================================
    // 3. GET ACCOUNT
    // ============================================================

    @Test
    void shouldGetAccountById()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                accountId,
                response.getBody().get("accountId")
        );
    }


    @Test
    void shouldReturnActiveStatusForNewAccount()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                "ACTIVE",
                response.getBody().get("status")
        );
    }


    @Test
    void shouldReturnAccountType()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                "SAVINGS",
                response.getBody().get("accountType")
        );
    }


    // ============================================================
    // 4. GET ALL ACCOUNTS
    // ============================================================

    @Test
    void shouldGetAllAccounts() {

        createAccount(1000);

        ResponseEntity<Object[]> response =
                restTemplate.getForEntity(
                        accountsUrl(),
                        Object[].class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().length >= 1
        );
    }


    @Test
    void shouldReturnMultipleAccounts() {

        createAccount(1000);
        createAccount(2000);

        ResponseEntity<Object[]> response =
                restTemplate.getForEntity(
                        accountsUrl(),
                        Object[].class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertTrue(
                response.getBody().length >= 2
        );
    }


    // ============================================================
    // 5. DEPOSIT
    // ============================================================

    @Test
    void shouldDepositMoney()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        deposit(accountId, 2000);

        Thread.sleep(1000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                7000.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );
    }


    @Test
    void shouldDepositMultipleTimes()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        deposit(accountId, 1000);
        deposit(accountId, 2000);

        Thread.sleep(1000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                8000.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );
    }


    @Test
    void shouldRejectZeroDeposit() {

        String accountId =
                createAccount(5000);

        Map<String, Object> request =
                Map.of("amount", 0);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/deposit",
                        request,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    @Test
    void shouldRejectNegativeDeposit() {

        String accountId =
                createAccount(5000);

        Map<String, Object> request =
                Map.of("amount", -500);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/deposit",
                        request,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    // ============================================================
    // 6. WITHDRAW
    // ============================================================

    @Test
    void shouldWithdrawMoney()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        withdraw(accountId, 2000);

        Thread.sleep(1000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                3000.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );
    }


    @Test
    void shouldWithdrawEntireBalance()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        withdraw(accountId, 5000);

        Thread.sleep(1000);

        ResponseEntity<Map> response =
                waitForAccount(accountId);

        assertEquals(
                0.0,
                ((Number) response.getBody()
                        .get("balance")).doubleValue()
        );
    }


    @Test
    void shouldRejectWithdrawalGreaterThanBalance() {

        String accountId =
                createAccount(5000);

        Map<String, Object> request =
                Map.of("amount", 6000);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        request,
                        jsonHeaders()
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/withdraw",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    @Test
    void shouldRejectZeroWithdrawal() {

        String accountId =
                createAccount(5000);

        Map<String, Object> request =
                Map.of("amount", 0);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        request,
                        jsonHeaders()
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/withdraw",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    @Test
    void shouldRejectNegativeWithdrawal() {

        String accountId =
                createAccount(5000);

        Map<String, Object> request =
                Map.of("amount", -100);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        request,
                        jsonHeaders()
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/withdraw",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    // ============================================================
    // 7. TRANSACTION HISTORY
    // ============================================================

    @Test
    void shouldReturnTransactionHistory()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        deposit(accountId, 1000);

        Thread.sleep(1500);

        ResponseEntity<Object[]> response =
                restTemplate.getForEntity(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/transactions",
                        Object[].class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().length >= 2,
                "Expected account-opened and deposit transactions"
        );
    }


    @Test
    void shouldRecordDepositTransaction()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        deposit(accountId, 1500);

        Thread.sleep(1500);

        ResponseEntity<Object[]> response =
                restTemplate.getForEntity(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/transactions",
                        Object[].class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertTrue(
                response.getBody().length >= 2
        );
    }


    // ============================================================
    // 8. CLOSE ACCOUNT
    // ============================================================

    @Test
    void shouldRejectClosingAccountWithBalance() {

        String accountId =
                createAccount(5000);

        HttpEntity<Void> entity =
                new HttpEntity<>(jsonHeaders());

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/close",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertTrue(
                response.getStatusCode().is5xxServerError()
                        || response.getStatusCode().is4xxClientError()
        );
    }


    @Test
    void shouldCloseAccountWithZeroBalance()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        withdraw(accountId, 5000);

        Thread.sleep(1000);

        HttpEntity<Void> entity =
                new HttpEntity<>(jsonHeaders());

        ResponseEntity<String> response =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/close",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        Thread.sleep(1000);

        ResponseEntity<Map> account =
                waitForAccount(accountId);

        assertEquals(
                "CLOSED",
                account.getBody().get("status")
        );
    }


    @Test
    void shouldRejectClosingAlreadyClosedAccount()
            throws InterruptedException {

        String accountId =
                createAccount(5000);

        withdraw(accountId, 5000);

        Thread.sleep(1000);

        HttpEntity<Void> entity =
                new HttpEntity<>(jsonHeaders());

        ResponseEntity<String> firstClose =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/close",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                firstClose.getStatusCode()
        );

        Thread.sleep(1000);

        ResponseEntity<String> secondClose =
                restTemplate.exchange(
                        accountsUrl()
                                + "/"
                                + accountId
                                + "/close",
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertTrue(
                secondClose.getStatusCode().is5xxServerError()
                        || secondClose.getStatusCode().is4xxClientError()
        );
    }
}