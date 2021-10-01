import org.bcbsaz.TerraformBuilder
import org.bcbsaz.Terraform

void call(Map params) {

  Map config = [
    terraformVersion: '0.13.5',
    sshGitCredentials: 'azdoGitCred',
    rootPath: 'infrastructure/terraform',
    autoApprove: false,
    debug: 'OFF',
    targets: [],
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  if (! config.servicePrincipalCredential) {
    error('servicePrincipalCredential was not specified')
  }

  Terraform terraform = new TerraformBuilder(this)
    .setRootPath(config.rootPath)
    .setDebug(config.debug)
    .setEnvironment(config.environment)
    .setRemoteBackend(config.remoteBackend)
    .setCommand(config.command)
    .setVarValues(config.varValues)
    .setVarFiles(config.varFiles)
    .setArguments(config.arguments)
    .build()

  withCredentials([
    azureServicePrincipal(
      credentialsId: config.servicePrincipalCredential,
      subscriptionIdVariable: 'ARM_SUBSCRIPTION_ID',
      clientIdVariable: 'ARM_CLIENT_ID',
      clientSecretVariable: 'ARM_CLIENT_SECRET',
      tenantIdVariable: 'ARM_TENANT_ID'
    ),

    sshUserPrivateKey(credentialsId: config.sshGitCredentials, keyFileVariable: 'sshKey')
  ]) {

    String terraformImage = "devops/terraform:$config.terraformVersion"
    String registry = nexusRegistry.replace('7000', '5443')

    Integer statusCode = fetchDockerImage(registry, terraformImage)

    if (statusCode == 0) {
      docker.withRegistry(registry) {
        docker.image(terraformImage).inside {
          return terraform.executeCommand(config.autoApprove, config.targets)
        }
      }
    }

    else if (statusCode == 1) {
      buildDockerImage(nexusRegistry, terraformImage, config.terraformVersion).inside {
        return terraform.executeCommand(config.autoApprove, config.targets)
      }

      registry = nexusRegistry - ~'^http.*://'

      sh("docker image rm $terraformImage ${registry}/$terraformImage")
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

def buildDockerImage(String registry, String image, String terraformVersion) {
  writeFile(
    file: 'Dockerfile.terraform',
    text: libraryResource('org/bcbsaz/Dockerfile.terraform')
  )

  String dockerBuildArgs = "--build-arg TERRAFORM_VERSION=$terraformVersion -f ./Dockerfile.terraform ."

  docker.withRegistry(registry) {
    def dockerImage = docker.build(image, dockerBuildArgs)
    dockerImage.push()

    return dockerImage
  }
}
