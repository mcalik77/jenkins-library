import org.bcbsaz.helm.HelmChartBuilder
import org.bcbsaz.helm.HelmChart

void call(Map params) {

  Map config = [
    helmVersion: '3.5.3',
    repositoryType: 'nexus',

    nexusCredential: 'Nexus',
    nexusRepository: 'http://nexus.azblue.com:8081/repository/helm-hosted/'
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  HelmChart helmChart = new HelmChartBuilder(this)
    .setRepositoryType(config.repositoryType)
    .setChartName(config.chartName)
    .setChartVersion(config.chartVersion)
    .setAzureRegistry(config.azureRegistry)
    .setNexusRepository(config.nexusRepository)
    .build()

  String helmImage = "devops/helm:$config.helmVersion"
  String registry = nexusRegistry.replace('7000', '5443')

  Integer statusCode = fetchDockerImage(registry, helmImage)

  if (statusCode == 0) {
    docker.withRegistry(registry) {
      docker.image(helmImage).inside {
        if (config.repositoryType == 'nexus') {
          helmChart.nexusPublishChart(config.nexusCredential)
        }

        else if (config.repositoryType == 'acr') {
          helmChart.acrPublishChart(config.servicePrincipalCredential)
        }
      }
    }
  }

  else if (statusCode == 1) {
    buildDockerImage(nexusRegistry, helmImage, config.helmVersion).inside {
      if (config.repositoryType == 'nexus') {
        helmChart.nexusPublishChart(config.nexusCredential)
      }

      else if (config.repositoryType == 'acr') {
        helmChart.acrPublishChart(config.servicePrincipalCredential)
      }
    }

    registry = nexusRegistry - ~'^http.*://'

    sh("docker image rm $helmImage ${registry}/$helmImage")
  }
}

Integer fetchDockerImage(String registry, String image) {
  registry = registry - ~'^http.*://'

  return sh(
    script: "docker image pull ${registry}/$image",
    returnStatus: true
  )
}

def buildDockerImage(String registry, String image, String helmVersion) {
  writeFile(
    file: 'Dockerfile.helm',
    text: libraryResource('image/Dockerfile')
  )

  String dockerBuildArgs = "--build-arg HELM_VERSION=$helmVersion -f ./Dockerfile.helm ."

  docker.withRegistry(registry) {
    def dockerImage = docker.build(image, dockerBuildArgs)
    dockerImage.push()

    return dockerImage
  }
}