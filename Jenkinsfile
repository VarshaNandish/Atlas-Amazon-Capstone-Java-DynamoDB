pipeline {
  agent any

  tools {
    maven 'Maven3'   // name must match what you configured in Jenkins Tools
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit Tests') {
      steps {
        sh "mvn -B -DskipITs=true test"
      }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }

    stage('Integration Tests') {
      steps {
        sh "mvn -B -DskipITs=false verify"
      }
      post { always { junit 'target/failsafe-reports/*.xml' } }
    }

    stage('Package') {
      steps {
        sh "mvn -B -DskipTests package"
      }
      post { success { archiveArtifacts artifacts: 'target/*-jar-with-dependencies.jar', fingerprint: true } }
    }
  }

  post {
    success { echo "Build succeeded" }
    failure { echo "Build failed" }
  }
}