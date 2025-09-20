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



    stage('Start DynamoDB') {
  steps {
    // start only the dynamodb service from your docker-compose (or docker run)
    sh '''
      echo "Starting dynamodb-local container..."
      docker-compose -f docker-compose.yml up -d dynamodb-local || docker run -d --name dynamodb-local -p 8000:8000 amazon/dynamodb-local
      # wait for tcp port 8000
      echo "Waiting for DynamoDB endpoint..."
      for i in $(seq 1 30); do
        if curl -sS --max-time 2 http://localhost:8000/ >/dev/null 2>&1; then
          echo "DynamoDB is up"
          break
        fi
        echo "waiting... ($i)"
        sleep 1
      done
    '''
  }
}



    stage('Integration Tests') {
  steps {
    sh "${tool 'Maven3'}/bin/mvn -B -DskipITs=false verify"
  }
  post {
    always {
      // debug: list report files and show first lines to help diagnose empty results
      sh '''
        echo "---- Listing failsafe-reports ----"
        ls -la target/failsafe-reports || true

        for f in target/failsafe-reports/*.xml; do
          if [ -f "$f" ]; then
            echo "---- $f ----"
            sed -n '1,80p' "$f" || true
          fi
        done
      '''
      // archive results but allow empty so pipeline doesn't fail
      junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true, keepLongStdio: true
    }
  }
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