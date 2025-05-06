# Java Swing Quiz Application

A feature-rich quiz application built with Java Swing, implementing the MVC design pattern. This desktop application allows users to test their knowledge across various categories, while providing powerful administrative tools for content management, analytics, and user management.

## Features

### User Features
- **Account Management**: Register, login, and manage your profile
- **Quiz Selection**: Choose from multiple categories and difficulty levels
- **Interactive Quiz Experience**: User-friendly interface with timed questions
- **Score Tracking**: View your results and performance history
- **Leaderboards**: Compare your scores with other users

### Administrative Features
- **User Management**: View, edit, activate/deactivate, and manage user accounts
- **Content Management**: Create, edit, and organize categories and questions
- **Analytics Dashboard**: Track quiz completions, scores, and performance metrics
- **Import/Export**: Import and export questions in CSV and JSON formats
- **Content Moderation**: Review and approve user-submitted questions

## Technology Stack

- **Programming Language**: Java 11
- **UI Framework**: Java Swing with FlatLaf modern look-and-feel
- **Database**: MySQL
- **Connection Pooling**: HikariCP
- **Password Security**: jBCrypt
- **Logging**: Logback
- **Data Visualization**: JFreeChart
- **Data Formats**: Jackson (JSON), Apache Commons CSV

## Architecture

The application follows the Model-View-Controller (MVC) architectural pattern:

- **Model**: Represents the data and business logic
- **View**: User interfaces built with Java Swing
- **Controller**: Handles user input and updates the model and view

## Database Schema

The application uses a relational database with the following main tables:
- `users`: User account information
- `categories`: Quiz categories and subcategories
- `questions`: Quiz questions
- `answers`: Answers for each question
- `quiz_attempts`: Records of quiz attempts by users
- `question_responses`: Individual responses to questions
- `pending_questions`: User-submitted questions awaiting moderation
- `pending_answers`: Answers for pending questions

## Setup and Installation

### Prerequisites
- Java 11 or higher
- MySQL 8.0 or higher
- Maven

### Database Setup
1. Create a MySQL database named `quiz_db`
2. Use the following login credentials or update `config.properties`:
   - Username: `root`
   - Password: (your MySQL root password)

### Configuration
The application uses a `config.properties` file in the `src/main/resources` directory for database connection settings:

```properties
db.url=jdbc:mysql://localhost:3306/quiz_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

### Building the Application
1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```
   mvn clean package
   ```

### Running the Application
1. Run the compiled JAR file:
   ```
   java -jar target/quiz-application-1.0.0-SNAPSHOT.jar
   ```
2. The application will automatically create the required database tables on first run
3. Log in with the default admin account:
   - Username: `admin`
   - Password: `admin`

### Generating Test Data
To populate the database with sample data for testing:
```
java -cp target/quiz-application-1.0.0-SNAPSHOT.jar com.quizapp.TestDataMain
```

## Usage Guide

### User Interface
- **Login Screen**: Enter your username and password
- **Main Menu**: Navigate to different sections of the application
- **Quiz Selection**: Choose a category and start a quiz
- **Quiz Interface**: Answer questions within the time limit
- **Results Screen**: View your score and performance

### Admin Panel
- **Categories Tab**: Manage quiz categories and subcategories
- **Questions Tab**: Create, edit, and organize questions
- **User Management Tab**: Manage user accounts
- **Analytics Tab**: View performance metrics and charts
- **Import/Export Tab**: Import and export questions in different formats

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- FlatLaf for the modern look-and-feel
- JFreeChart for data visualization
- Apache Commons CSV for CSV processing
- Jackson for JSON processing