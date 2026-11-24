pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        SONAR_HOST_URL = 'https://sonarcloud.io'
        REGISTRY = 'ghcr.io/moaz1117'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Unit Tests & Coverage') {
            steps {
                sh 'mvn -f services/bin-service/pom.xml clean test jacoco:report'
                sh 'mvn -f services/user-service/pom.xml clean test jacoco:report'
            }
            post {
                always {
                    junit 'services/*/target/surefire-reports/*.xml'
                    jacoco(execPattern: 'services/*/target/jacoco.exec')
                }
            }
        }

        stage('SonarQube Quality Gate') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn -f services/bin-service/pom.xml org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=Moaz1117_ReUcle_bin-service \
                          -Dsonar.organization=moaz1117 \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.token=${SONAR_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.qualitygate.wait=true

                        mvn -f services/user-service/pom.xml org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=Moaz1117_ReUcle_user-service \
                          -Dsonar.organization=moaz1117 \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.token=${SONAR_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.qualitygate.wait=true
                    '''
                }
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = [
                        'user-service', 'bin-service', 'collection-service', 'fleet-service',
                        'material-service', 'inventory-service', 'reward-service', 'payment-service',
                        'shop-service', 'marketplace-service', 'analytics-service', 'notification-service'
                    ]
                    withCredentials([usernamePassword(credentialsId: 'GHCR_CREDENTIALS', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh 'echo $PASS | docker login ghcr.io -u $USER --password-stdin'
                        for (svc in services) {
                            sh "docker build -t ${REGISTRY}/reucle-${svc}:build-${env.BUILD_NUMBER} ./services/${svc}"
                            sh "docker push ${REGISTRY}/reucle-${svc}:build-${env.BUILD_NUMBER}"
                        }
                    }
                }
            }
        }
    }
}
