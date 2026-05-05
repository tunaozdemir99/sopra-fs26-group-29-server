# JointJourney — Server

> RESTful backend for the JointJourney collaborative travel-planning application, built as part of the Software Engineering Praktikum (SoPra) FS26 at the University of Zurich.

[![Build & Deploy](https://github.com/tunaozdemir99/sopra-fs26-group-29-server/actions/workflows/main.yml/badge.svg)](https://github.com/tunaozdemir99/sopra-fs26-group-29-server/actions)

---

## Introduction

Planning group trips often results in chaotic groupchats and messy spreadsheets, leading to
scheduling conflicts and logistical stress. **JointJourney** motivates a more collaborative approach by providing a unified, real-time workspace for itinerary design, by leveraging the Google Maps API. We propose a synchronized application that allows users to timeline their activities, while automatically calculating travel times between locations and flagging scheduling overlaps.
JointJourney provides quasi real-time collaboration through a synchronized voting system, a shared itinerary, an idea bucket and much more. We make organising trips easier and fun!
---

## Technologies

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.0 |
| Database | H2 (in-memory, development) |
| ORM | Spring Data JPA / Hibernate |
| DTO mapping | MapStruct 1.5.5 |
| Build tool | Gradle (Wrapper included) |
| Testing | JUnit 5, Spring Boot Test |
| Code quality | SonarCloud, JaCoCo |
| Containerisation | Docker (multi-stage build) |
| Deployment | Google App Engine (Standard, Java 17) |
| External API | Google Maps Routes API (travel-time estimation) |

---

## High-Level Components

### 1. User Management — [`UserService`](src/main/java/ch/uzh/ifi/hase/soprafs26/service/UserService.java) · [`UserController`](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/UserController.java)

Handles registration, login, and logout. On every successful login a fresh UUID token is issued and stored on the `User` entity; all subsequent requests must supply this token in the `Authorization: Bearer <token>` header. The token is cleared on logout.

### 2. Trip Management — [`TripService`](src/main/java/ch/uzh/ifi/hase/soprafs26/service/TripService.java) · [`TripController`](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/TripController.java)

The `Trip` is the central aggregate of the application. A user creates a trip (becoming its admin), and other users join via a unique invite URL. Every other feature (bucket list, timeline, tasks) is attached to a trip. The service grants read or modifications access to only trip members.

### 3. Idea Bucket — [`BucketItemService`](src/main/java/ch/uzh/ifi/hase/soprafs26/service/BucketItemService.java) · [`BucketItemController`](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/BucketItemController.java)

Trip members add activity ideas (with optional location coordinates) to a shared bucket list. Ideas can be upvoted/downvoted by members to surface the most popular ones. A bucket item keeps its entry in the list even after it has been promoted to a scheduled activity.

### 4. Activity Timeline — [`ActivityService`](src/main/java/ch/uzh/ifi/hase/soprafs26/service/ActivityService.java) · [`ActivityController`](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/ActivityController.java)

Members schedule confirmed activities on the trip's timeline by picking a bucket item and assigning a date and time slot. The timeline endpoint returns activities sorted chronologically and annotates each entry with its duration, the gap to the next activity on the same day, and the estimated driving time to the next venue.

### 5. Travel Time — [`TravelTimeService`](src/main/java/ch/uzh/ifi/hase/soprafs26/service/TravelTimeService.java)

A thin wrapper around the Google Maps Routes API. Given two sets of coordinates it returns the estimated driving time in minutes. If the API key is absent the field is simply absent from the response, so the rest of the application works without it.

---

## Launch & Deployment

### Prerequisites

- Java 17 (set `JAVA_HOME` on Windows)
- A Google Maps API key for travel-time estimates

### Clone the repository

```bash
git clone https://github.com/tunaozdemir99/sopra-fs26-group-29-server.git
cd sopra-fs26-group-29-server
```

### Configure the Google Maps API key (optional)

Create a file `local.properties` in the project root (it is git-ignored):

```properties
GOOGLE_MAPS_API_KEY=your_key_here
```

If the file is absent the travel-time feature is silently disabled.

### Build

```bash
./gradlew build        # macOS / Linux
gradlew.bat build      # Windows
```

### Run locally

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.  
The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, no password).

### Development mode (auto-reload)

Open two terminals:

```bash
# Terminal 1 — continuous build (skip tests for speed)
./gradlew build --continuous -xtest

# Terminal 2 — run
./gradlew bootRun
```

### Run the tests

```bash
./gradlew test
```

A JaCoCo HTML report is generated at `build/reports/jacoco/test/html/index.html`.

### API exploration

Import the collection into [Postman](https://www.postman.com/) or use any HTTP client. All protected endpoints expect:

```
Authorization: Bearer <token>
Content-Type: application/json
```

### Deployment

The project ships with a `Dockerfile` (multi-stage build) and an `app.yaml` for Google App Engine.

**Docker**

```bash
docker build -t triptogether-server .
docker run -p 8080:8080 -e GOOGLE_MAPS_API_KEY=your_key triptogether-server
```

**Google App Engine**

1. Install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) and authenticate.
2. Set your project: `gcloud config set project YOUR_PROJECT_ID`
3. Edit `app.yaml` and replace the `GOOGLE_MAPS_API_KEY` placeholder with your key.
4. Deploy: `gcloud app deploy`

**CI/CD** — Every push to `main` triggers the GitHub Actions workflow that builds the Docker image, pushes it to Docker Hub, and deploys to Google App Engine automatically.

---

## Roadmap

The following features would be valuable additions for new contributors:

1. **Push notifications for trip events** — Members currently have no way to be alerted when someone joins the trip, adds a bucket item, or schedules an activity. Integrating Firebase Cloud Messaging (or a similar push service) and storing device tokens per user would enable real-time notifications.

2. **Persistent production database** — The application currently relies on an H2 in-memory database which is reset on every restart. Migrating to PostgreSQL (and adding Flyway or Liquibase for schema migrations) would make the deployed application production-ready and allow data to survive redeploys.

---

## Authors and Acknowledgment

| Name | GitHub |
|---|---|
| Adnana Ivana     | [@adnana24](https://github.com/adnana24) |
| Doğa Mentese     | [@dogamentese](https://github.com/dogamentese) |
| Emmanuel Oyelana | [@eoyelana](https://github.com/eoyelana) |
| Tuna Özdemir | [@tunaozdemir99](https://github.com/tunaozdemir99) |
| Stella Xiao | [@stella-sy-x](https://github.com/stella-sy-x) |


We would like to thank our teaching assistant and the SoPra FS26 course team at the University of Zurich for their guidance throughout the project.

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).