City Bus Platform Design Document
Architecture Diagram
code
Mermaid
flowchart LR
    User[English Interface] -->|REST API| BE[Spring Boot Backend]
    BE -->|SQL| DB[(PostgreSQL 15)]
    BE -->|Local IO| FS[(Local Backup Strategy)]

    subgraph InternalServices
      Scheduler[Spring Task Scheduler]
      Cleaning[Data Cleaning Pipeline]
      Workflow[Workflow State Machine]
    end

    subgraph Monitoring
      Trace[Trace ID Tracking]
      Metrics[P95 Latency Monitor]
      Health[Health Check Service]
    end

    BE --> InternalServices
    BE --> Monitoring
Workflow Approval State Machine
code
Mermaid
stateDiagram-v2
    [*] --> pending
    pending --> routine_branch: Minor Change
    pending --> risky_branch: Structural Change
    
    routine_branch --> approved: Dispatcher Sign-off
    risky_branch --> joint_approval: Parallel Approvals Required
    joint_approval --> approved: Final Admin/Joint Sign-off
    
    approved --> [*]
    
    pending --> returned: Task Return
    returned --> pending: Resubmission
    
    pending --> escalated: 24h Timeout
Tech Stack Justification
Spring Boot 3.x: Selected for robust local scheduling, REST decoupling, and seamless integration with PostgreSQL in offline environments.
Angular 17+: Provides the required English interface with modular components for separate Role-Based dashboards.
PostgreSQL 15: Reliable relational storage for versioned stop structures and complex weighted search queries.
Docker: Canonical runtime contract; ensures "Offline LAN" deployment is reproducible and isolated.
Salted BCrypt: Ensures industry-standard password security without needing external auth providers.
Data Cleaning & Normalization Logic
Standardization: All area fields are unified as sqm and prices as yuan/month.
Missing Values: If a source field is empty, the system marks it as NULL and generates a source log entry for audit.
Versioning: Every change to a bus stop structure creates a new entry in the stop_versions table, allowing for rollback and historical review.
Observability & Performance Metrics
Trace IDs: Injected via MDC into all logs (Search, Parsing, Workflow) for end-to-end request tracking.
P95 Monitoring: An internal interceptor tracks response times. If the 95th percentile exceeds 500ms, a local alert and diagnostic report are generated.
Local Backup: A secondary container/scheduled job executes a pg_dump daily to ensure data persistence without cloud dependencies.