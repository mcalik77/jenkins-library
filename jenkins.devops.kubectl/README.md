# Jenkins Kubectl Library

Jenkins reusable library for Kubectl.

```
library identifier: 'kubectl@0.0.3', retriever: modernSCM(
  [
    $class: 'GitSCMSource',
    remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.kubectl',
    credentialsId: 'azdoGitCred'
  ]
)

pipeline {
  agent any

  stages {
    stage('Create namespace without file') {
      
      steps {
        kubectl([
          aks_rg: 'rgGarfieldDevopsD001',
          aks_name: 'aksGarfieldDevopsD001',
          command: 'create namespace',
          arguments: [value: sample]
          credential: 
        ])
      }
    }
    stage('Create pod using file') {
      
      steps {
        kubectl([
          aks_rg: 'rgGarfieldDevopsD001',
          aks_name: 'aksGarfieldDevopsD001',
          command: 'create',
          arguments: [filename: 'pod.yaml', namespace: 'sample'],
          credential: 
      
        ])
      }
    }
    
     stage('Delete pod using file') {
      
      steps {
        kubectl([
          aks_rg: 'rgGarfieldDevopsD001',
          aks_name: 'aksGarfieldDevopsD001',
          command: 'delete',
          arguments: [filename: 'pod.yaml', namespace: 'sample'],
      
        ])
      }
    }
  }
}

```

## Inputs

The following are the supported inputs for the library.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| kubectlVersion | Version of kubectl image to use. | `String` | `1.22.0` | no |
| aks_rg | Resoure group of the AKS cluster. | `String` | n/a | yes |
| aks_name | Name of the AKS cluster. | `String` | `n/a` | yes |
| credential | Id of Jenkins credential of Service principal used to authenticate with Azure | `String` | `n/a` | yes |
| command | Kubectl command to execute. | `String` | `n/a` | yes |
| arguments | Map of Kubectl arguments to use. These are arguments to use after the command. | `Map` | `n/a` | no |
