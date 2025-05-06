@echo off
echo =============================
echo Building Quiz Application...
echo =============================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Maven not found! Please install Maven and ensure it's in your PATH.
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Java not found! Please install Java 11 or higher and ensure it's in your PATH.
    exit /b 1
)

REM Build the application with Maven
echo Building the application with Maven...
call mvn clean package

if %ERRORLEVEL% neq 0 (
    echo Maven build failed! Please check the errors above.
    exit /b 1
)

REM Create distribution directory
echo Creating distribution package...
mkdir dist 2>nul
mkdir dist\lib 2>nul

REM Copy the JAR file
copy target\quiz-application-1.0.0-SNAPSHOT.jar dist\ >nul

REM Create launcher script
echo @echo off > dist\run-quiz-app.bat
echo echo Starting Quiz Application... >> dist\run-quiz-app.bat
echo java -jar quiz-application-1.0.0-SNAPSHOT.jar >> dist\run-quiz-app.bat

REM Create README
echo ## Quiz Application >> dist\README.txt
echo >> dist\README.txt
echo This is the Quiz Application, a Java Swing desktop application for creating and taking quizzes. >> dist\README.txt
echo >> dist\README.txt
echo To start the application, double-click on run-quiz-app.bat or run it from the command line. >> dist\README.txt
echo >> dist\README.txt
echo Default admin credentials: >> dist\README.txt
echo Username: admin >> dist\README.txt
echo Password: admin >> dist\README.txt
echo >> dist\README.txt
echo For more information, see the full documentation in the project repository. >> dist\README.txt

echo =============================
echo Build completed successfully!
echo =============================
echo The application has been packaged to the 'dist' directory.
echo To run the application, go to the 'dist' directory and run 'run-quiz-app.bat'.
echo. 