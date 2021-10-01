# Package and Push Nuget 

The package and push Nuget utility is used to package .Net project and push it to artifact.

```
 parameters {
    string(
      name: 'gitRepoName',
      description: 'Name of the Git repository to use, ex. api.edi-cache'
    )
    string(
      name: 'gitCommitHash',
      description: 'Git Commit hash to checkout.'
    )
    string(
      name: 'project',
      description: 'Path of the .NET project to package.'
    )
    string(
      name: 'packageVersion',
      description: 'Version of NuGet package to create.'
    )
  }

   publishDotnet(
          project     : params.project,
          buildConfig : params.buildConfig,
          buildOutput : params.buildOutput
        )
```

## Inputs

The following are the supported inputs for the utility.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| dockerImageVersion | Version of the Docker image to use for publishing .NET app. | `String` | `3.1` | no |
| project | Name of the .NET project to build. | `String` | n/a | yes |
| buildConfig | Build configuration to use, Debug vs Release. | `String` | `Release` | no |
| buildOutput | Path to publish the artifacts from the build. | `String` | `build` | no |

# Test .NET Utility

The test .NET utility is used to run unit tests and generate a coverage report.
By default the utility assumes that you want to test a release build. You can
override the build config by passing in the buildConfig parameter.

```
  parameters {
    string(
      name: 'project',
      description: 'Path of the .NET project to publish'
    )

    string(
      name: 'buildConfig',
      defaultValue: 'Release',
      description: 'Build configuration to use, ex. Debug vs Release'
    )

     string(
      name: 'testOutput',
      defaultValue: 'test',
      description: 'Path to publish the artifacts from the test.'
    )
  }

  testDotnet(
    project     : params.project,
    buildConfig : params.buildConfig,
    testOutput  : params.testOutput
  )
```

## Inputs

The following are the supported inputs for the utility.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| dockerImageVersion | Version of the Docker image to use for testing .NET app. | `String` | `3.1` | no |
| project | Name or regex of the .NET project to test. | `String` | `.Tests.csproj` | no |
| buildConfig | Build configuration to use, Debug vs Release. | `String` | `Release` | no |
| testOutput | Path to publish the reports from the test. | `String` | `build` | no |
