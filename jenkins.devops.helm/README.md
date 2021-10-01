# HELM Reusable Library for HELM


This git repository contains a library of reusable [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) steps that can be used in your `Jenkinsfile` to help improve your HELM Continuous Delivery pipeline.


## How to use this library

To use the functions in this library just add the following to the top of your `Jenkinsfile`:

```groovy
library identifier: 'helm@master', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.helm',
   credentialsId: 'azdoGitCred'])
```

That will use the master branch of this library. You can if you wish pick a specific [tag](https://dev.azure.com/AZBlue/OneAZBlue/_git/jenkins.devops.helm/tags) or [commit SHA](https://dev.azure.com/AZBlue/OneAZBlue/_git/jenkins.devops.helm/commits) of this repository too. Best practice is to version lock the above library.

## Example of upgrade command

You can include the library in your jenkinsfile by using the following code:

```groovy
library identifier: 'helm@1.0.0', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.helm',
   credentialsId: 'azdoGitCred'])

pipeline {

  agent any

  stages {
    stage('Deploy') {
      steps {
          
          helm (
              appVersion: params.IMAGE_TAG,
              environment: params.ENV,
              aks_rg: params.AKS_RG,
              aks_name: params.AKS_NAME,
              command: 'upgrade',
              chart_path: './edi-cache',
              install: true,
              wait: true,
              verify: false,
              release_name: 'api-edi-cache',
              namespace: "edi-${params.ENV}",
              value_files: ["values.yaml", "values-${params.ENV}.yaml"],
              set_values: [
                'image.repository': "${params.IMAGE_REPOSITORY}",
                'image.tag': "${params.IMAGE_TAG}"
              ],
              credential: params.AZ_SPN_CRED_ID)

      }
    }
  }
}
```

## Example of Deploying Helm Chart From Nexus

```
parameters {
  string(
    name: 'helmCommand',
    description: 'Helm command to execute, ex. upgrade, delete.'
  )

  booleanParam(
    name: 'install',
    defaultValue: false,
    description: 'Command argument used for installing a new chart.'
  )

  string(
    name: 'chartName',
    description: 'Name of the Helm chart to deploy.'
  )

  string(
    name: 'chartVersion',
    description: 'Version of the Helm chart to deploy.'
  )

  string(
    name: 'releaseName',
    description: 'Name of the release to use for the chart deployment.'
  )

  string(
    name: 'namespace',
    defaultValue: 'massimo-dev',
    description: 'Namespace of the cluster to deploy the Helm chart resources.'
  )

  string(
    name: 'aksResourceGroup',
    defaultValue: 'rgAKSD005',
    description: 'Resoure group of the AKS cluster.'
  )

  string(
    name: 'aksName',
    defaultValue: 'appAKSD005',
    description: 'Name of the AKS cluster.'
  )

  string(
    name: 'servicePrincipal',
    defaultValue: 'spJenkinsVelocityConnectDevTest',
    description: 'Service principal used to authenticate with Azure.'
  )
}

stage('Deploy Helm Chart') {
  steps {
    helm (
      command: params.helmCommand,
      install: params.install,
      debug: true,

      chartName: params.chartName,
      chartVersion: params.chartVersion,

      release_name: params.releaseName,
      namespace: params.namespace,

      aks_rg: params.aksResourceGroup,
      aks_name: params.aksName,
      credential: params.servicePrincipal
    )
  }
}
```

## Example of delete command

You can include the library in your jenkinsfile by using the following code:

```groovy
library identifier: 'helm@1.0.0', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.helm',
   credentialsId: 'azdoGitCred'])

pipeline {

  agent any

  stages {
    stage('Deploy') {
      steps {
          
          helm (
              appVersion: params.IMAGE_TAG,
              environment: params.ENV,
              aks_rg: params.AKS_RG,
              aks_name: params.AKS_NAME,
              command: 'delete',
              chart_path: './edi-cache',
              release_name: 'api-edi-cache',
              namespace: "edi-${params.ENV}",
              credential: params.AZ_SPN_CRED_ID)
      }
    }
  }
}
```


## Library Inputs
The following are the supported inputs for this library.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| appVersion | SemVer of the application being deployed with HELM Ex) '0.12.28' | `string` | n/a | yes |
| environment | Target environment for HELM deployment (dev, qa, uat, prod) | `string` | n/a | yes |
| command | HELM command to run (upgrade, delete) | `string` | n/a | yes |
| chart_path | Location of the HELM chart. Will be appended to /infrastructure/helm | `string` | n/a | yes |
| chartName | Name of the Helm chart to deploy from Nexus. **Note:** chart_path must be unset if using chartName. | `String` | `n/a` | yes |
| chartVersion | Version of the Helm chart to package and publish. | `String` | `n/a` | no |
| aks_rg | Azure Resource-Group for the AKS Cluster | `string` | n/a | yes |
| aks_name | Name of the AKS cluster | `string` | n/a | yes |
| install | If true, and a release by this name doesn't already exist, run an install| `boolean` | n/a | no |
| wait | If true, will wait until all resources are in a ready state before marking the release as successful.| `boolean` | n/a | no |
| verify | If true, verify the package before installing it | `boolean` | n/a | no |
| release_name | The name to use with the release | `string` | n/a | yes |
| namespace | The namespace scope for this request | `string` | n/a | yes |
| set_values | Helm values to set via command line arguments. | `map` | n/a | no |
| value_files | Name of the HELM values files to use. | `list` | n/a | no |
| credential | Name of the Jenkins Client Secret holding Azure SPN Credentials. | `string` | n/a | yes |

## Publish Helm Chart

The publish Helm chart library allows you to package and publish a Helm chart
to Nexus or ACR. By default it will assume that you want to publish to Nexus.
Another thing to note is that by default the version of the chart to package
and publish is fetched from Charts.yaml and can be overriden by specifing the
**chartVersion** param to the library.

It is recommened to avoid using chartVersion and instead update the version of
the chart in Chart.yaml in order to document the actual version of the chart.

### Publish Helm Chart Using Nexus

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
    name: 'chartName',
    description: 'Name of the Helm chart to publish.'
  )
}

publishHelmChart(chart: params.chartName)
```

### Publish Helm Chart Using ACR

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
    name: 'chartName',
    description: 'Name of the Helm chart to publish.'
  )

  string(
    name: 'azureRegistry',
    description: 'Name of the Azure Container Registry to publish the Helm chart'
  )
}

publishHelmChart(
  repositoryType: 'acr',
  chartName: params.chartName
  azureRegistry: params.azureRegistry
)
```

### Inputs

The following are the supported inputs for the utility.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| helmVersion | Version of Helm to use for publishing the chart. | `String` | `3.5.3` | no |
| repositoryType | Repository type to publish the Helm chart, nexus vs acr. | `String` | `nexus` | no |
| nexusCredential | Name of the Nexus credential to use to authenticate to the Nexus server. | `String` | `Nexus` | no |
| nexusRepository | Full URL of the Nexus Repository server to publish the Helm chart. | `String` | `http://nexus.azblue.com:8081/repository/helm-hosted/` | no |
| chartName | Name of the Helm chart to publish. | `String` | `n/a` | yes |
| chartVersion | Version of the Helm chart to package and publish. | `String` | `n/a` | no |
| azureRegistry | Name of the Azure Container Registry to publish the Helm chart.
| servicePrincipalCredential | Name of the Azure Service Principal credential to use. **Required** when repositoryType is set to acr. | `String` | `n/a` | yes |