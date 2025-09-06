NewellGames is a web application built with Java Spring Boot that simulates a game shop platform. It follows a microservice-friendly architecture, with the main app handling users, games, carts, reviews, and transactions, while a separate Notification Microservice manages all user notifications.
It allows regular users to:

- Browse and search for games

- Top up their accounts (with transaction logging)

- Add/remove games from a shopping cart

- Purchase games from their shopping cart (with transaction logging)

- Ability to write a positive/negative review to any owned game, they also can see all reviews for the given game or all of the reviews they made

- Users can manage notification preferences (enable/disable). Notifications require an email to be set. Users also see a small history of the notifications they received (5 last notifications). They have an ability to clear this list or to retry all notifications with status (FAILED)

- Users can change their account information (Username/Email/ProfilePicture) 

- Receive purchase/deposit/edit profile/leaving a review notifications in form of emails using google SMTP.

Additional functionalities for users with ADMIN role to:

- See a list with all transactions that users made and a search bar where Admin can enter Username/ID of the user or transaction ID to quickly find needed transaction

- See a list with all users in the DB and ability to switch their status (active/inactive) also providing them with (ADMIN) privileges (a switch button USER/ADMIN) and a search bar where Admin can search by Username/ID of the user for easier accessibility.

This project demonstrates backend development with Spring Boot, JPA/Hibernate, and testing with JUnit & MockMvc.

Tech Stack:

- Java 17

- Spring Boot 3.x (Web, Data JPA, Security, Validation)

- Hibernate / JPA

- MySQL database (as a main DB)

- H2 Database (for dev & testing)

- Maven (build tool)

- JUnit 5, Mockito, MockMvc (testing)

- Google SMTP

- Notification Microservice (Spring Boot service for sending and retrying notifications, also creates notification preferences for users and handles the CRUD operations for notifications, decoupled from main app)

- Docker

- Swagger (Small documentation of REST API endpoints)

Future improvements:

- Separating UserService (UserService is large), planned to be split into smaller services

- Implement a new feature for Users Wishlist

- Add an option of obtaining games through Access Keys (enter the key -> get the game without purchasing)

- Improve UI (HTML/CSS)

- Create a server from where users will download games

- More tests (API/Integration/Unit)

- More custom exceptions

- Implement Kafka

How to run:

- Make sure the following are installed on the machine:

  - Docker

  - Docker Compose

- Application ports:

    - Main app: http://localhost:8080/
    - Notification-svc: http://localhost:8081/
    - Mailhog: http://localhost:8025/
    - Swagger: http://localhost:8081/swagger-ui/index.html

- Mailhog: In my Demo I am using mailhog so you can see all emails the app is sending, in order to access it, just go to the port http://localhost:8025/ and you will be able to see the emails (by default every user has notifications DISABLED, if you want to see the emails you'll have to go to the Notifications tab in the navigation header and ENABLE notifications).

- When application is running it will initialize 2 users and 3 games so you can easily test the functionalities:
  
    - User (Admin):
      - Username: admin
      - Password: admin123
      - Balance: 500
      - Email: admin@newellgames.com
      - Description: Registered user with Admin authentication
    - User (User):
      - Username: john
      - Password: password
      - Balance: 500
      - Email: john@example.com
      - Description: Registered user with User authentication

- Run these commands to run/stop the app:

```markdown
- Run these commands:  
  - `docker compose up --build` → start the app  
  - `docker compose down` → stop the app
