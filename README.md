# Smart Waste Management and Recycling Platform

Smart bins report their fill level and waste type, the company collects only what is needed along optimized routes, and customers earn a share of the value of the recyclable and reusable materials found in their waste. Recovered materials are resold to the public and to recycling companies.

## Team
| ID | Name | GitHub | Services |
|---|---|---|---|
| 23101486 | Moaz Mahmoud Gaber Ibrahim | [username] | user-service, bin-service |
| [ID] | [Member 2] | [username] | collection-service, fleet-service |
| [ID] | [Member 3] | [username] | material-service, inventory-service |
| [ID] | [Member 4] | [username] | reward-service, payment-service |
| [ID] | [Member 5] | [username] | shop-service, marketplace-service |
| [ID] | [Member 6] | [username] | analytics-service, notification-service |

## Services and owners
| Service | Owner |
|---|---|
| user-service, bin-service | Member 1 |
| collection-service, fleet-service | Member 2 |
| material-service, inventory-service | Member 3 |
| reward-service, payment-service | Member 4 |
| shop-service, marketplace-service | Member 5 |
| analytics-service, notification-service | Member 6 |

## Planned tech stack
- Frontend: React
- Services: Python with FastAPI, one service per folder
- Database: PostgreSQL
- Bin readings: MQTT broker with a Python sensor simulator
- Routing: road-distance API and a nearest-neighbor plus 2-opt optimizer
- Prediction: scikit-learn
- Payments: payment gateway in test mode
- Notifications: email and SMS gateway in test mode
- DevOps: Docker Compose and GitHub Actions

## Planned folder structure
    smart-waste-platform/
        README.md
        docker-compose.yml
        frontend/
        services/
            user-service/
            bin-service/
            collection-service/
            fleet-service/
            material-service/
            inventory-service/
            reward-service/
            payment-service/
            shop-service/
            marketplace-service/
            analytics-service/
            notification-service/
        simulator/
        .github/workflows/
