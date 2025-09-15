@echo off
echo Starting File Sync Server with Docker Compose...

REM Check if .env file exists
if not exist secrets/.env (
    echo Creating .env file from example...
    copy .env.example .env
    echo Please edit the .env file with your configuration and run this script again.
    pause
    exit /b 1
)

REM Build and start containers
docker-compose up -d --build

echo Containers started successfully!
echo Application will be available at http://localhost:8080
pause