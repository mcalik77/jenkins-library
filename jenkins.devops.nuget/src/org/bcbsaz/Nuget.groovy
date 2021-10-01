package org.bcbsaz

class Nuget implements Serializable {
  def steps

  String packageFeed = 'azblue_nuget'

  public Nuget(steps, Map config = [:]) {
    this.steps = steps

    if (config.packageFeed) {
      this.packageFeed = config.packageFeed
    } 
  }

private nugetConfig() {
    steps.writeFile(
      file : 'nuget.config',
      text : this.steps.libraryResource('org/bcbsaz/nuget.config')
    )
  }
  
private String getApiKey() {
  return steps.sh(
    returnStdout: true,
    script: """
      set +x
      PAT_TOKEN=`
        echo \$VSS_NUGET_EXTERNAL_FEED_ENDPOINTS \
          | awk -F':' '{print \$5}'
      `
      echo \${PAT_TOKEN::-3}
    """
  ).trim()
}

  public nugetPackage(String project, String packageVersion, String packageConfig, String packageOutput) {
    
    steps.sh(
      """#!/usr/bin/env bash
          shopt -s globstar
          dotnet pack **/${project}.csproj \
            --configuration $packageConfig \
            --output $packageOutput \
            --no-build \
            -p:PackageVersion="$packageVersion"
"""
    )
    
    push(project, packageVersion, packageOutput)
  }

  private push(String project, String packageVersion, String packageOutput) {
    nugetConfig()
    
    String apiKey = getApiKey()
    steps.sh(
      """
        set +x
        dotnet nuget push \
          --skip-duplicate \
          --source $packageFeed \
          --api-key $apiKey \
          "${packageOutput}/${project}.${packageVersion}.nupkg" 
      """
    )
  }


}
