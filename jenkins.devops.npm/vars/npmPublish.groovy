import org.bcbsaz.Npm

void call(Map params) {

  Map config = [
    nodeVersion: '12'
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  Npm npm = new Npm(this)

  String volumes = ""

  Boolean volumesFound = volumesExists()

  if (volumesFound) {
    volumes = "-v ${BUILD_TAG}-tmp:/tmp -v ${BUILD_TAG}-local:/home/jenkins/.local"
  }

  

  String nodeImage = "devops/node:$config.nodeVersion"
  String registry = nexusRegistry.replace('7000', '5443')

  Integer statusCode = fetchDockerImage(registry, nodeImage)

  if (statusCode == 0) {
    docker.withRegistry(registry) {
      docker.image(nodeImage).inside(volumes) {
        npm.run(config.command)
      }
    }
  }

  else if (statusCode == 1) {
    buildDockerImage(nexusRegistry, nodeImage, config.nodeVersion).inside(volumes) {
      npm.run(config.command)
    }

    registry = nexusRegistry - ~'^http.*://'

    sh("docker image rm $nodeImage ${registry}/$nodeImage")
  }
  
}

Integer fetchDockerImage(String registry, String image) {
  registry = registry - ~'^http.*://'

  return sh(
    script: "docker image pull ${registry}/$image",
    returnStatus: true
  )
}

def buildDockerImage(String registry, String image, String nodeVersion) {
  writeFile(
    file: 'Dockerfile.node',
    text: libraryResource('org/bcbsaz/Dockerfile.npm')
  )

  String dockerBuildArgs = "--build-arg NODE_VERSION=$nodeVersion -f ./Dockerfile.node ."

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