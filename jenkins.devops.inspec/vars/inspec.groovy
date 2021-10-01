import org.bcbsaz.Inspec
import org.bcbsaz.InspecBuilder

void call(Map params) {

  Map config = [
    credential    : 'AzureSP',
    inspecVersion : '4.25.1',

  ]

  config += params

    withCredentials([
        azureServicePrincipal(
            credentialsId:          config.credential,
            subscriptionIdVariable: 'AZURE_SUBSCRIPTION_ID',
            clientIdVariable:       'AZURE_CLIENT_ID',
            clientSecretVariable:   'AZURE_CLIENT_SECRET',
            tenantIdVariable:       'AZURE_TENANT_ID'
        )
        ]) {

      def dockerImage = buildDockerImage(config.inspecVersion)

      dockerImage.inside("""--entrypoint=''""") {   

        Inspec inspec = new InspecBuilder(this)
          .setPlatform(config.platform)
          .build()

        inspec.InspecCheck()
        inspec.InspecExec(config.platform)
        }
    }
}               
def buildDockerImage(String inspecVersion) {
  writeFile(
    file: 'Dockerfile.inspec',
    text: libraryResource('org/bcbsaz/Dockerfile')
  )

  String dockerArgs = "--build-arg INSPEC_VER=$inspecVersion"

  return docker.build("inspec:$inspecVersion", "$dockerArgs -f ./Dockerfile.inspec .")
}