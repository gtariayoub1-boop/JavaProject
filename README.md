# PIDEV_JAVA

JavaFX desktop application for user management with role-based dashboards for administrators, teachers, and students.

## Features

- Login screen with role-based navigation
- Admin dashboard for user management and statistics
- Teacher dashboard with profile view, student profile list, and password change
- Student dashboard with profile view and password change
- Forgot password screen
- MySQL-backed user data

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- MySQL Connector/J
- Java Persistence API
- Java Validation API

## Project Structure

- `src/main/java/entities`: user entities
- `src/main/java/gui`: JavaFX controllers
- `src/main/java/services`: authentication and data services
- `src/main/java/utils`: database, scene, and session helpers
- `src/main/resources/gui`: FXML views and shared CSS

## Requirements

- JDK 17
- Maven
- MySQL running locally
- PHP available at `C:\xampp\php\php.exe`

Password hashing and verification currently use PHP from XAMPP inside `AuthService`.

## Database Configuration

The app currently connects with these defaults from `DBConnection`:

- Host: `127.0.0.1`
- Port: `3306`
- Database: `gestion_utilisateur`
- User: `root`
- Password: empty

Update `src/main/java/utils/DBConnection.java` if your local database settings are different.

## Run

Using Maven:

```bash
mvn javafx:run
```

If Maven is not installed globally, install Maven first or add a Maven wrapper to the project.

## Entry Point

Application entry point:

- `src/main/java/main/Main.java`

The app opens the login screen first:

- `src/main/resources/gui/login.fxml`

## Notes

- This repository currently contains the `user` branch in the remote.
- There was no `main` branch in the repo at the time this README was added.
- Adding this README does not damage your work. It is just another tracked file committed with your project history.
