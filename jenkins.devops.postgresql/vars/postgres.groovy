import org.bcbsaz.PostgresBuilder
import org.bcbsaz.Postgres

void call(Map params) {

  Map config = [
    postgresVersion: '13',
    port: '5432',
    sslmode: 'require',
    activeDirectoryAdmin: '',
    spCredential: 'AzureSPNonProd'
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  Postgres postgres = new PostgresBuilder(this)
    .setArguments(config.arguments)
    .build()

  withCredentials([
    azureServicePrincipal(
      credentialsId:          config.spCredential,
      subscriptionIdVariable: 'ARM_SUBSCRIPTION_ID',
      clientIdVariable:       'ARM_CLIENT_ID',
      clientSecretVariable:   'ARM_CLIENT_SECRET',
      tenantIdVariable:       'ARM_TENANT_ID'
    )]) { 

  config.put('ARM_SUBSCRIPTION_ID', ARM_SUBSCRIPTION_ID)
  config.put('ARM_CLIENT_ID', ARM_CLIENT_ID)
  config.put('ARM_CLIENT_SECRET', ARM_CLIENT_SECRET)
  config.put('ARM_TENANT_ID', ARM_TENANT_ID)

  String volumes = ""

  Boolean volumesFound = volumesExists()

  if (volumesFound) {
    volumes = "-v ${BUILD_TAG}-tmp:/tmp -v ${BUILD_TAG}-local:/home/jenkins/.local"
  }

  

  String postgresImage = "devops/postgres:$config.postgresVersion"
  String registry = nexusRegistry.replace('7000', '5443')

  Integer statusCode = fetchDockerImage(registry, postgresImage)

  if (statusCode == 0) {
    docker.withRegistry(registry) {
      docker.image(postgresImage).inside(volumes) {
       
       postgres.executeCommand(config.host, config.port, config.dbname, config.activeDirectoryAdmin, config.sslmode)
     
        }
    }
  }

  else if (statusCode == 1) {
    buildDockerImage(nexusRegistry, postgresImage, config.postgresVersion).inside(volumes) {
     postgres.executeCommand(config.host, config.port, config.dbname, config.activeDirectoryAdmin, config.sslmode)
        
    }

    registry = nexusRegistry - ~'^http.*://'

    sh("docker image rm $postgresImage ${registry}/$postgresImage")
  }
  
}
}

Integer fetchDockerImage(String registry, String image) {
  registry = registry - ~'^http.*://'

  return sh(
    script: "docker image pull ${registry}/$image",
    returnStatus: true
  )
}

def buildDockerImage(String registry, String image, String postgresVersion) {
  writeFile(
    file: 'Dockerfile.postgres',
    text: libraryResource('org/bcbsaz/Dockerfile.postgres')
  )

  String dockerBuildArgs = "--build-arg POSTGRES_VERSION=$postgresVersion -f ./Dockerfile.postgres ."

  docker.withRegistry(registry) {
    def dockerImage = docker.build(image, dockerBuildArgs)
    dockerImage.push()

    return dockerImage
  }
}

String volumesExists() {
  sh(
    returnStatus: true,

    script: """#!/usr/bin/env bash

      TMP_VOLUME=`
        docker volume ls \
          --filter name=\${BUILD_TAG}-tmp \
          --format '{{.Name}}'
      `

      LOCAL_VOLUME=`
        docker volume ls \
          --filter name=\${BUILD_TAG}-local \
          --format '{{.Name}}'
      `

      VOLUMES_FOUND=1

      [[ -z \$TMP_VOLUME || -z \$LOCAL_VOLUME ]] && VOLUMES_FOUND=0

      exit \$VOLUMES_FOUND
    """
  )
}
