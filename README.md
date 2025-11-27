# FINT Flyt Authorization Service

Spring Boot service that persists Flyt user permissions, answers client authorization requests, and exposes internal APIs for managing user access. It maps configured SSO client IDs to source applications, synchronizes permissions to Kafka, and provisions new users from JWT claims when they first sign in.

## Highlights

- **Internal OAuth2 APIs** — resource-server profile locks down `/api/intern/authorization` endpoints and enforces admin roles for management actions.
- **Kafka request/reply** — listens for `client-id` authorization requests and replies with source application mappings using the FINT Kafka request/reply utilities.
- **Permission sync** — persists users in Postgres and emits `userpermission` entity events whenever users are created, updated, or bulk-synced.
- **Role-gated onboarding** — accepts users only when their token roles match a configured allow-list and auto-grants admins access to all source applications.
- **Scheduled publishing** — optional scheduler republishes all user permissions on a fixed delay to keep downstream caches in sync.

## Architecture Overview

| Component | Responsibility |
| --- | --- |
| `ClientAuthorizationRequestConfiguration` | Provisions the Kafka request topic (`authorization` / `client-id`) and hosts the listener that responds to client ID lookups. |
| `ClientAuthorizationProducerRecordBuilder` | Resolves incoming client IDs to `ClientAuthorization` replies with the correct `sourceApplicationId`, or marks them unauthorized. |
| `AcosSourceApplication`, `DigisakSourceApplication`, `EgrunnervervSourceApplication`, `VigoSourceApplication`, `AltinnSourceApplication`, `HMSRegSourceApplication` | Bind SSO client IDs from properties and expose static source-application IDs used in authorization replies. |
| `UserService` | Core business logic for persisting users, mapping DTOs/entities, and emitting `UserPermission` Kafka events. |
| `UserRepository` | Spring Data JPA repository for `UserEntity` storage and lookups by `objectIdentifier`. |
| `UserPermissionEntityProducerService` | Produces last-value `userpermission` events to Kafka with a 4-day retention window. |
| `UserPublishingComponent` | Conditional scheduler that republishes all users at configured intervals. |
| `UserController` | Admin-only internal API for paginating users and bulk updating `sourceApplicationIds`. |
| `MeController` | Self-service internal API that provisions users from JWT claims, reports restricted-page access, and returns the current user. |
| `TokenParsingUtils` / `AccessControlProperties` | Parse JWT claims, check permitted app roles, and detect `ROLE_ADMIN` authority. |

## HTTP API

Base path: `/api/intern/authorization`

