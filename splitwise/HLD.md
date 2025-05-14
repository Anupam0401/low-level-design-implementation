# Splitwise High-Level Design (HLD)

## 1. System Overview

The Splitwise application is designed to help users track shared expenses and settle debts efficiently. The system allows users to create groups, add expenses, split costs using various strategies, and simplify debts to minimize the number of transactions required for settlement.

## 2. Architecture Diagram

```
┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
│                   │     │                   │     │                   │
│   Client Apps     │────▶│   API Gateway     │────▶│   Load Balancer   │
│                   │     │                   │     │                   │
└───────────────────┘     └───────────────────┘     └─────────┬─────────┘
                                                              │
                                                              ▼
┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
│                   │     │                   │     │                   │
│   Cache Layer     │◀───▶│   Application     │◀───▶│   Database        │
│   (Redis)         │     │   Servers         │     │   (SQL/NoSQL)     │
│                   │     │                   │     │                   │
└───────────────────┘     └───────────────────┘     └───────────────────┘
                                    │
                                    ▼
                          ┌───────────────────┐
                          │                   │
                          │   Notification    │
                          │   Service         │
                          │                   │
                          └───────────────────┘
```

## 3. Core Components

### 3.1 API Gateway
- Entry point for all client requests
- Handles authentication and authorization
- Routes requests to appropriate services
- Rate limiting and request throttling

### 3.2 Application Servers
- Implements the business logic
- Processes expense creation and splitting
- Manages user and group data
- Implements debt simplification algorithms
- Stateless for horizontal scalability

### 3.3 Database Layer
- Stores user profiles, groups, expenses, and transactions
- Maintains balance records between users
- Supports ACID transactions for financial operations
- Current implementation uses in-memory storage with interfaces for easy migration to persistent storage

### 3.4 Cache Layer
- Caches frequently accessed data (user balances, group information)
- Reduces database load
- Improves response times for common operations

### 3.5 Notification Service
- Sends notifications for new expenses, settlements, and reminders
- Supports multiple channels (email, push notifications, SMS)

## 4. Data Flow Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│             │     │             │     │             │     │             │
│  User       │────▶│  Controller │────▶│  Service    │────▶│  Repository │
│  Interface  │     │  Layer      │     │  Layer      │     │  Layer      │
│             │     │             │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │                   │
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│                             Database                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 5. Key Subsystems

### 5.1 User Management Subsystem
- User registration and authentication
- User profile management
- Friend/contact management

### 5.2 Group Management Subsystem
- Group creation and configuration
- Member management
- Group expense tracking

### 5.3 Expense Management Subsystem
- Expense creation with different types (personal, group, non-group)
- Support for various splitting strategies (equal, percentage, exact amount)
- Expense categorization and tagging

### 5.4 Balance Management Subsystem
- Balance calculation between users
- Net balance tracking for groups
- Debt simplification algorithm

### 5.5 Transaction Management Subsystem
- Settlement transaction recording
- Transaction history and reporting
- Payment integration (future enhancement)

## 6. System Interactions Diagram

```
┌───────────┐     ┌───────────┐     ┌───────────┐     ┌───────────┐
│           │     │           │     │           │     │           │
│  User     │────▶│  Group    │────▶│  Expense  │────▶│  Balance  │
│  System   │     │  System   │     │  System   │     │  System   │
│           │     │           │     │           │     │           │
└───────────┘     └───────────┘     └───────────┘     └───────────┘
                                                           │
                                                           │
                                                           ▼
                                                    ┌───────────┐
                                                    │           │
                                                    │Transaction│
                                                    │  System   │
                                                    │           │
                                                    └───────────┘
```

## 7. API Structure

