import org.bcbsaz.KubectlBuilder
import org.bcbsaz.Kubectl

void call(Map params) {
  Map config = [
    kubectlVersion: '1.22.0',
    aks_rg: '',
    credential: '',
    aks_name: ''
  ]

  config += params

  String nexusRegistry = 'http://lp-nex-a01.corp.net.bcbsaz.com:7000'

  Kubectl kubectl = new KubectlBuilder(this)
    .setCommand(config.command)
    .setGlobalArguments(config.globalArguments)
    .setArguments(config.arguments)
    .build()

 withCredentials([
    azureServicePrincipal(
      credentialsId:          config.credential,
      subscriptionIdVariable: 'ARM_SUBSCRIPTION_ID',
      clientIdVariable:       'ARM_CLIENT_ID',
      clientSecretVariable:   'ARM_CLIENT_SECRET',
      tenantIdVariable:       'ARM_TENANT_ID'
    )]) {

    config.put('ARM_SUBSCRIPTION_ID', ARM_SUBSCRIPTION_ID)
    config.put('ARM_CLIENT_ID', ARM_CLIENT_ID)
    config.put('ARM_CLIENT_SECRET', ARM_CLIENT_SECRET)
    config.put('ARM_TENANT_ID', ARM_TENANT_ID)
    
    String kubectlImage = "devops/kubectl:$config.kubectlVersion"
    String registry = nexusRegistry.replace('7000', '5443')

    Integer statusCode = fetchDockerImage(registry, kubectlImage)

    if (statusCode == 0) {
      docker.withRegistry(registry) {
        docker.image(kubectlImage).inside {
        sh(
               """#!/usr/bin/env bash
                 az login --service-principal -u '${config.ARM_CLIENT_ID}' -p '${config.ARM_CLIENT_SECRET}' -t '${config.ARM_TENANT_ID}'
                 az account set --subscription '${config.ARM_SUBSCRIPTION_ID}'
                 az aks get-credentials --resource-group '${config.aks_rg}' --name '${config.aks_name}' --admin
               """
             )
          kubectl.executeCommand()
        }
      }
    }

    else if (statusCode == 1) {
      buildDockerImage(nexusRegistry, kubectlImage, config.kubectlVersion).inside {
        sh(
               """#!/usr/bin/env bash
                 az login --service-principal -u '${config.ARM_CLIENT_ID}' -p '${config.ARM_CLIENT_SECRET}' -t '${config.ARM_TENANT_ID}'
                 az account set --subscription '${config.ARM_SUBSCRIPTION_ID}'
                 az aks get-credentials --resource-group '${config.aks_rg}' --name '${config.aks_name}' --admin
               """
             )
        kubectl.executeCommand()
      }

      registry = nexusRegistry - ~'^http.*://'

      sh("docker image rm $kubectlImage ${registry}/$kubectlImage")
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

def buildDockerImage(String registry, String image, String kubectlVersion) {
  writeFile(
    file: 'Dockerfile.kubectl',
    text: libraryResource('org/bcbsaz/Dockerfile.kubectl')
  )

  String dockerBuildArgs = "--build-arg KUBECTL_VERSION=$kubectlVersion -f ./Dockerfile.kubectl ."

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