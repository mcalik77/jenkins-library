import org.bcbsaz.FlywayBuilder
import org.bcbsaz.Flyway


void call(Map params) {
  Map config = [
    flywayVersion: '7.15',
    databaseCredential: 'databaseCredential',
    licenseCredential: '',
    command: ''
    
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  if (! config.databaseCredential) {
    error('databaseCredential was not specified')
  }

  Flyway flyway = new FlywayBuilder(this)
    .setCommand(config.command)
    .build()

  withCredentials([
    usernamePassword(
      credentialsId: config.databaseCredential,
      usernameVariable: 'DB_USERNAME',
      passwordVariable: 'DB_PASSWORD'
    ),
    string(credentialsId: config.licenseCredential, variable: 'licenseKey')
  ]) {

    String flywayImage = "devops/flyway:$config.flywayVersion"
    String registry = nexusRegistry.replace('7000', '5443')

    Integer statusCode = fetchDockerImage(registry, flywayImage)

    if (statusCode == 0) {
      docker.withRegistry(registry) {
        docker.image(flywayImage).inside {
          flyway.executeCommand(config.command, config.url, config.locations, config.schemas)
        }
      }
    }

    else if (statusCode == 1) {
      buildDockerImage(nexusRegistry, flywayImage, config.flywayVersion).inside {
        flyway.executeCommand(config.command, config.url, config.locations, config.schemas)
      }

      registry = nexusRegistry - ~'^http.*://'

      sh("docker image rm $flywayImage ${registry}/$flywayImage")
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

def buildDockerImage(String registry, String image, String flywayVersion) {
  writeFile(
    file: 'Dockerfile.flyway',
    text: libraryResource('org/bcbsaz/Dockerfile.flyway')
  )

  String dockerBuildArgs = "--build-arg FLYWAY_VERSION=$flywayVersion -f ./Dockerfile.flyway ."

  docker.withRegistry(registry) {
    def dockerImage = docker.build(image, dockerBuildArgs)
    dockerImage.push()

    return dockerImage
  }
}