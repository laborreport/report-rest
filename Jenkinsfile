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
        registryAddress = "${REGISTRY_ADDRESS}"
        nameImage = 'report-rest'
        numberBuild = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Build and push images') {
            steps {
                script {
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

        stage(Deploy) {
            steps {
                build job: 'deploy-service-laborreport' , parameters: [
                    string(name: 'service', value: serviceName)
                ]
            }
        }
    }
}
