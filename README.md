# Smart Waste Management and Recycling Platform

Smart bins report their fill level and waste type, the company collects only what is needed along optimized routes, and customers earn a share of the value of the recyclable and reusable materials found in their waste. Recovered materials are resold to the public and to recycling companies.

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
