# AI Code Review Assistant — Project Log

## Day 1 — July 15, 2026

### Goal for the day
Get a Spring Boot 4.1 project scaffolded, running locally, and connected to an H2
in-memory database with a working console — before writing any business logic.

---

### What we set up
- **Project generated via Spring Initializr**
  - Spring Boot 4.1.0, Java 21, Maven, JAR packaging
  - Dependencies: Spring Web, Spring Reactive Web (WebFlux), Spring Data JPA,
    H2 Database, Validation, Lombok
  - Config format: YAML (`application.yml`) instead of `.properties`, chosen
    because our config has nested keys (`spring.datasource.*`, `gemini.*`) and
    YAML represents that nesting far more readably than dotted property lines.

- **Why WebFlux even though we're building a normal REST API (not reactive):**
  We don't need a reactive server — we need `WebClient`, which lives in the
  WebFlux dependency, to call the Gemini API later. We're using it in blocking
  mode (`.block()`), not fully reactive.

- **`application.yml` configured for:**
  - Server port 8080
  - H2 in-memory DB with a **stable name** (`jdbc:h2:mem:codereviewdb`) instead
    of Spring Boot's random default — so the DB doesn't change identity every
    restart
  - H2 web console enabled at `/h2-console`
  - JPA `ddl-auto: update` (auto-creates tables from `@Entity` classes — fine
    for dev, never for production) and `show-sql: true` (prints generated SQL,
    good for learning what Hibernate does under the hood)
  - `open-in-view: false` to avoid the default warning and the anti-pattern of
    DB queries firing during view rendering

---

### Issues hit today (and what they taught us)

**1. `release version 5 not supported`**
- **Cause:** IntelliJ's own per-module compiler settings hadn't synced with the
  freshly generated `pom.xml` (which correctly said Java 21).
- **Fix:** Full Maven reload (Maven tool window → reload all projects).
- **Lesson:** When config *looks* correct but behavior disagrees, suspect a
  stale IDE cache before changing anything else.

**2. H2 console returned 404 (Whitelabel Error Page)**
- **Initial hypothesis:** A known Spring Boot 4 rough edge where having both
  `spring-boot-starter-webmvc` and `spring-boot-starter-webflux` on the
  classpath can interfere with H2 console auto-configuration.
- **How we actually diagnosed it:** Ran the app with `--debug` as a program
  argument, which prints Spring Boot's full auto-configuration report
  (Positive/Negative matches with reasons). Searched that output for
  `H2ConsoleAutoConfiguration` and found the *real* reason:
  ```
  Did not match:
    - @ConditionalOnBooleanProperty (spring.h2.console.enabled=true)
      did not find property 'spring.h2.console.enabled'
  ```
  So the WebFlux theory was a red herring — the actual cause was much simpler.
- **Root cause:** A YAML indentation bug. We had written:
  ```yaml
  h2:
    console: true
    path: /h2-console
  ```
  which sets `spring.h2.console` to the boolean `true` (wrong key entirely),
  instead of nesting `enabled` and `path` **under** `console`.
- **Fix:**
  ```yaml
  h2:
    console:
      enabled: true
      path: /h2-console
  ```
- **Lesson:** In YAML, indentation *is* the parent-child relationship — there's
  no `{ }` or `;` to make nesting visually obvious like in JSON/Java. A dotted
  property `a.b.c.d` always maps to one indentation level per dot.

**3. H2 console said "Database not found" for `test-db`**
- **Cause:** Not a bug — the H2 console is a generic, standalone DB browser
  tool. It had a leftover default JDBC URL in its login form and had no way of
  knowing what our app's actual datasource URL was.
- **Fix:** Manually typed the correct URL into the console's login screen:
  `jdbc:h2:mem:codereviewdb`, user `sa`, blank password.
- **Lesson:** The H2 console never auto-syncs with `application.yml` — always
  match the URL by hand, and remember `mem` databases only exist while the app
  is running (gone on restart — expected, not a bug).

---

### State at end of Day 1
- App boots cleanly on port 8080
- H2 in-memory DB connects with a stable name
- H2 console reachable and browsable at `/h2-console`
- No business logic (entities, repository, service, controller) written yet —
  intentionally deferred until the foundation was verified solid

### Next session
Start the **Entity** layer — the `CodeReview` JPA entity that maps to a
database table (file name, source code, Gemini result, score, timestamp) —
then Repository → Service → Controller, in that order, same as the dependency
chain of who relies on whom.
