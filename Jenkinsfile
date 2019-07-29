pipeline {
  agent {
    label 'slave'
  }
  stages {
    stage('Generate License Report') {
      agent {
        docker {
          image 'openjdk:8'
          reuseNode true
        }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-maven-ebf-releases-deployment', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh './gradlew clean build publish -Pnexus_user=$USER -Pnexus_pass=$PASS'
        }
      }
    }
  }
}

