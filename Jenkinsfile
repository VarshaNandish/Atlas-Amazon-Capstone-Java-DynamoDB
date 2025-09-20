pipeline {
  agent any

  environment {
    DYNAMODB_ENDPOINT = "http://dynamodb-local:8000"   // adjust if needed
    IMAGE_NAME = "atlas-app:${env.BUILD_ID}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit Tests') {
      steps {
        script {
          docker.image('maven:3.9.2-eclipse-temurin-17').inside {
            sh 'mvn -B -DskipITs=true test'
          }
        }
      }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }

    stage('Integration Tests') {
      steps {
        script {
          docker.image('maven:3.9.2-eclipse-temurin-17').inside {
            // ensure your tests use DYNAMODB_ENDPOINT (env injected by Jenkins)
            sh 'mvn -B -DskipITs=false verify'
          }
        }
      }
      post { always { junit 'target/failsafe-reports/*.xml' } }
    }

    stage('Package') {
      steps {
        script {
          docker.image('maven:3.9.2-eclipse-temurin-17').inside {
            sh 'mvn -B -DskipTests package'
          }
        }
      }
      post { success { archiveArtifacts artifacts: 'target/*-jar-with-dependencies.jar', fingerprint: true } }
    }

    stage('Build Docker Image') {
      when { expression { fileExists('Dockerfile') } }
      steps {
        // requires docker daemon access from this node (docker.sock mounted into Jenkins container)
        sh "docker build -t ${IMAGE_NAME} ."
      }
    }

    stage('Run App Container (smoke)') {
      when { expression { fileExists('Dockerfile') } }
      steps {
        // run with random host port mapping, then print mapped port
        sh "docker rm -f atlas-smoke || true"
        sh "docker run -d --rm --name atlas-smoke -P ${IMAGE_NAME}"
        // give container a sec, then show host port mapped to container 8080
        sh "sleep 5"
        sh "docker port atlas-smoke 8080 || true"
      }
      post { always { sh "docker rm -f atlas-smoke || true" } }
    }
  }

  post {
    success { echo "Build succeeded" }
    failure { echo "Build failed" }
  }
}