### 7.1 User APIs
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/{userId}` - Get user details
- `GET /api/v1/users` - Get all users

### 7.2 Group APIs
- `POST /api/v1/groups` - Create a new group
- `GET /api/v1/groups/{groupId}` - Get group details
- `POST /api/v1/groups/{groupId}/members/{userId}` - Add member to group

### 7.3 Expense APIs
- `POST /api/v1/expenses/personal` - Add personal expense
- `POST /api/v1/expenses/non-group` - Add non-group expense
- `POST /api/v1/expenses/group` - Add group expense
- `GET /api/v1/expenses/users/{userId}` - Get user expenses
- `GET /api/v1/expenses/groups/{groupId}` - Get group expenses

### 7.4 Balance APIs
- `GET /api/v1/balances/users/{userId1}/users/{userId2}` - Get balance between users
- `GET /api/v1/balances/users/{userId}` - Get user balances
- `GET /api/v1/balances/groups/{groupId}` - Get group balances
- `GET /api/v1/balances/groups/{groupId}/simplify` - Get simplified group debts
- `GET /api/v1/balances/users/{userId}/simplify` - Get simplified user debts

### 7.5 Transaction APIs
- `POST /api/v1/transactions` - Perform transaction
- `GET /api/v1/transactions/users/{userId}` - Get user transactions
- `GET /api/v1/transactions/{transactionId}` - Get transaction details

## 8. Scalability Considerations

### 8.1 Horizontal Scaling
- Stateless application servers allow for easy horizontal scaling
- Load balancing across multiple application instances
- Database sharding for large-scale deployments

### 8.2 Caching Strategy
- Multi-level caching (application-level and distributed cache)
- Cache invalidation strategies for maintaining data consistency
- Time-to-live (TTL) settings based on data access patterns

### 8.3 Database Scaling
- Read replicas for handling high read traffic
- Database partitioning for large user bases
- Eventual consistency model where appropriate

## 9. Fault Tolerance and Reliability

### 9.1 Data Redundancy
- Database replication for data durability
- Regular backups and point-in-time recovery

### 9.2 Service Redundancy
- Multiple application server instances
- Automatic failover mechanisms

### 9.3 Error Handling
- Comprehensive error handling and logging
- Circuit breakers for dependent services
- Graceful degradation of functionality

## 10. Security Considerations

### 10.1 Authentication and Authorization
- Token-based authentication
- Role-based access control
- OAuth integration for third-party authentication

### 10.2 Data Security
- Encryption of sensitive data
- Secure API endpoints
- Input validation and sanitization

### 10.3 API Security
- Rate limiting to prevent abuse
- HTTPS for all communications
- API key management

## 11. Monitoring and Observability

### 11.1 Logging
- Centralized logging system
- Log aggregation and analysis
- Error tracking and alerting

### 11.2 Metrics
- System health metrics
- Performance monitoring
- Business metrics (active users, transaction volume)

### 11.3 Alerting
- Threshold-based alerts
- Anomaly detection
- On-call rotation for incident response

## 12. Future Enhancements

### 12.1 Payment Integration
- Integration with payment gateways
- Direct settlement through the application

### 12.2 Advanced Analytics
- Spending patterns and insights
- Budget recommendations
- Expense categorization using ML

### 12.3 Social Features
- Activity feed
- Comments and notes on expenses
- Integration with social networks

## 13. Deployment Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                         Cloud Provider                            │
│                                                                   │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐             │
│  │             │   │             │   │             │             │
│  │  Web/App    │   │  Container  │   │  Database   │             │
│  │  Servers    │   │  Orchestration│  │  Cluster    │             │
│  │             │   │             │   │             │             │
│  └─────────────┘   └─────────────┘   └─────────────┘             │
│                                                                   │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐             │
│  │             │   │             │   │             │             │
│  │  Cache      │   │  Message    │   │  Storage    │             │
│  │  Cluster    │   │  Queue      │   │  Service    │             │
│  │             │   │             │   │             │             │
│  └─────────────┘   └─────────────┘   └─────────────┘             │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## 14. Conclusion

The high-level design of the Splitwise application provides a scalable, reliable, and maintainable architecture for expense sharing and debt settlement. The modular design allows for easy extension and modification as requirements evolve, while the separation of concerns ensures that each component has a clear responsibility.

The current implementation uses in-memory storage with well-defined interfaces, making it straightforward to migrate to persistent storage solutions as needed. The system is designed to handle increasing load through horizontal scaling and efficient caching strategies.
