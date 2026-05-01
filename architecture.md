```mermaid
graph LR
    subgraph Frontend
        VUE[Vue Components]
        API[Axios API Client]
        VUE --> API
    end

    subgraph Backend
        CONTROLLER[Controller Layer]
        SERVICE[Service Layer]
        REPOSITORY[Repository Layer]
    end

    DB[(MySQL Database)]

    API --> CONTROLLER
    CONTROLLER --> SERVICE
    SERVICE --> REPOSITORY
    REPOSITORY --> DB
```