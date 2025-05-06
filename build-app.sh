#!/bin/bash

echo "============================="
echo "Building Quiz Application..."
echo "============================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven not found! Please install Maven and ensure it's in your PATH."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java not found! Please install Java 11 or higher and ensure it's in your PATH."
    exit 1
fi

# Build the application with Maven
echo "Building the application with Maven..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Maven build failed! Please check the errors above."
    exit 1
fi

# Create distribution directory
echo "Creating distribution package..."
mkdir -p dist/lib

# Copy the JAR file
cp target/quiz-application-1.0.0-SNAPSHOT.jar dist/

# Create launcher script
cat > dist/run-quiz-app.sh << EOL
#!/bin/bash
echo "Starting Quiz Application..."
java -jar quiz-application-1.0.0-SNAPSHOT.jar
EOL

# Make launcher executable
chmod +x dist/run-quiz-app.sh

# Create README
cat > dist/README.txt << EOL
## Quiz Application

This is the Quiz Application, a Java Swing desktop application for creating and taking quizzes.

To start the application, run ./run-quiz-app.sh from the terminal.

Default admin credentials:
Username: admin
Password: admin

For more information, see the full documentation in the project repository.
EOL

echo "============================="
echo "Build completed successfully!"
echo "============================="
echo "The application has been packaged to the 'dist' directory."
echo "To run the application, go to the 'dist' directory and run './run-quiz-app.sh'."
echo 