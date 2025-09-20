pipeline {
  agent any

  tools {
    // make sure this name matches your Jenkins configured Maven tool name
    maven 'Maven3'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Unit Tests') {
      steps {
        // run only Surefire (unit tests)
        sh "${tool 'Maven3'}/bin/mvn -B -DskipITs=true test"
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        // Important: skip Surefire here so unit tests are NOT re-run.
        // Run failsafe lifecycle directly to execute integration tests only.
        sh "${tool 'Maven3'}/bin/mvn -B -DskipTests=true failsafe:integration-test failsafe:verify"
      }
      post {
        always {
          // debug output to help diagnose test report issues
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
          junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true, keepLongStdio: true
        }
      }
    }

    stage('Package') {
      steps {
        // package without running tests (they already ran)
        sh "${tool 'Maven3'}/bin/mvn -B -DskipTests package"
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*-jar-with-dependencies.jar', fingerprint: true
        }
      }
    }
  }

     stage('Build Docker Image') {
  steps {
    script {
      // image name & tag
      def imageName = "mydockerhubuser/atlas-capstone"
      def imageTag = "${env.BRANCH_NAME ?: 'local'}-${env.BUILD_NUMBER ?: '0'}"

      sh "docker build -t ${imageName}:${imageTag} ."

      // optional: show local images
      sh "docker images | grep atlas-capstone || true"

      // optional: push to registry (uncomment if you add credentials)
      // withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
      //   sh "echo $DOCKER_PASS | docker login --username $DOCKER_USER --password-stdin"
      //   sh "docker push ${imageName}:${imageTag}"
      // }
    }
  }
}


  post {
    success { echo "Build succeeded" }
    failure { echo "Build failed" }
  }
}
