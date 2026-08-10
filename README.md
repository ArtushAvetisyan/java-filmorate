# Filmorate — Movie Rating & Social Recommendation Service (With Database Support)

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![H2 Database](https://img.shields.io/badge/H2-Database-blue?style=flat-square&logo=h2)
![Spring JDBC](https://img.shields.io/badge/Spring-JDBC%20%2F%20JdbcTemplate-green?style=flat-square)
![Zalando Logbook](https://img.shields.io/badge/Zalando-Logbook-blue?style=flat-square)
![REST API](https://img.shields.io/badge/REST-API-red?style=flat-square)

**Filmorate** — is a full-fledged backend service for rating movies, forming a user community, and receiving movie-watching recommendations. In the current version, the service supports long-term data storage in the H2 database, working with genres, MPA ratings, a one-sided friendship system, and is covered by integration tests.

---

## 📋 Table of Contents
- [About the Service](#-about-the-service)
- [Architectural Solutions](#-architectural-solutions)
- [Component Composition](#-component-composition)
- [Technology Stack](#-technology-stack)
- [Database Structure](#-database-structure)
- [Business Logic and REST API](#-business-logic-and-rest-api)
- [Error Handling and Logging](#-error-handling-and-logging)
- [Integration Testing](#-integration-testing)
- [Launch Instructions](#-launch-instructions)

---

## 📖 About the Service

The main goal of **Filmorate** is to unite movie lovers into a single community, provide a convenient tool for finding popular movies, and ensure that the system state is preserved between application restarts:
1. **Data Persistence**: All data about users, movies, likes, friends, genres, and ratings is reliably stored in a relational database.
2. **Social Interaction**: Support for one-sided friendship (sending friend requests) and finding common interests.
3. **Cataloging and Ratings**: Classification of movies by genres and age ratings (MPA/MPAA) with the ability to form top lists by likes.

---

## 🏗 Architectural Solutions

The project is designed using the **Data Access Object (DAO)**, **Inversion of Control (IoC)** patterns and **Clean Architecture** principles:

1. **DAO Pattern (Data Access Object)**: The data access layer is abstracted through the `UserStorage` and `FilmStorage` interfaces. Their concrete implementations (`UserDbStorage`, `FilmDbStorage`) interact with the relational database through `JdbcTemplate`.
2. **Storage Separation via `@Qualifier`**: To manage different storage implementations (InMemory and DB Storage), the `@Qualifier` annotation is used, allowing Spring IoC to precisely inject the required components.
3. **Two-Mode Operation with H2**:
    - **Production Mode**: Data is stored in a file on the hard drive (`jdbc:h2:file:./db/filmorate`), which prevents its loss between restarts.
    - **Test Mode**: Use of an in-memory database for fast and isolated execution of integration tests.
4. **Automatic Schema Initialization**: The database schema is restored at startup from the `schema.sql` file, while reference data (genres, ratings) is initialized from `data.sql` using the `IF NOT EXISTS` construct.

---

## 🧩 Component Composition

### 1. Data Access Layer (DAO / Storage Layer)
| Interface / Class | Purpose and Functionality |
| :--- | :--- |
| **`UserStorage`** | Interface defining the contract for user management. |
| **`UserDbStorage`** | DAO implementation of `UserStorage` based on `JdbcTemplate` (`@Repository` / `@Component`). |
| **`FilmStorage`** | Interface defining the contract for movie management. |
| **`FilmDbStorage`** | DAO implementation of `FilmStorage` based on `JdbcTemplate` (`@Repository` / `@Component`). |
| **`GenreDbStorage`** | DAO for working with the movie genre reference directory. |
| **`MpaDbStorage`** | DAO for working with the MPA rating reference directory. |

### 2. Business Logic Layer (Service Layer)
| Service | Purpose and Functionality |
| :--- | :--- |
| **`UserService`** | Business logic for user interaction: one-sided friendship, retrieving the list of friends and common friends. |
| **`FilmService`** | Business logic for the movie library: adding/removing likes, filtering by ratings/genres, returning the top movies. |
| **`GenreService`** | Retrieving information about movie genres. |
| **`MpaService`** | Retrieving information about MPA ratings. |

### 3. Presentation Layer (Controller Layer)
| Controller | Purpose |
| :--- | :--- |
| **`UserController`** | REST API for managing users and friendship relationships. |
| **`FilmController`** | REST API for movies, likes, and the top of popular movies. |
| **`GenreController`** | REST API for retrieving reference data about genres. |
| **`MpaController`** | REST API for retrieving reference data about MPA ratings. |

---

## 🛠 Technology Stack

- **Programming Language**: Java 21
- **Framework**: Spring Boot 3.x (Spring Web, Spring JDBC)
- **Database**: H2 Database (File & In-Memory modes)
- **Database Tools**: `org.springframework.boot:spring-boot-starter-jdbc` (`JdbcTemplate`)
- **HTTP Logging**: Zalando Logbook (`logbook-spring-boot-starter`)
- **Testing**: JUnit 5, AssertJ, Spring Boot Test (`@JdbcTest`, `@AutoConfigureTestDatabase`), Postman

---

## 🗄 Database Structure

The database schema is automatically initialized from the `schema.sql` script:

* **`users`**: User information cards (`user_id`, `email`, `login`, `name`, `birthday`).
* **`films`**: Movie catalog (`film_id`, `name`, `description`, `release_date`, `duration`, `mpa_id`).
* **`mpa_ratings`**: Age rating reference directory (`mpa_id`, `name`).
* **`genres`**: Genre reference directory (`genre_id`, `name`).
* **`film_genres`**: Linking table for movies and genres (`film_id`, `genre_id`).
* **`likes`**: Recording of user likes for movies (`film_id`, `user_id`).
* **`friendships`**: Friendship statuses and relationships between users (`user_id`, `friend_id`, `status`).

---

## 💡 Business Logic and REST API

### 1. Users and One-Sided Friendship (`/users`)
* **One-sided friendship**: When a user adds someone as a friend, the relationship is recorded only for the request initiator until confirmation.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/users` | Retrieving the list of all users. |
| `GET` | `/users/{id}` | Retrieving user data by ID. |
| `POST` | `/users` | Creating a new user. |
| `PUT` | `/users` | Updating user data. |
| `PUT` | `/users/{id}/friends/{friendId}` | Sending a request / adding as a friend. |
| `DELETE` | `/users/{id}/friends/{friendId}` | Removing from friends. |
| `GET` | `/users/{id}/friends` | List of the user's friends. |
| `GET` | `/users/{id}/friends/common/{otherId}` | List of common friends of two users. |

### 2. Movies and Likes (`/films`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/films` | Retrieving the list of all movies. |
| `GET` | `/films/{id}` | Retrieving a movie by ID. |
| `POST` | `/films` | Adding a movie (with `mpa` and a list of `genres`). |
| `PUT` | `/films` | Updating movie data. |
| `PUT` | `/films/{id}/like/{userId}` | Like a movie. |
| `DELETE` | `/films/{id}/like/{userId}` | Remove a like from a movie. |
| `GET` | `/films/popular?count={count}` | Top `count` most popular movies by likes (default: 10). |

### 3. Reference Directories: Genres and MPA Ratings

#### Genres (`/genres`):
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/genres` | Retrieving the list of all genres. |
| `GET` | `/genres/{id}` | Retrieving a genre by ID. |

*Example response for `/genres/1`:*
```json
{
  "id": 1,
  "name": "Comedy"
}
