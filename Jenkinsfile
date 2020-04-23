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
        stage('Build and push images') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'jrxml_file', variable: 'JRXML_FILE')]){
                        sh "cp ${JRXML_FILE} ./src/main/resources/template/akt.jrxml"
                    }
                    docker.withRegistry("https://$registryAddress") {
                        def customImage = docker.build(nameImage, "-f ./docker/Dockerfile .")
                        customImage.push(numberBuild)
                        customImage.push("latest")
                    }       
                }
            }
        }

        stage('Remove docker images') {
            steps {
                sh "docker rmi -f $registryAddress/$nameImage:$numberBuild"
                sh "docker rmi -f $registryAddress/$nameImage:latest"
                sh "docker rmi -f $nameImage:$numberBuild"
                sh "docker rmi -f $nameImage:latest"
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def remote = [:]
                    remote.name = deployServerHostname
                    remote.allowAnyHosts = true
                    node {
                        withCredentials([usernamePassword(credentialsId: deployServerCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                            remote.user = USERNAME
                            remote.host = deployServerIp
                            remote.password = PASSWORD
                            stage('Deploy docker container') {
                                sshCommand remote: remote, failOnError: false, command: "docker-compose rm -f -s -v laborreport"
                                sshCommand remote: remote, failOnError: false, command: "docker rmi -f $registryAddress/$nameImage:latest"
                                sshCommand remote: remote, command: "docker-compose up -d"
                            }
                        }
                    }
                }
            }
        }
    }
}
