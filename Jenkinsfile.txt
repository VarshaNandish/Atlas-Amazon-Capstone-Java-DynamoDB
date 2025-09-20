pipeline {
  agent any

  environment {
    // Jenkins will run inside docker-compose network; DynamoDB host is service name
    DYNAMODB_ENDPOINT = "http://dynamodb-local:8000"
    IMAGE_NAME = "atlas-app:${env.BUILD_ID}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'mvn -B -DskipITs=true test'
      }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }

    stage('Integration Tests') {
      steps {
        // integration tests should pick up DYNAMODB_ENDPOINT from environment
        sh 'mvn -B -DskipITs=false test'
      }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }

    stage('Build Docker Image') {
      steps {
        // build docker image on host docker daemon (requires docker.sock mount in jenkins container)
        sh "docker build -t ${IMAGE_NAME} ."
      }
    }

    stage('Run App Container (smoke)') {
      steps {
        // run briefly; map container port 8080 to a random host port to avoid collisions
        sh "docker run -d --rm --name atlas-smoke -p 0:8080 ${IMAGE_NAME}"
        // simple wait & health check - adjust as needed
        sh "sleep 5"
        // optional: curl health endpoint (if your app exposes one). Comment out if none.
        // sh "curl -f http://localhost:8080/actuator/health || true"
      }
      post {
        always {
          sh "docker rm -f atlas-smoke || true"
        }
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }
  }

  post {
    success { echo "Build succeeded" }
    failure { echo "Build failed" }
  }
}
