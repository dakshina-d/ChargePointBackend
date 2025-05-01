# ChargePointBackend

## Overview
There are two Spring Boot microservices:

- **AuthenticationService** – Validates driver tokens based on a whitelist.
- **TransactionService** – Receives charging requests and delegates token authentication to the AuthenticationService via Kafka.


## Communication Flow
[Client] → [TransactionService REST API] → [Kafka Topic] → [AuthenticationService] → [Kafka Topic] → [TransactionService] → [Client]

## Tech Stack
- Java 17 (Spring Boot)
- Apache Kafka & Zookeeper
- Docker & Docker Compose

## The payload of a request:
{
  "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
  "driverIdentifier": { "id": "id1234" }
}


## Run the Application
-- There are two ways to run the Application

1. Cloning the repository
# Clone the Repository
git clone https://github.com/dakshina-d/ChargePointBackend.git
cd ChargePointBackend

# Build the Services Individually

-- Build the AuthenticationService:

cd AuthenticationService
mvn clean package
docker build -t authentication-service:1.0.0 .
cd ..

-- Build the TransactionService:

cd TransactionService
mvn clean package
docker build -t transaction-service:1.0.0 .
cd ..


# Run the application 

-- Use Docker Compose to start all containers (Zookeeper, Kafka, Authentication, Transaction):

docker-compose up

-- Used ports
Zookeeper on port 2181
Kafka on port 9092
AuthenticationService on port 8080
TransactionService on port 8081

Need to make available those ports to run the Application

# Running Tests in both Services

cd AuthenticationService
mvn test
cd ../TransactionService
mvn test






2. Pulling Docker images

# You can also pull prebuilt images directly from Docker Hub:

docker pull dakshina321/chagepoint-authentication:1.0.0
docker pull dakshina321/chagepoint-transaction-service:1.0.0


-- If you use this method, need to change the docker-compose.yml file as below.

"
version: '3.8'

services:
  zookeeper:
    image: 'confluentinc/cp-zookeeper:latest'
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - '2181:2181'

  kafka:
    image: 'confluentinc/cp-kafka:latest'
    depends_on:
      - zookeeper
    ports:
      - '9092:9092'
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:9092'
      KAFKA_LISTENERS: 'PLAINTEXT://0.0.0.0:9092'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  authentication-service:
    image: 'dakshina321/chagepoint-authentication:1.0.0'
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: 'kafka:9092'
    ports:
      - '8080:8080'

  transaction-service:
    image: 'dakshina321/chagepoint-transaction-service:1.0.0'
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: 'kafka:9092'
    ports:
      - '8081:8081'
"

-- Then run them
docker-compose up



## Sample Request

curl -X POST http://localhost:8081/transaction/authorize \
  -H "Content-Type: application/json" \
  -d '{
        "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
        "driverIdentifier": { "id": "12345678901234567890" }
      }'


## Sample Response:

{
  "authorizationStatus": "Accepted"
}


## Thoughts on Scaling and Improvements
1. Partition topics (e.g., by stationUuid or region) to parallelize processing and increase throughput.
2. Use Kafka replication to improve fault tolerance.
3. Introduce consumer groups for horizontal scaling of the AuthenticationService and TransactionService.
4. Introduce Schema Registry or Dead Letter Queue to handle invalid message formats
5. Use API Gateway to avoid the exposion the innser services and ports to the outside.
6. Add Rate-Limiting, circuit breakers to handle failures gracefully
7. Migrate the deployment from Docker Compose to Kubernetes
8. Spring Cloud LoadBalancer or integrate with Kubernetes Services for traffic distribution