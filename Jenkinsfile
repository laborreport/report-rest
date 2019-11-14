#!groovy
properties([disableConcurrentBuilds()])
pipeline {
    agent { 
        label 'master'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10')) //хранить логи 10 сборок и артефактов
        timestamps() //временные отметки 
    }
    environment {
        serviceName = 'laborreport'
        deployServerIp = "${DEPLOY_SERVER_IP}"
        deployServerHostname = "${DEPLOY_SERVER_HOSTNAME}"
        deployServerCredential = "${DEPLOY_SERVER_CREDENTIAL}"
        registryAddress = "${REGISTRY_ADDRESS}"
        nameImage = 'report-rest'
        numberBuild = "${env.BUILD_NUMBER}"
    }
    
    stages {
       // stage('Build and push images') {
         //   steps {
           //     script {
             //       docker.withRegistry("https://$registryAddress") {
               //         def customImage = docker.build(nameImage, "-f ./docker/Dockerfile .")
                 //       customImage.push(numberBuild)
                   //     customImage.push("latest")
                  //  }
                        
               // }
           // }
        //}

       // stage('Remove docker images') {
         //   steps {
           //     sh "docker rmi -f $registryAddress/$nameImage:$numberBuild"
             //   sh "docker rmi -f $registryAddress/$nameImage:latest"
               // sh "docker rmi -f $nameImage:$numberBuild"
               // sh "docker rmi -f $nameImage:latest"
           // }
       // }

        stage(Deploy) {
            steps {
                script {
                    def remote = [:]
                    remote.name = deployServerHostname
                    remote.allowAnyHosts = true
                    withCredentials([usernamePassword(credentialsId: deployServerCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                            remote.user = USERNAME
                            remote.host = deployServerIp
                            remote.password = PASSWORD
                            withCredentials([file(credentialsId: 'dcrr', variable: 'DC_FILE')]) {
                                sh "hostname"
                                sh "pwd"
                                sh "cp ${DC_FILE} docker-compose.yml"
                            }
                            stage("Deploy docker container"){
                                //sshCommand remote: remote, command: "docker-compose rm -f -s -v $service"
                                //sshCommand remote: remote, command: "docker rmi -f $registryAddress/$nameImage:latest"
                                sshCommand remote: remote, command: "docker-compose pull $service && docker-compose up -d $service"
                            }
                    }
                }
            }
        }
    }
}
