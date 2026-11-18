# Architecture

C4 level 1 to 3 for fint-flyt-authorization-service. Blue is this service, purple is the database,
turquoise is external.

## C4 level 1: System context

Authorization-service decides which source applications a FLYT user may see, and which source
application an external client ID belongs to. The other FLYT services ask it through
flyt-web-resource-server.

```mermaid
C4Context
    title System context for fint-flyt-authorization-service

    System_Ext(frontend, "fint-flyt-frontend", "Users sign in, admins manage access.")
    System_Ext(idp, "NAM", "Issues the JWTs this service validates.")
    System(authz, "authorization-service", "User access per source application and client ID lookup.")
    System_Ext(services, "Other FLYT services", "Ask through flyt-web-resource-server and flyt-audit-starter.")
    System_Ext(kafka, "Kafka", "userpermission topic.")

    Rel(frontend, authz, "API", "HTTPS")
    Rel(authz, idp, "Validates JWT")
    Rel(services, authz, "Lookups", "HTTP")
    Rel(authz, kafka, "userpermission")

    UpdateRelStyle(frontend, authz, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(authz, idp, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(services, authz, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(authz, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateElementStyle(authz, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(idp, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(services, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- **Users** sign in through fint-flyt-frontend. On first sign-in the service creates the user from
  the JWT, if the token has a permitted app role. Admins get access to all source applications.
- **Other FLYT services** call `/api/intern-klient/authorization` with a token for
  `fint-flyt-authorization-oauth2-client`, the shared client for internal calls:
  - flyt-web-resource-server checks which source applications a user may see, and which source
    application the client ID of an incoming token belongs to.
  - flyt-audit-starter looks up the display names of users for audit history.
- **Kafka:** user permissions are still published as `userpermission` entity events. The only
  consumer was the archived fint-flyt-resource-server, so the topic has no consumers now.

## C4 level 2: Containers

One Spring Boot application and one PostgreSQL database.

```mermaid
C4Container
    title Containers

    System_Ext(frontend, "fint-flyt-frontend", "Browser app.")
    System_Ext(services, "Other FLYT services", "flyt-web-resource-server, flyt-audit-starter.")
    Container(app, "authorization-service", "Kotlin, Spring Boot 3, Java 25", "Internal API and internal client API.")
    ContainerDb(db, "PostgreSQL", "Flyway, JPA, Envers", "Users, source application access and audit history.")
    ContainerQueue_Ext(kafka, "Kafka", "Topic", "userpermission")
    System_Ext(prometheus, "Prometheus", "Metrics in the cluster.")

    Rel(frontend, app, "API", "HTTPS, JWT")
    Rel(services, app, "Internal client API", "HTTP")
    Rel(app, db, "Reads, writes", "JDBC")
    Rel(app, kafka, "Publishes")
    Rel(prometheus, app, "Scrapes", "HTTP")

    UpdateRelStyle(frontend, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(services, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(app, db, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(app, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(prometheus, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateElementStyle(app, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(db, $bgColor="#C4B5FD", $fontColor="#1F2937", $borderColor="#8B5CF6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(services, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(prometheus, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- The ingress only routes `/api/intern/authorization`. The internal client API, Swagger UI,
  OpenAPI and Actuator are only reachable inside the cluster.
- The database is the shared `fint-flyt` database, with Flyway migrations in this repo.

## C4 level 3: Components

### Users

```mermaid
C4Component
    title Users

    System_Ext(frontend, "fint-flyt-frontend", "Browser app.")
    System_Ext(services, "Other FLYT services", "flyt-web-resource-server, flyt-audit-starter.")
    Component(userApi, "User API", "Spring MVC", "MeController and UserController under /api/intern.")
    Component(clientUserApi, "Internal client user API", "Spring MVC", "InternalClientUserController under /api/intern-klient.")
    Component(userService, "UserService", "Service", "Creates users from JWT claims, checks access, saves changes.")
    Component(publisher, "UserPublishingComponent", "Spring @Scheduled", "Republishes all users every 12 hours.")
    Component(repository, "UserRepository", "Spring Data JPA", "Users and their source application IDs.")
    Component(producer, "Kafka producer", "flyt-kafka", "userpermission entity events.")
    ContainerDb(db, "PostgreSQL", "Flyway, Envers", "user_entity and audit tables")
    ContainerQueue_Ext(kafka, "Kafka", "Topic", "userpermission")

    Rel(frontend, userApi, "GET, POST", "HTTPS")
    Rel(services, clientUserApi, "Lookups", "HTTP")
    Rel(userApi, userService, "Uses")
    Rel(clientUserApi, userService, "Uses")
    Rel(publisher, userService, "Publishes all")
    Rel(userService, repository, "Reads, saves")
    Rel(userService, producer, "On change")
    Rel(repository, db, "SQL", "JDBC")
    Rel(producer, kafka, "Sends")

    UpdateRelStyle(frontend, userApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(services, clientUserApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(userApi, userService, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(clientUserApi, userService, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(publisher, userService, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(userService, repository, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(userService, producer, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(repository, db, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(producer, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateElementStyle(userApi, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(clientUserApi, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(userService, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(publisher, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(repository, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(producer, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(db, $bgColor="#C4B5FD", $fontColor="#1F2937", $borderColor="#8B5CF6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(services, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- `MeController` returns the signed-in user and creates it on first sign-in. `UserController` is
  for admins: list users and set their source applications in bulk.
- `InternalClientUserController` looks up users by `objectIdentifier` and answers
  `actions/authorize-source-applications`, which flyt-web-resource-server calls for every request
  that needs user access.
- Changes to `user_entity` are kept in Envers audit tables with the actor from flyt-audit-starter.
  `LocalActorNameLookupConfig` looks names up in the same database instead of over HTTP.

### Source applications and clients

```mermaid
C4Component
    title Source applications and clients

    System_Ext(services, "Other FLYT services", "flyt-web-resource-server.")
    System_Ext(frontend, "fint-flyt-frontend", "Browser app.")
    Component(clientApi, "Client API", "Spring MVC", "Client ID lookup. 404 for unknown clients.")
    Component(sourceAppApi, "Source application API", "Spring MVC", "SourceApplicationController under /api/intern.")
    Component(clientService, "ClientAuthorizationService", "Service", "Finds the source application for a client ID.")
    Component(sourceApps, "Source applications", "Spring beans", "One per source application: ID, name, client ID, available.")
    System_Ext(prometheus, "Prometheus", "Metrics in the cluster.")
    Component(metrics, "Availability metrics", "Micrometer", "Available source applications per org.")

    Rel(services, clientApi, "GET /clients/{clientId}", "HTTP")
    Rel(frontend, sourceAppApi, "GET", "HTTPS")
    Rel(clientApi, clientService, "Uses")
    Rel(sourceAppApi, sourceApps, "Lists")
    Rel(clientService, sourceApps, "Matches")
    Rel(metrics, sourceApps, "Reads")
    Rel(prometheus, metrics, "Scrapes")

    UpdateRelStyle(services, clientApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(frontend, sourceAppApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(clientApi, clientService, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(sourceAppApi, sourceApps, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(clientService, sourceApps, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(metrics, sourceApps, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(prometheus, metrics, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateElementStyle(clientApi, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(sourceAppApi, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(clientService, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(sourceApps, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(metrics, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(services, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(prometheus, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- `InternalClientAuthorizationController` is the client API. `SourceApplicationAvailabilityMetrics`
  publishes the availability metrics.
- Each source application (Acos, eGrunnerverv, Digisak, Vigo, Altinn, HMSReg, eApply, fskyss,
  ISY Graving, SiDE) is a bean with a fixed ID. The client ID comes from the source application's
  OAuth client secret in the namespace (`fint.flyt.<name>.sso.client-id`), and
  `fint.flyt.<name>.available` says whether it is enabled for the org.
- flyt-web-resource-server calls `/clients/{clientId}` for incoming tokens on the external API and
  caches the answer for one minute. It replaced the `authorization` request/reply topic
  (FFS-2398, removed in FFS-2400).
