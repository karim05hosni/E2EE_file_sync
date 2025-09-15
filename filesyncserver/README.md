# File Sync Server

## Docker Setup

This project includes Docker configuration for easy setup and deployment.

### Prerequisites

- Docker and Docker Compose installed on your machine
- Java 21 (for local development)

### Environment Variables

Before running the application with Docker, you need to set up your environment variables:

1. Copy the example environment file to create your own:
   ```
   cp .env.example .env
   ```

2. Edit the `.env` file with your specific configuration values:
   ```
   # Database Configuration
   DB_NAME=file_sync
   DB_USERNAME=myuser
   DB_PASSWORD=your_secure_password
   DB_URL=localhost
   DB_PORT=5432

   # JWT Configuration
   JWT_PRIVATE_KEY=your_jwt_private_key_here

   # Application Configuration
   SERVER_PORT=8080
   ```

### Running with Docker

1. Build and start the containers:
   ```
   docker-compose up -d
   ```

2. To stop the containers:
   ```
   docker-compose down
   ```

3. To view logs:
   ```
   docker-compose logs -f
   ```

### Accessing the Application

Once the containers are running, you can access the application at:
- API: http://localhost:8080/api

### Database

The PostgreSQL database is accessible at:
- Host: localhost
- Port: 5432
- Database: file_sync (or the value you set in DB_NAME)
- Username: myuser (or the value you set in DB_USERNAME)
- Password: The value you set in DB_PASSWORD

### Volumes

The Docker setup includes persistent volumes for:
- PostgreSQL data: All database data is stored in a named volume for persistence between container restarts

### Networks

All services are connected through a custom bridge network named `filesync-network`.