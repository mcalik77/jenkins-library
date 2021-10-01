import org.bcbsaz.Nuget

def call(Map config = [:]) {

  String dockerImageVersion = '3.1'

  String packageConfig = 'Release'
  String packageOutput = 'pkg'


  if (config.dockerImageVersion) {
    dockerImageVersion = config.dockerImageVersion
  }

  if (config.packageConfig) {
    packageConfig = config.packageConfig
  }

  if (config.packageOutput) {
    packageOutput = config.packageOutput
  }

  validateParams(config)
  nuget = new Nuget(this, config)

  docker.withRegistry('http://lp-nex-a01.corp.net.bcbsaz.com:5443') {

    docker.image("devops/dotnet-core:$dockerImageVersion").inside {

      withCredentials([
        string(credentialsId: 'azdoNugetFeed', variable: 'VSS_NUGET_EXTERNAL_FEED_ENDPOINTS')
      ]) {

        nuget.nugetPackage(config.project, config.packageVersion, packageConfig, packageOutput)
      }
    }
  }
}

def validateParams(Map config = [:]) {

  String errorMessage = """
    dockerImageVersion [String] (Optional) version of the docker image to use.
    project            [String] (Required) name of the .NET project to package.
    packageVersion     [String] (Required) version of the .NET package to create.
    packageConfig      [String] (Optional) package configuration to use, Debug vs Release.
    packageOutput      [String] (Optional) path to publish the package.

    Default Values:
      dockerImageVersion = 3.1
      packageConfig      = Release
      packageOutput      = pkg
  """

  if (! config.project) {
    error "\n    project is a required parameter \n$errorMessage"
  }

    if (! config.packageVersion) {
    error "\n    packageVersion is a required parameter \n$errorMessage"
  }
}