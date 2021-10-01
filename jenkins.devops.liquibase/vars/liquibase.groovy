import org.bcbsaz.LiquibaseBuilder
import org.bcbsaz.Liquibase

void call(Map params) {
  Map config = [
    liquibaseVersion: '4.3.5',
    rootPath: 'changelogs',
    changelogFile: 'changelog.xml'
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  if (! config.databaseCredential) {
    error('databaseCredential was not specified')
  }

  Liquibase liquibase = new LiquibaseBuilder(this)
    .setRootPath(config.rootPath)
    .setConnectionString(config.connectionString)
    .setChangelogFile(config.changelogFile)
    .setCommand(config.command)
    .setGlobalArguments(config.globalArguments)
    .setArguments(config.arguments)
    .build()

  withCredentials([
    usernamePassword(
      credentialsId: config.databaseCredential,
      usernameVariable: 'DB_USERNAME',
      passwordVariable: 'DB_PASSWORD'
    )
  ]) {

    String liquibaseImage = "devops/liquibase:$config.liquibaseVersion"
    String registry = nexusRegistry.replace('7000', '5443')

    Integer statusCode = fetchDockerImage(registry, liquibaseImage)

    if (statusCode == 0) {
      docker.withRegistry(registry) {
        docker.image(liquibaseImage).inside {
          liquibase.executeCommand()
        }
      }
    }

    else if (statusCode == 1) {
      buildDockerImage(nexusRegistry, liquibaseImage, config.liquibaseVersion).inside {
        liquibase.executeCommand()
      }

      registry = nexusRegistry - ~'^http.*://'

      sh("docker image rm $liquibaseImage ${registry}/$liquibaseImage")
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

def buildDockerImage(String registry, String image, String liquibaseVersion) {
  writeFile(
    file: 'Dockerfile.liquibase',
    text: libraryResource('org/bcbsaz/Dockerfile.liquibase')
  )

  String dockerBuildArgs = "--build-arg LIQUIBASE_VERSION=$liquibaseVersion -f ./Dockerfile.liquibase ."

  docker.withRegistry(registry) {
    def dockerImage = docker.build(image, dockerBuildArgs)
    dockerImage.push()

    return dockerImage
  }
}
