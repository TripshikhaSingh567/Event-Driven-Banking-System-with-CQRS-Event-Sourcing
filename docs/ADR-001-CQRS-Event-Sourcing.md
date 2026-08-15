# ADR-001: Adoption of CQRS and Event Sourcing

## Status

Accepted

## Context

The Banking System contains operations such as opening accounts, depositing money, withdrawing money, closing accounts and transferring money.

These operations modify important banking state and need a reliable history of changes. At the same time, the application needs efficient read operations such as retrieving account details, listing accounts and viewing transaction history.

Using a single model for both writes and reads would tightly couple command processing with query requirements.

## Decision

We decided to use:

- CQRS (Command Query Responsibility Segregation)
- Event Sourcing
- Axon Framework
- Axon Server
- PostgreSQL for the projected read model

The command side is responsible for processing business operations and generating domain events.

The query side uses projections created from events and stores the resulting read models in PostgreSQL.

Axon Server is used for command routing and event handling in the current implementation.

## Architecture

The application is separated into two logical sides.

### Command Side

The command side contains:

- Commands
- Aggregates
- Command handlers
- REST command controllers
- Domain events

Example flow:

Client → Controller → CommandGateway → Axon Server → Aggregate → Event

### Query Side

The query side contains:

- Queries
- Query handlers
- Projections
- Read-model entities
- Repositories
- Query controllers

Example flow:

Event → Projection → PostgreSQL Read Model → Query Handler → REST Response

## Why CQRS?

CQRS provides a clear separation between operations that change system state and operations that read system state.

This makes the application easier to organize and allows the read model to be optimized independently from the command model.

## Why Event Sourcing?

Event Sourcing preserves business changes as events instead of relying only on the current state.

For example, an account balance can be derived from events such as:

- AccountOpenedEvent
- MoneyDepositedEvent
- MoneyWithdrawnEvent
- TransferStartedEvent
- TransferCompletedEvent
- AccountClosedEvent

This provides an audit-friendly history of banking operations.

## Why Axon Framework?

Axon provides infrastructure for implementing CQRS and Event Sourcing in Java applications.

It provides:

- CommandGateway
- Aggregates
- Command handlers
- Event handlers
- Event processing
- Axon Server integration

This reduces the amount of infrastructure code required to implement the architecture.

## Consequences

### Benefits

- Clear separation between command and query responsibilities
- Complete history of domain events
- Independent read-model projections
- Easier rebuilding of read models
- Suitable architecture for complex banking workflows
- Axon provides infrastructure for event-driven processing

### Trade-offs

- More classes and architectural complexity
- Eventual consistency between command and query models
- Additional infrastructure through Axon Server
- Projections need to be maintained correctly
- Event schema changes need to be handled carefully

## Alternatives Considered

### Traditional CRUD

A traditional CRUD architecture would be simpler, but it would not provide the same event history and command/query separation.

### CQRS Without Event Sourcing

CQRS without Event Sourcing could provide command/query separation, but would not preserve the complete sequence of domain events needed for event replay.

### Kafka-Based Messaging

Kafka was considered as a messaging/event transport option. However, the current implementation uses Axon Server for command routing and event handling, so Kafka was not added to the current runtime architecture.

## Final Decision

CQRS and Event Sourcing with Axon Framework were selected because they provide a suitable architecture for separating banking commands from queries while maintaining a history of domain events that can be used to rebuild read models.