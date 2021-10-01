# Terraform Reusable Library for Jenkins

Jenkins reusable library for Terraform.

```
library identifier: 'terraform@1.0.0', retriever: modernSCM(
  [
    $class: 'GitSCMSource',
    remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.terraform',
    credentialsId: 'azdoGitCred'
  ]
)

pipeline {
  agent any

  stages {
    stage('Deploy Terraform') {
      steps {
        script {
          Map outputs = terraform(
            terraformVersion: '0.13.5',
            environment: params.environment,

            command: params.terraformCommand,
            autoApprove: true,

            remoteBackend: [
              resource_group_name: params.resourceGroup,
              storage_account_name: params.storageAccount,
              container_name: params.container,
              key: params.statefile
            ],

            varFiles: ["${params.environment}.tfvars"],

            varValues: [
              ad_password: AD_PASSWORD,
              network_share_password: NETWORK_SHARE_PASSWORD
            ],

            servicePrincipalCredential: params.servicePrincipal
          )

          println(outputs.location.value)
        }
      }
    }
  }
}
```

## Inputs

The following are the supported inputs for this library.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| terraformVersion | Version of Terraform to use when deploying via Terraform. | `String` | `0.13.5` | no |
| sshGitCredentials | Name of the Git SSH credential to authenticate with Azure DevOps repository. | `String` | `azdoGitCred` | no |
| rootPath | Path of the Terraform files to deploy. | `String` | `infrastructure/terraform` | no |  
| autoApprove | Determines if apply or destroy should be executed without approval. | `Boolean` | `false` | no |
| debug | Debug flag to use when using terraform apply command (TRACE, DEBUG, INFO, WARN, ERROR). | `String` | `OFF` | no |
| targets | Resource(s) to target on deployment. | `List` | `[]` | no |
| environment | Target environment & workspace for terraform deployment (dev, qa, uat, prod). | `String` | `n/a` | yes |
| remoteBackend | Map that contains information for the remote backend. | `Map` | `n/a` | yes |
| command | Terraform command to execute (plan, apply, destroy). | `String` | `n/a` | yes |
| varValues | Map that contains Terraform values to use. | `Map` | `n/a` | no |
| varFiles | List of Terraform variable definitions (.tfvars) files to use. | `List` | `n/a` | no |
| arguments | List of additional Terraform arguments to use. | `List` | `n/a` | no |
| servicePrincipalCredential | Name of the Azure Service Principal credential to use. | `String` | `n/a` | yes |

## Library outputs

| Command | Description | Type |
|---------|-------------|:-------:|
| outputs | Map that contains all the outputs fetched from Terraform apply. | `Map` |
