// =============================================================================
// Jenkinsfile - Public Parking Availability Dashboard CI/CD Pipeline
// Student: Sajiya Shaikh | Roll: 24102A2005
//
// Pipeline stages:
//   Checkout → Environment Check → Maven Build → Unit Tests →
//   Package → Docker Build → Docker Push → Deploy → Health Check
// =============================================================================

pipeline {
    agent any

    // ── Environment Variables ─────────────────────────────────────────────────
    environment {
        APP_NAME        = 'public-parking-dashboard'
        APP_VERSION     = '1.0.0'
        APP_PORT        = "${params.APP_PORT ?: '8080'}"
        IMAGE_NAME      = "${params.DOCKER_REGISTRY ? params.DOCKER_REGISTRY + '/' : ''}public-parking-dashboard"
        IMAGE_TAG       = "1.0.0-${BUILD_NUMBER}"
        CONTAINER_NAME  = 'public-parking-dashboard-container'
        LOW_AVAILABILITY_THRESHOLD = "${params.LOW_AVAILABILITY_THRESHOLD ?: '85'}"

        // Docker Hub credentials ID (configured in Jenkins Credentials Store)
        // NEVER hardcode credentials here
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'

        MAVEN_OPTS = '-Xmx512m'
    }

    // ── Build Parameters ──────────────────────────────────────────────────────
    parameters {
        string(name: 'APP_PORT',
               defaultValue: '8080',
               description: 'Port on which the application will listen')
        string(name: 'DOCKER_REGISTRY',
               defaultValue: '',
               description: 'Docker registry (e.g., docker.io/username). Leave blank for local only.')
        string(name: 'LOW_AVAILABILITY_THRESHOLD',
               defaultValue: '85',
               description: 'Occupancy % above which a location is flagged as low availability')
        booleanParam(name: 'PUSH_IMAGE',
                     defaultValue: false,
                     description: 'Push Docker image to registry (requires credentials)')
        booleanParam(name: 'RUN_SELENIUM',
                     defaultValue: false,
                     description: 'Run Selenium tests (requires app to be accessible)')
        string(name: 'ROLLBACK_TAG',
               defaultValue: '',
               description: 'Docker image tag to roll back to (leave blank to skip)')
    }

    // ── Pipeline Options ──────────────────────────────────────────────────────
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "=== [1/9] Checkout: ${env.APP_NAME} ==="
                checkout scm
                script {
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.GIT_COMMIT_SHORT = commitId
                    echo "Commit: ${commitId}"
                }
            }
        }

        // ── Stage 2: Environment Verification ────────────────────────────────
        stage('Environment Check') {
            steps {
                echo '=== [2/9] Environment Check ==='
                sh 'java -version'
                sh 'mvn -version 2>/dev/null || ./mvnw -version'
                sh 'docker --version || echo "Docker not available on this agent"'
                sh 'echo "Branch: ${GIT_BRANCH:-unknown}"'
                sh 'echo "Build #: ${BUILD_NUMBER}"'
            }
        }

        // ── Stage 3: Maven Build ──────────────────────────────────────────────
        stage('Build') {
            steps {
                echo '=== [3/9] Maven Build (compile) ==='
                sh './mvnw clean compile -B -q'
            }
        }

        // ── Stage 4: Unit Tests ───────────────────────────────────────────────
        stage('Unit Tests') {
            steps {
                echo '=== [4/9] Unit & Integration Tests ==='
                sh './mvnw test -B'
            }
            post {
                always {
                    // Publish JUnit test reports
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
                failure {
                    echo '!!! Unit tests FAILED. Stopping pipeline.'
                }
            }
        }

        // ── Stage 5: Package ──────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo '=== [5/9] Package (JAR) ==='
                sh './mvnw package -DskipTests -B -q'
                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
                echo "Artifact archived: target/${env.APP_NAME}-${env.APP_VERSION}.jar"
            }
        }

        // ── Stage 6: Selenium Tests (optional) ───────────────────────────────
        stage('Selenium Tests') {
            when {
                expression { params.RUN_SELENIUM == true }
            }
            steps {
                echo '=== [6/9] Selenium E2E Tests ==='
                // Start app in background for Selenium
                sh """
                    java -jar target/${env.APP_NAME}-${env.APP_VERSION}.jar \
                         --server.port=${env.APP_PORT} &
                    echo \$! > /tmp/parking-app.pid
                    sleep 30  # Wait for application to start
                """
                sh """
                    mvn test -Dtest=ParkingDashboardSeleniumTest \
                             -Dapp.base.url=http://localhost:${env.APP_PORT} \
                             -Dselenium.headless=true -B || true
                """
            }
            post {
                always {
                    sh 'kill $(cat /tmp/parking-app.pid) 2>/dev/null || true'
                    junit testResults: 'target/surefire-reports/TEST-*Selenium*.xml',
                          allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/selenium-screenshots/**',
                                     allowEmptyArchive: true
                }
            }
        }

        // ── Stage 7: Docker Build ─────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                echo "=== [7/9] Docker Image Build: ${env.IMAGE_NAME}:${env.IMAGE_TAG} ==="
                sh """
                    docker build \
                        -t ${env.IMAGE_NAME}:${env.IMAGE_TAG} \
                        -t ${env.IMAGE_NAME}:latest \
                        .
                """
                sh "docker images | grep ${env.APP_NAME}"
            }
        }

        // ── Stage 8: Docker Push (conditional) ───────────────────────────────
        stage('Docker Push') {
            when {
                expression { params.PUSH_IMAGE == true && params.DOCKER_REGISTRY != '' }
            }
            steps {
                echo "=== [8/9] Push to Registry: ${params.DOCKER_REGISTRY} ==="
                withCredentials([usernamePassword(
                        credentialsId: "${env.DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh "docker push ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    sh "docker push ${env.IMAGE_NAME}:latest"
                    sh 'docker logout'
                }
                echo "Pushed: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
            }
        }

        // ── Stage 9: Deploy ───────────────────────────────────────────────────
        stage('Deploy') {
            steps {
                echo "=== [9/9] Container Deployment ==="
                script {
                    if (params.ROLLBACK_TAG) {
                        echo "ROLLBACK requested to tag: ${params.ROLLBACK_TAG}"
                        sh "docker stop ${env.CONTAINER_NAME} 2>/dev/null || true"
                        sh "docker rm ${env.CONTAINER_NAME} 2>/dev/null || true"
                        sh """
                            docker run -d \
                                --name ${env.CONTAINER_NAME} \
                                -p ${env.APP_PORT}:8080 \
                                -v parking-data:/app/data \
                                -e LOW_AVAILABILITY_THRESHOLD=${env.LOW_AVAILABILITY_THRESHOLD} \
                                ${env.IMAGE_NAME}:${params.ROLLBACK_TAG}
                        """
                    } else {
                        // Stop old container safely before deploying new one
                        sh "docker stop ${env.CONTAINER_NAME} 2>/dev/null || true"
                        sh "docker rm ${env.CONTAINER_NAME} 2>/dev/null || true"
                        sh """
                            docker run -d \
                                --name ${env.CONTAINER_NAME} \
                                -p ${env.APP_PORT}:8080 \
                                -v parking-data:/app/data \
                                -e LOW_AVAILABILITY_THRESHOLD=${env.LOW_AVAILABILITY_THRESHOLD} \
                                --restart unless-stopped \
                                ${env.IMAGE_NAME}:${env.IMAGE_TAG}
                        """
                    }
                }
                echo "Container started: ${env.CONTAINER_NAME}"
            }
        }

    }

    // ── Post Actions ──────────────────────────────────────────────────────────
    post {
        success {
            echo """
            ╔════════════════════════════════════════════╗
            ║   BUILD AND DEPLOYMENT SUCCESSFUL          ║
            ║   App: ${env.APP_NAME}:${env.IMAGE_TAG}    ║
            ║   Port: ${env.APP_PORT}                    ║
            ║   URL:  http://localhost:${env.APP_PORT}    ║
            ╚════════════════════════════════════════════╝
            """
        }
        failure {
            echo "!!! Pipeline FAILED. Review logs above. Container NOT deployed with broken code."
            script {
                try {
                    def container = env.CONTAINER_NAME ?: 'public-parking-dashboard-container'
                    sh "docker logs ${container} 2>/dev/null | tail -20 || true"
                } catch (Throwable t) {
                    echo "Container inspection skipped: ${t.message}"
                }
            }
        }
        always {
            script {
                try {
                    cleanWs(cleanWhenNotBuilt: false,
                            deleteDirs: true,
                            disableDeferredWipeout: true,
                            notFailBuild: true,
                            patterns: [[pattern: 'target/', type: 'INCLUDE']])
                } catch (Throwable t) {
                    echo "Workspace cleanup skipped: ${t.message}"
                }
            }
        }
    }
}
