# Event-Driven Banking System with CQRS & Event Sourcing

A backend banking application built using **Java, Spring Boot, Axon Framework, Axon Server, PostgreSQL, CQRS, and Event Sourcing**.

The system supports core banking operations such as account creation, deposits, withdrawals, account closure, money transfers, and transaction history. The application separates write operations from read operations while maintaining a history of domain events.

---

## Table of Contents

* [Overview](#overview)
* [Objectives](#objectives)
* [Key Features](#key-features)
* [Technology Stack](#technology-stack)
* [Architecture](#architecture)
* [CQRS Flow](#cqrs-flow)
* [Event Sourcing](#event-sourcing)
* [Project Structure](#project-structure)
* [Banking Operations](#banking-operations)
* [Domain Commands](#domain-commands)
* [Domain Events](#domain-events)
* [Aggregates](#aggregates)
* [Query Side](#query-side)
* [Transfer Saga](#transfer-saga)
* [Database](#database)
* [API Documentation](#api-documentation)
* [Postman Testing](#postman-testing)
* [Validation and Business Rules](#validation-and-business-rules)
* [Integration Testing](#integration-testing)
* [Axon Server Setup](#axon-server-setup)
* [PostgreSQL Setup](#postgresql-setup)
* [Configuration](#configuration)
* [How to Run](#how-to-run)
* [Event Flow](#event-flow)
* [Advantages](#advantages)
* [Trade-offs](#trade-offs)
* [Future Enhancements](#future-enhancements)
* [Contributors](#contributors)

---

# Overview

The **Event-Driven Banking System** is a backend application designed to demonstrate how **CQRS (Command Query Responsibility Segregation)** and **Event Sourcing** can be applied to a banking domain.

Instead of directly modifying database records when a banking operation occurs, the command side processes business operations through Axon Framework and generates domain events.

Examples of domain events include:

* `AccountOpenedEvent`
* `MoneyDepositedEvent`
* `MoneyWithdrawnEvent`
* `TransferStartedEvent`
* `TransferCompletedEvent`
* `AccountClosedEvent`

These events are stored through the Axon infrastructure and are consumed by event handlers that update the PostgreSQL read model.

The application therefore maintains two logical responsibilities:

```text
Command Side
    ↓
Process banking operations
    ↓
Generate domain events

Query Side
    ↓
Consume domain events
    ↓
Build PostgreSQL read models
    ↓
Serve query APIs
```

---

# Objectives

The main objectives of the project are:

* Implement a banking system using an event-driven architecture.
* Demonstrate CQRS using Axon Framework.
* Implement Event Sourcing for banking state changes.
* Maintain a separate PostgreSQL read model.
* Maintain transaction history based on domain events.
* Implement account lifecycle operations.
* Implement deposits and withdrawals.
* Implement money transfers using an Axon Saga.
* Provide REST APIs for commands and queries.
* Demonstrate event-based synchronization between command and query models.
* Provide integration testing using Testcontainers.

---

# Key Features

## Account Management

* Open a bank account.
* View account details.
* View all accounts.
* Close an account.
* Prevent closing an account with a non-zero balance.
* Prevent closing an already closed account.

## Money Management

* Deposit money.
* Withdraw money.
* Prevent zero or negative deposits.
* Prevent zero or negative withdrawals.
* Prevent withdrawals greater than the available balance.
* Maintain updated account balances.

## Money Transfer

* Transfer money between two accounts.
* Use a dedicated Transfer Aggregate.
* Use an Axon Saga to coordinate withdrawal and deposit operations.
* Generate a unique transfer ID.
* Track transfer-related transactions.

## Transaction History

The system maintains a transaction read model containing:

* Transaction ID
* Account ID
* Transaction type
* Amount
* Balance after transaction
* Related account ID
* Transfer ID
* Timestamp

---

# Technology Stack

| Technology        | Version / Usage                 |
| ----------------- | ------------------------------- |
| Java              | 21                              |
| Spring Boot       | 3.5.5                           |
| Spring Framework  | 6.2.x                           |
| Axon Framework    | 4.13.2                          |
| Axon Server       | 2026.0.5                        |
| PostgreSQL        | Read Model / Database           |
| Hibernate ORM     | JPA persistence                 |
| Maven             | Build and dependency management |
| Spring Web        | REST APIs                       |
| Spring Data JPA   | Database access                 |
| Spring Validation | Request validation              |
| Testcontainers    | Integration testing             |
| JUnit             | Testing                         |
| Postman           | API testing                     |
| IntelliJ IDEA     | Development environment         |

---

# Architecture

The application follows **CQRS + Event Sourcing**.

High-level architecture:

```text
                         ┌─────────────────────┐
                         │       Client        │
                         │      / Postman      │
                         └──────────┬──────────┘
                                    │
                   ┌────────────────┴────────────────┐
                   │                                 │
                   ▼                                 ▼
          ┌─────────────────┐              ┌─────────────────┐
          │ Command         │              │ Query           │
          │ Controller      │              │ Controller      │
          └────────┬────────┘              └────────┬────────┘
                   │                                │
                   ▼                                │
          ┌─────────────────┐                       │
          │ Command         │                       │
          │ Gateway         │                       │
          └────────┬────────┘                       │
                   │                                │
                   ▼                                │
          ┌─────────────────┐                       │
          │   Axon Server   │                       │
          └────────┬────────┘                       │
                   │                                │
                   ▼                                │
          ┌─────────────────┐                       │
          │    Aggregate    │                       │
          └────────┬────────┘                       │
                   │                                │
                   ▼                                │
          ┌─────────────────┐                       │
          │ Domain Event    │                       │
          └────────┬────────┘                       │
                   │                                │
                   ▼                                │
          ┌─────────────────┐                       │
          │   Event Store   │                       │
          └────────┬────────┘                       │
                   │                                │
                   ▼                                │
          ┌──────────────────────────┐              │
          │ Event Handlers /         │              │
          │ Projections              │              │
          └────────────┬─────────────┘              │
                       │                            │
                       ▼                            │
                ┌───────────────┐                   │
                │  PostgreSQL   │◄──────────────────┘
                │  Read Model   │
                └───────────────┘
```

---

# CQRS Flow

## Command Side

Commands represent an intention to perform an operation.

Example:

```text
POST /api/accounts/{accountId}/deposit
```

Flow:

```text
REST Request
     ↓
AccountCommandController
     ↓
DepositMoneyCommand
     ↓
CommandGateway
     ↓
Axon Server
     ↓
AccountAggregate
     ↓
Business Validation
     ↓
MoneyDepositedEvent
```

---

## Query Side

Queries retrieve information from the PostgreSQL read model.

Flow:

```text
GET /api/accounts/{accountId}
     ↓
AccountQueryController
     ↓
QueryGateway
     ↓
GetAccountByIdQuery
     ↓
AccountQueryHandler
     ↓
AccountRepository
     ↓
PostgreSQL
     ↓
AccountResponse
```

---

# Event Sourcing

In this project, important banking state changes are represented as events.

For example, an account can have the following event history:

```text
AccountOpenedEvent
        ↓
MoneyDepositedEvent
        ↓
MoneyDepositedEvent
        ↓
MoneyWithdrawnEvent
        ↓
AccountClosedEvent
```

The aggregate state can be reconstructed by replaying these events.

For example:

```text
Initial Balance = 5000

AccountOpenedEvent       → 5000
MoneyDepositedEvent 1000 → 6000
MoneyWithdrawnEvent 500  → 5500
```

The system therefore maintains the history of state-changing operations rather than depending only on the current balance.

---

# Project Structure

```text
banking-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/team/banking/
│   │   │
│   │   │   ├── command/
│   │   │   │   ├── aggregate/
│   │   │   │   │   ├── AccountAggregate.java
│   │   │   │   │   └── TransferAggregate.java
│   │   │   │   │
│   │   │   │   ├── commands/
│   │   │   │   │   ├── OpenAccountCommand.java
│   │   │   │   │   ├── DepositMoneyCommand.java
│   │   │   │   │   ├── WithdrawMoneyCommand.java
│   │   │   │   │   ├── CloseAccountCommand.java
│   │   │   │   │   ├── TransferMoneyCommand.java
│   │   │   │   │   └── CompleteTransferCommand.java
│   │   │   │   │
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AccountCommandController.java
│   │   │   │   │   └── TransferController.java
│   │   │   │   │
│   │   │   │   └── dto/
│   │   │   │       ├── OpenAccountRequest.java
│   │   │   │       ├── DepositMoneyRequest.java
│   │   │   │       ├── WithdrawMoneyRequest.java
│   │   │   │       └── TransferMoneyRequest.java
│   │   │   │
│   │   │   ├── event/
│   │   │   │   └── events/
│   │   │   │       ├── AccountOpenedEvent.java
│   │   │   │       ├── MoneyDepositedEvent.java
│   │   │   │       ├── MoneyWithdrawnEvent.java
│   │   │   │       ├── AccountClosedEvent.java
│   │   │   │       ├── TransferStartedEvent.java
│   │   │   │       └── TransferCompletedEvent.java
│   │   │   │
│   │   │   ├── query/
│   │   │   │   ├── controller/
│   │   │   │   │   └── AccountQueryController.java
│   │   │   │   │
│   │   │   │   ├── dto/
│   │   │   │   │   ├── AccountResponse.java
│   │   │   │   │   └── TransactionResponse.java
│   │   │   │   │
│   │   │   │   ├── entity/
│   │   │   │   │   ├── AccountEntity.java
│   │   │   │   │   └── TransactionEntity.java
│   │   │   │   │
│   │   │   │   ├── handler/
│   │   │   │   │   ├── AccountQueryHandler.java
│   │   │   │   │   └── TransactionQueryHandler.java
│   │   │   │   │
│   │   │   │   ├── projection/
│   │   │   │   │   ├── AccountProjection.java
│   │   │   │   │   └── TransactionProjection.java
│   │   │   │   │
│   │   │   │   ├── queries/
│   │   │   │   │   ├── GetAccountByIdQuery.java
│   │   │   │   │   ├── GetAllAccountsQuery.java
│   │   │   │   │   └── GetTransactionHistoryQuery.java
│   │   │   │   │
│   │   │   │   └── repository/
│   │   │   │       ├── AccountRepository.java
│   │   │   │       └── TransactionRepository.java
│   │   │   │
│   │   │   ├── saga/
│   │   │   │   └── TransferSaga.java
│   │   │   │
│   │   │   └── BankingSystemApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│       └── java/
│           └── com/team/banking/
│               ├── BankingSystemApplicationTests.java
│               └── integration/
│                   └── BankingIntegrationTest.java
│
├── docs/
│   ├── ADR-001-CQRS-Event-Sourcing.md
│   └── event-flow.md
│
├── Banking_System_CQRS_Postman_Collection.json
├── pom.xml
└── README.md
```

---

# Banking Operations

The system currently supports:

| Operation           | HTTP Method | Endpoint                                 |
| ------------------- | ----------- | ---------------------------------------- |
| Open Account        | POST        | `/api/accounts`                          |
| Deposit Money       | POST        | `/api/accounts/{accountId}/deposit`      |
| Withdraw Money      | PUT         | `/api/accounts/{accountId}/withdraw`     |
| Transfer Money      | POST        | `/api/accounts/{accountId}/transfer`     |
| Close Account       | PUT         | `/api/accounts/{accountId}/close`        |
| Get All Accounts    | GET         | `/api/accounts`                          |
| Get Account         | GET         | `/api/accounts/{accountId}`              |
| Transaction History | GET         | `/api/accounts/{accountId}/transactions` |

---

# API Documentation

Base URL:

```text
http://localhost:8080
```

---

## 1. Open Account

### Request

```http
POST /api/accounts
Content-Type: application/json
```

### Body

```json
{
  "customerName": "Test User 1",
  "accountType": "SAVINGS",
  "initialBalance": 5000
}
```

### Response

```text
Account created successfully. Account ID: <generated-account-id>
```

The account ID is generated by the application using UUID.

---

# 2. Deposit Money

### Request

```http
POST /api/accounts/{accountId}/deposit
Content-Type: application/json
```

### Body

```json
{
  "amount": 1000
}
```

### Response

```text
Money deposited successfully.
```

### Processing Flow

```text
DepositMoneyCommand
        ↓
AccountAggregate
        ↓
MoneyDepositedEvent
        ↓
Axon Event Store
        ↓
AccountProjection
        ↓
PostgreSQL balance updated
```

---

# 3. Withdraw Money

### Request

```http
PUT /api/accounts/{accountId}/withdraw
Content-Type: application/json
```

### Body

```json
{
  "amount": 500
}
```

### Response

```text
Money withdrawn successfully.
```

---

# 4. Transfer Money

### Request

```http
POST /api/accounts/{sourceAccountId}/transfer
Content-Type: application/json
```

### Body

```json
{
  "destinationAccountId": "<destination-account-id>",
  "amount": 1000
}
```

### Response

```text
Transfer request submitted successfully. Transfer ID: <generated-transfer-id>
```

A UUID is generated for each transfer.

The transfer is coordinated using the `TransferSaga`.

---

# 5. Close Account

### Request

```http
PUT /api/accounts/{accountId}/close
```

### Response

```text
Account closed successfully.
```

An account can only be closed when its balance is zero.

---

# 6. Get All Accounts

### Request

```http
GET /api/accounts
```

### Example Response

```json
[
  {
    "accountId": "account-id",
    "customerName": "Test User 1",
    "accountType": "SAVINGS",
    "balance": 5000.0,
    "status": "ACTIVE"
  }
]
```

---

# 7. Get Account By ID

### Request

```http
GET /api/accounts/{accountId}
```

### Example Response

```json
{
  "accountId": "account-id",
  "customerName": "Test User 1",
  "accountType": "SAVINGS",
  "balance": 5000.0,
  "status": "ACTIVE"
}
```

---

# 8. Get Transaction History

### Request

```http
GET /api/accounts/{accountId}/transactions
```

### Example Response

```json
[
  {
    "id": 1,
    "accountId": "account-id",
    "transactionType": "DEPOSIT",
    "amount": 1000.0,
    "balanceAfterTransaction": 6000.0,
    "relatedAccountId": null,
    "transferId": null,
    "timestamp": "2026-07-29T12:00:00"
  }
]
```

Transactions are returned in descending timestamp order.

---

# Domain Commands

The command side contains the following commands:

### OpenAccountCommand

Used to create a new account.

Fields:

```text
accountId
customerName
accountType
initialBalance
```

### DepositMoneyCommand

Used to deposit money.

Fields:

```text
accountId
amount
transferId
```

### WithdrawMoneyCommand

Used to withdraw money.

Fields:

```text
accountId
amount
transferId
```

### CloseAccountCommand

Used to close an account.

Fields:

```text
accountId
```

### TransferMoneyCommand

Used to initiate a transfer.

Fields:

```text
transferId
sourceAccountId
destinationAccountId
amount
```

### CompleteTransferCommand

Used to mark a transfer as completed.

Fields:

```text
transferId
```

---

# Domain Events

The system defines the following events:

## AccountOpenedEvent

Generated when a new account is created.

Contains:

```text
accountId
customerName
accountType
initialBalance
```

## MoneyDepositedEvent

Generated after a successful deposit.

Contains:

```text
accountId
amount
transferId
```

## MoneyWithdrawnEvent

Generated after a successful withdrawal.

Contains:

```text
accountId
amount
transferId
```

## AccountClosedEvent

Generated when an account is closed.

Contains:

```text
accountId
```

## TransferStartedEvent

Generated when a transfer begins.

Contains:

```text
transferId
sourceAccountId
destinationAccountId
amount
```

## TransferCompletedEvent

Generated when the transfer workflow completes.

Contains:

```text
transferId
sourceAccountId
destinationAccountId
amount
```

---

# Aggregates

## AccountAggregate

`AccountAggregate` is responsible for account-related business operations.

It handles:

* Opening accounts
* Depositing money
* Withdrawing money
* Closing accounts

The aggregate maintains:

```text
accountId
customerName
accountType
balance
status
```

Business rules are enforced inside the aggregate.

For example:

```text
Initial balance cannot be negative.

Deposit amount must be greater than zero.

Withdrawal amount must be greater than zero.

Withdrawal amount cannot exceed balance.

A closed account cannot be closed again.

An account cannot be closed while its balance is not zero.
```

---

## TransferAggregate

`TransferAggregate` manages the transfer lifecycle.

It maintains:

```text
transferId
sourceAccountId
destinationAccountId
amount
completed
```

It generates:

```text
TransferStartedEvent
TransferCompletedEvent
```

---

# Query Side

The query side maintains a PostgreSQL read model.

## AccountEntity

Mapped to:

```text
accounts
```

Fields:

```text
accountId
customerName
accountType
balance
status
```

## TransactionEntity

Mapped to:

```text
transactions
```

Fields:

```text
id
accountId
transactionType
amount
balanceAfterTransaction
relatedAccountId
transferId
timestamp
```

---

# Projections

## AccountProjection

Consumes:

* `AccountOpenedEvent`
* `MoneyDepositedEvent`
* `MoneyWithdrawnEvent`
* `AccountClosedEvent`

It updates the `accounts` read model.

For example:

```text
MoneyDepositedEvent
        ↓
AccountProjection
        ↓
Current balance + deposit amount
        ↓
accounts table
```

---

## TransactionProjection

Consumes banking events and creates transaction records.

It records:

```text
ACCOUNT_OPENED
DEPOSIT
WITHDRAW
TRANSFER_IN
TRANSFER_OUT
ACCOUNT_CLOSED
```

Transfer start and completion events themselves do not create additional account transaction records because the actual financial changes are represented by the withdrawal and deposit events.

---

# Query Handlers

## AccountQueryHandler

Handles:

```text
GetAccountByIdQuery
GetAllAccountsQuery
```

## TransactionQueryHandler

Handles:

```text
GetTransactionHistoryQuery
```

The handlers retrieve information from PostgreSQL using Spring Data JPA repositories.

---

# Transfer Saga

The project uses an Axon Saga to coordinate transfers between accounts.

The workflow is:

```text
TransferMoneyCommand
        ↓
TransferAggregate
        ↓
TransferStartedEvent
        ↓
TransferSaga
        ↓
WithdrawMoneyCommand
        ↓
MoneyWithdrawnEvent
        ↓
TransferSaga
        ↓
DepositMoneyCommand
        ↓
MoneyDepositedEvent
        ↓
TransferSaga
        ↓
CompleteTransferCommand
        ↓
TransferCompletedEvent
```

The Saga associates the workflow using the `transferId`.

This allows the transfer to be coordinated across multiple account aggregates.

---

# Database

PostgreSQL is used as the application's projected read database.

The project uses:

```text
Database: banking_system
```

The application uses Spring Data JPA and Hibernate.

The main read-model tables are:

```text
accounts
transactions
```

Axon also creates its required persistence tables for features such as tokens, saga state, and dead-letter processing when configured through JPA.

---

# Database Configuration

The application is configured through:

```text
src/main/resources/application.yml
```

Example configuration:

```yaml
server:
  port: 8080

spring:
  application:
    name: banking-system

  datasource:
    url: jdbc:postgresql://localhost:5432/banking_system
    username: postgres
    password: YOUR_POSTGRES_PASSWORD
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.springframework: INFO
    com.team.banking: DEBUG
```

> Use your local PostgreSQL port and credentials. Do not commit real database passwords to Git.

---

# Postman Testing

The repository contains:

```text
Banking_System_CQRS_Postman_Collection.json
```

The collection contains the following requests.

## Account Commands

```text
1. Open Source Account
2. Deposit Money
3. Withdraw Money
4. Open Destination Account
5. Transfer Money
6. Close Source Account
```

## Queries

```text
7. Get All Accounts
8. Get Source Account By ID
9. Get Destination Account By ID
10. Get Source Transaction History
11. Get Destination Transaction History
```

The Postman collection uses:

```text
{{baseUrl}}
{{sourceAccountId}}
{{destinationAccountId}}
```

Configure:

```text
baseUrl = http://localhost:8080
```

Account IDs are obtained from the account creation responses and used for subsequent requests.

---

# Recommended Testing Sequence

For manual testing, use this order:

```text
1. Start PostgreSQL
        ↓
2. Start Axon Server
        ↓
3. Start Spring Boot application
        ↓
4. Open Source Account
        ↓
5. Open Destination Account
        ↓
6. Deposit Money
        ↓
7. Withdraw Money
        ↓
8. Transfer Money
        ↓
9. Check Account Details
        ↓
10. Check Transaction History
        ↓
11. Bring account balance to zero
        ↓
12. Close Account
```

---

# Validation and Business Rules

## Account Creation

The aggregate rejects:

```text
Initial balance < 0
```

---

## Deposit

The aggregate rejects:

```text
Amount <= 0
```

---

## Withdrawal

The request and aggregate validate:

```text
Amount must be provided.

Amount must be greater than zero.

Withdrawal cannot exceed available balance.
```

---

## Transfer

The request validates:

```text
Destination account ID is required.

Amount is required.

Amount must be greater than zero.
```

---

## Account Closure

An account cannot be closed when:

```text
Balance != 0
```

An already closed account cannot be closed again.

---

# Integration Testing

The project contains:

```text
BankingIntegrationTest.java
```

The integration tests use **Testcontainers** with PostgreSQL.

The test suite covers scenarios including:

### Application

* Application starts successfully with a PostgreSQL Testcontainer.

### Account Creation

* Create account.
* Create account with zero balance.
* Create account with initial balance.
* Reject negative initial balance.
* Verify active account status.
* Verify account type.

### Account Queries

* Get account by ID.
* Get all accounts.
* Return multiple accounts.

### Deposits

* Deposit money.
* Perform multiple deposits.
* Reject zero deposit.
* Reject negative deposit.

### Withdrawals

* Withdraw money.
* Withdraw entire balance.
* Reject withdrawal greater than balance.
* Reject zero withdrawal.
* Reject negative withdrawal.

### Transaction History

* Return transaction history.
* Record deposit transactions.

### Account Closure

* Reject closing account with balance.
* Close account with zero balance.
* Reject closing an already closed account.

---

# Running Tests

Use Maven:

```bash
mvn test
```

or:

```bash
./mvnw test
```

The integration tests require Docker because Testcontainers uses containers to provide the PostgreSQL test environment.

---

# Axon Server Setup

The application uses **Axon Server** for command routing and event handling.

Start Axon Server before starting the Spring Boot application.

From the Axon Server installation directory:

```bash
java -jar axonserver.jar
```

The application connects to the Axon Server gateway through:

```text
localhost:8124
```

Axon Server's cluster communication uses:

```text
8224
```

The Axon Server dashboard can be used to inspect the server, contexts, applications, and event-related infrastructure.

---

# Axon Server Verification

Before starting the Spring Boot application, verify that the Axon Server gateway is listening:

### Windows

```cmd
netstat -ano | findstr 8124
```

Expected:

```text
TCP    0.0.0.0:8124    0.0.0.0:0    LISTENING
```

When the Spring Boot application starts successfully, the logs should contain:

```text
Successfully connected to localhost:8124
```

---

# Spring Boot Startup Verification

A successful application startup should contain messages similar to:

```text
HikariPool - Start completed
```

followed by:

```text
Initialized JPA EntityManagerFactory
```

and:

```text
Successfully connected to localhost:8124
```

and finally:

```text
Tomcat started on port 8080
```

and:

```text
Started BankingSystemApplication
```

---

# Running the Application

## 1. Start PostgreSQL

Make sure PostgreSQL is running and the database exists:

```text
banking_system
```

---

## 2. Start Axon Server

Navigate to the Axon Server installation directory:

```bash
cd <AxonServer-directory>
```

Run:

```bash
java -jar axonserver.jar
```

Wait until Axon Server has started successfully.

---

## 3. Start the Spring Boot Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run:

```text
BankingSystemApplication
```

directly from IntelliJ IDEA.

---

## 4. Verify the API

The application runs on:

```text
http://localhost:8080
```

---

# Build the Project

To compile the project:

```bash
mvn clean compile
```

To package the application:

```bash
mvn clean package
```

The generated JAR will be available under:

```text
target/
```

---

# Event Flow

The complete event-driven flow is:

```text
Client / Postman
        │
        ▼
REST Controller
        │
        ▼
Command
        │
        ▼
CommandGateway
        │
        ▼
Axon Server
        │
        ▼
Command Handler / Aggregate
        │
        ▼
Domain Event
        │
        ├──────────────► Event Store
        │
        ▼
Event Handlers / Projections
        │
        ├──────────────► AccountProjection
        │
        └──────────────► TransactionProjection
                               │
                               ▼
                         PostgreSQL
                         Read Model
                               │
                               ▼
                         Query Handler
                               │
                               ▼
                         Query Controller
                               │
                               ▼
                            Client
```

---

# Example: Deposit Money Event Flow

Suppose an account currently has:

```text
Balance = 5000
```

The client sends:

```http
POST /api/accounts/{accountId}/deposit
```

with:

```json
{
  "amount": 1000
}
```

The application creates:

```text
DepositMoneyCommand
```

The command reaches the `AccountAggregate`.

The aggregate validates the amount and applies:

```text
MoneyDepositedEvent
```

The event is processed by the projections.

The account read model becomes:

```text
5000 + 1000 = 6000
```

A transaction record is also created:

```text
Transaction Type = DEPOSIT
Amount = 1000
Balance After Transaction = 6000
```

---

# Example: Transfer Flow

Suppose:

```text
Source Account Balance      = 5000
Destination Account Balance = 1000
Transfer Amount             = 1000
```

The client calls:

```http
POST /api/accounts/{sourceAccountId}/transfer
```

with:

```json
{
  "destinationAccountId": "{destinationAccountId}",
  "amount": 1000
}
```

The transfer flow becomes:

```text
TransferStartedEvent
        ↓
Withdraw from Source
        ↓
MoneyWithdrawnEvent
        ↓
Deposit into Destination
        ↓
MoneyDepositedEvent
        ↓
TransferCompletedEvent
```

Final balances:

```text
Source      = 4000
Destination = 2000
```

The transaction projection records:

```text
TRANSFER_OUT
```

for the source account and:

```text
TRANSFER_IN
```

for the destination account.

---

# Why CQRS?

CQRS separates operations that modify system state from operations that retrieve information.

### Command Side

Responsible for:

```text
Commands
Aggregates
Command Handlers
Business Rules
Domain Events
```

### Query Side

Responsible for:

```text
Queries
Query Handlers
Projections
Read Models
Repositories
Query APIs
```

This separation allows the read model to be optimized independently of the command model.

---

# Why Event Sourcing?

Traditional systems generally store only the current state.

With Event Sourcing, business changes are represented as events.

For example:

```text
AccountOpenedEvent
MoneyDepositedEvent
MoneyWithdrawnEvent
```

The account state can be reconstructed by replaying these events.

Benefits include:

* Complete history of state changes.
* Audit-friendly architecture.
* Ability to rebuild projections.
* Clear representation of domain actions.
* Strong fit for event-driven workflows.

---

# Why Axon Framework?

Axon Framework provides infrastructure for implementing CQRS and Event Sourcing in Java.

The project uses Axon's:

* `CommandGateway`
* `QueryGateway`
* `@Aggregate`
* `@CommandHandler`
* `@EventSourcingHandler`
* `@EventHandler`
* `@QueryHandler`
* Saga support
* Axon Server integration

This reduces the amount of infrastructure code required to implement the event-driven architecture.

---

# Why PostgreSQL?

PostgreSQL is used for the query/read model.

The database stores projected information required for fast queries, including:

```text
accounts
transactions
```

The PostgreSQL model is therefore optimized for reading rather than being the source of truth for command-side state.

---

# Kafka

Kafka is **not used in the current runtime implementation**.

The current implementation uses:

```text
Axon Server
```

for command routing and event handling.

Kafka may be considered as a future messaging infrastructure option if the system is expanded into multiple independently deployed services.

---

# Architecture Decision

The project includes an Architecture Decision Record:

```text
docs/ADR-001-CQRS-Event-Sourcing.md
```

The decision was to use:

```text
CQRS
+
Event Sourcing
+
Axon Framework
+
Axon Server
+
PostgreSQL Read Model
```

The architecture provides clear separation between write processing and read processing while preserving domain events.

---

# Advantages

* Clear command/query separation.
* Event-driven architecture.
* Complete domain event history.
* Rebuildable read models.
* Business rules centralized in aggregates.
* Independent query projections.
* Saga-based transfer orchestration.
* PostgreSQL provides a practical read model.
* Testcontainers support realistic integration testing.

---

# Trade-offs

CQRS and Event Sourcing also introduce additional complexity.

### Increased Complexity

The application requires separate:

```text
Commands
Events
Aggregates
Queries
Handlers
Projections
Read Models
```

### Eventual Consistency

The query database is updated by event handlers, so there can be a small delay between command processing and read-model updates.

### Infrastructure

The application requires Axon Server in addition to PostgreSQL.

### Event Evolution

Changes to event structures need to be handled carefully because events represent historical facts.

---

# Future Enhancements

Possible future improvements include:

* Authentication and authorization.
* Role-based access control.
* Customer management.
* Beneficiary management.
* Scheduled transfers.
* Transaction pagination.
* Transaction filtering.
* Transaction sorting.
* Improved global exception handling.
* Standardized API error responses.
* Swagger / OpenAPI documentation.
* Better monetary precision using `BigDecimal`.
* Event versioning and upcasting.
* Dedicated event schemas.
* Distributed deployment.
* Monitoring and observability.
* Production-grade security.
* Kafka integration for external event streaming.
* More comprehensive transfer integration tests.

---

# Important Production Consideration

The current implementation uses `Double` for monetary values.

For a production banking application, monetary amounts should preferably use:

```text
BigDecimal
```

to avoid floating-point precision issues.

The current project intentionally keeps the implementation simple for demonstrating CQRS, Event Sourcing, Axon Framework, and event-driven banking workflows.

---

# API Summary

| Method | Endpoint                                 | Purpose                 |
| ------ | ---------------------------------------- | ----------------------- |
| POST   | `/api/accounts`                          | Open account            |
| POST   | `/api/accounts/{accountId}/deposit`      | Deposit money           |
| PUT    | `/api/accounts/{accountId}/withdraw`     | Withdraw money          |
| POST   | `/api/accounts/{accountId}/transfer`     | Transfer money          |
| PUT    | `/api/accounts/{accountId}/close`        | Close account           |
| GET    | `/api/accounts`                          | Get all accounts        |
| GET    | `/api/accounts/{accountId}`              | Get account details     |
| GET    | `/api/accounts/{accountId}/transactions` | Get transaction history |

---

# Repository Documentation

Additional architecture documentation is available under:

```text
docs/
```

### ADR

```text
docs/ADR-001-CQRS-Event-Sourcing.md
```

Documents the architectural decision to use CQRS and Event Sourcing.

### Event Flow

```text
docs/event-flow.md
```

Contains the system event-flow diagram and explanation.

---

# Contributors

* **Sahil Paliwal**
* **Tripshikha Singh**

---

# Project Summary

The Event-Driven Banking System demonstrates how a banking backend can be designed using modern event-driven architecture.

The system combines:

```text
Spring Boot
     +
CQRS
     +
Event Sourcing
     +
Axon Framework
     +
Axon Server
     +
PostgreSQL
     +
Spring Data JPA
     +
REST APIs
     +
Saga Pattern
```

The command side processes banking operations and produces domain events, while the query side builds PostgreSQL read models from those events.

This architecture provides a clear separation of responsibilities, maintains a history of business operations, and demonstrates how CQRS, Event Sourcing, projections, and Saga-based workflows can be combined to build a banking application.
