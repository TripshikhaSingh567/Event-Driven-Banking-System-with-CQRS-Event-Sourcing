```mermaid
flowchart TD

    A[Client / Postman] --> B[REST Controller]

    B --> C[Command]
    C --> D[Axon CommandGateway]

    D --> E[Axon Server]

    E --> F[Command Handler]
    F --> G[Aggregate]

    G --> H[Domain Event]

    H --> E

    E --> I[Event Store]

    H --> J[Event Handlers / Projections]

    J --> K[Account Projection]
    J --> L[Transaction Projection]

    K --> M[(PostgreSQL Read Model)]
    L --> M

    A --> N[Query Controller]
    N --> M

    M --> O[Query Response]
    O --> A
```


## Event Flow

The banking system follows CQRS and Event Sourcing principles using Axon Framework.

Commands are received through REST controllers and sent to Axon using the CommandGateway. Axon routes the commands to the appropriate command handler or aggregate.

The aggregate validates the business operation and generates domain events. These events are persisted by Axon and consumed by projection/event handlers.

The projection handlers update the PostgreSQL read model. Query controllers read the projected data and return it to the client.

This separates the command/write side from the query/read side and allows the read model to be rebuilt from the stored events.

### Messaging Infrastructure

The current implementation uses Axon Server for command routing and event handling. Kafka is not part of the current runtime implementation.