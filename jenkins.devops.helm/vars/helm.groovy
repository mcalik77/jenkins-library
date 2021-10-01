import org.bcbsaz.helm.HelmMethod
import org.bcbsaz.helm.Helm
import org.bcbsaz.helm.HelmMethodFactory

import org.bcbsaz.ioc.ContextRegistry

/**
 * Example custom step for easy use of MsBuild inside Jenkinsfiles
 * @param solutionPath Path to .sln file
 * @return
 */

@groovy.transform.Field
def outputs = [:]

def call(Map params) {
  ContextRegistry.registerDefaultContext(this)
  String sshGitCred = 'azdoGitCred'  

  Map config = [helmVersion: '3.5.3']

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  Helm helm = new Helm(config)

  withCredentials([
    azureServicePrincipal(
      credentialsId:          config.credential,
      subscriptionIdVariable: 'ARM_SUBSCRIPTION_ID',
      clientIdVariable:       'ARM_CLIENT_ID',
      clientSecretVariable:   'ARM_CLIENT_SECRET',
      tenantIdVariable:       'ARM_TENANT_ID'
    ),

    sshUserPrivateKey(credentialsId: sshGitCred, keyFileVariable: 'SSH_KEY', usernameVariable: 'GIT_USERNAME')
  ]) {

    config.put('ARM_SUBSCRIPTION_ID', ARM_SUBSCRIPTION_ID)
    config.put('ARM_CLIENT_ID', ARM_CLIENT_ID)
    config.put('ARM_CLIENT_SECRET', ARM_CLIENT_SECRET)
    config.put('ARM_TENANT_ID', ARM_TENANT_ID)

    String helmImage = "devops/helm:$config.helmVersion"
    String registry = nexusRegistry.replace('7000', '5443')

    Integer statusCode = fetchDockerImage(registry, helmImage)

    if (statusCode == 0) {
      docker.withRegistry(registry) {
        docker.image(helmImage).inside {
          setupNexusRepo()
          outputs = helm.execute(HelmMethodFactory.getHelmMethod(config.command))
        }
      }
    }

    else if (statusCode == 1) {
      buildDockerImage(nexusRegistry, helmImage, config.helmVersion).inside {
        setupNexusRepo()
        outputs = helm.execute(HelmMethodFactory.getHelmMethod(config.command))
      }

      registry = nexusRegistry - ~'^http.*://'

      sh("docker image rm $helmImage ${registry}/$helmImage")
    }

    return outputs
  }
}

Void setupNexusRepo() {
  sh(
    """
      NEXUS_URL='http://nexus.azblue.com:8081/repository'

      curl \
        --remote-name \
        --location "\${NEXUS_URL}/raw/certificates/bcbsaz.pem"

      helm repo add nexus-bcbsaz \
        --ca-file bcbsaz.pem \
        "\${NEXUS_URL}/helm-hosted"

      helm repo update
    """
  )
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