| Method | Path | Description | Request body | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/me` | Returns the current user; creates one from JWT claims when absent (admins are granted all source application IDs). | – | `200 OK` with `User` JSON. |
| `GET` | `/me/is-authorized` | Confirms the caller has a permitted role; provisions the user when missing. | – | `200 OK` with a confirmation string. |
| `GET` | `/me/restricted-page-authorization` | Indicates whether the caller can access the user-permission page (`ROLE_ADMIN`). | – | `200 OK` with `RestrictedPageAuthorization`. |
| `GET` | `/users` | Admin-only paginated list of users (`page`, `size`, `sort` supported). | – | `200 OK` with a `Page<User>` or `403` when unauthorized. |
| `POST` | `/users/actions/userPermissionBatchPut` | Admin-only bulk upsert of user permissions, then republishes all users. | JSON array of `User` objects (`objectIdentifier`, `email`, `name`, `sourceApplicationIds`). | `200 OK` or `403` on insufficient role. |

Errors are surfaced as standard Spring MVC responses (`403 Forbidden` when the caller lacks admin authority).

## Kafka Integration

- **Request/reply consumer** — provisions the `authorization` request topic (resource `authorization`, parameter `client-id`) with 10-minute retention and responds with `ClientAuthorization` payloads for known SSO client IDs.
- **Entity producer** — publishes `userpermission` entity events (partition count: 1, last/null-value retention: 4 days) whenever users are saved or bulk-updated; replay is also triggered by the scheduled publisher.
- Topic names are built with `orgId`/`domainContext` prefixes from `novari.kafka.topic.*` and the FINT Kafka templating services.

## Scheduled Tasks

`UserPublishingComponent.publishUsers()` runs on a fixed delay when `novari.flyt.authorization.access-control.enabled=true`. Defaults: initial delay `1000ms`, fixed delay `4320000ms` (~72 minutes). Each run republishes all stored users as `UserPermission` events.

## Configuration

The application includes the shared Spring profiles `flyt-kafka`, `flyt-logging`, `flyt-postgres`, and `flyt-resource-server`.

Key properties:

| Property | Description |
| --- | --- |
| `fint.application-id` | Defaults to `fint-flyt-authorization-service`. |
| `novari.flyt.authorization.access-control.permitted-approles.*` | Map of allowed app roles (e.g., `flyt-user`, `flyt-developer`) to role URIs used when onboarding users. |
| `novari.flyt.authorization.access-control.enabled` | Enables the scheduled user-permission publisher. |
| `novari.flyt.authorization.access-control.sync-schedule.initial-delay-ms` / `fixed-delay-ms` | Interval settings for the publisher task. |
| `fint.flyt.<app>.sso.client-id` | Client IDs for Acos, Digisak, Egrunnerverv, Vigo, Altinn, and HMSReg; mapped to source application IDs 1–6. |
| `novari.kafka.topic.domain-context` / `novari.kafka.topic.org-id` | Prefixes for Kafka topics and ACLs. |
| `spring.datasource.*` | JDBC details for Postgres; Flyway migrations live under `classpath:db/migration`. |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | OAuth issuer for protecting internal endpoints. |
| `novari.flyt.resource-server.security.api.internal.authorized-org-id-role-pairs-json` | Defines which org/role pairs may call internal APIs (injected by overlays). |
| `spring.kafka.consumer.group-id` | Defaults to the application ID. |

Secrets referenced by Kustomize overlays must provide database credentials, OAuth settings, Kafka access, and SSO client credentials (e.g., `fint-flyt-vigo-oauth2-client`, `fint-flyt-altinn-oauth2-client`, `fint-flyt-egrunnerverv-oauth2-client`, `fint-flyt-hmsreg-oauth2-client`).

## Running Locally

Prerequisites:

- Java 21+
- Docker (for the bundled Postgres helper) and access to a Kafka broker
- Gradle (wrapper included)

Useful commands:

```shell
./start-postgres                               # launch Postgres on localhost:5435
SPRING_PROFILES_ACTIVE=local-staging ./gradlew bootRun   # start with local defaults
./gradlew clean build                          # compile and run tests
./gradlew test                                 # run unit tests only
```

The `local-staging` profile points to `localhost:9092` for Kafka and configures database credentials/schema for local use. Override `fint.flyt.<app>.sso.client-id` and access-control properties as needed for experiments.

## Deployment

Kustomize layout:

- `kustomize/base/` contains the shared `Application` resource and OAuth/OnePassword references for each upstream SSO client.
- `kustomize/overlays/<org>/<env>/` applies namespace labels, Kafka org IDs, URL base paths, and authorized org/role pairs per organization and environment.

Templates are centralized in `kustomize/templates/`:

- `overlay.yaml.tpl` — `envsubst` template rendered for every overlay.

Regenerate overlays after changing the template or rendering logic:

```shell
./script/render-overlay.sh
```

The script injects namespace-specific values (base paths, Kafka topics, authorized org-role pairs) and rewrites each `kustomization.yaml` in place.

## Security

- Uses the FINT OAuth2 resource-server setup for JWT validation (`spring.security.oauth2.resourceserver.jwt.issuer-uri`).
- Internal APIs are restricted to callers whose org/role pairs are configured; management endpoints additionally require `ROLE_ADMIN`.
- Client-authorization replies are limited to explicitly configured SSO client IDs per source application.

## Observability & Operations

- Readiness and liveness: `/actuator/health`.
- Metrics: `/actuator/prometheus`.
- Structured logging via the shared logging profile; Kafka request/reply and user-permission publishing emit detailed log messages for traceability.

## Development Tips

- When adding a new source application, supply its `fint.flyt.<app>.sso.client-id`, assign a unique `SOURCE_APPLICATION_ID`, and update the builder test.
- If access-control role mappings change, adjust `novari.flyt.authorization.access-control.permitted-approles` and ensure admins still get full `sourceApplicationIds` in `MeController`.
- Bulk updates trigger Kafka publishes; keep database schema and `UserPermission` consumers aligned when altering user fields.
- Update `script/render-overlay.sh` if additional organizations need custom role mappings or path prefixes.

## Contributing

1. Create a topic branch for your change.
2. Run `./gradlew test` (and additional checks) before raising a PR.
3. If you modify Kustomize templates or overlay logic, rerun `./script/render-overlay.sh` and commit the generated manifests.
4. Add or adjust tests for any new behaviour or edge cases.

FINT Flyt Authorization Service is maintained by the FINT Flyt team. Reach out via the internal Slack channel or open an issue in this repository for questions or enhancements.
