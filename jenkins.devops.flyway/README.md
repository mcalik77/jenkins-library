# Jenkins Flyway Library

Jenkins reusable library for Flyway.

```
library identifier: 'npm@0.0.3', retriever: modernSCM(
  [
    $class: 'GitSCMSource',
    remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.npm',
    credentialsId: 'azdoGitCred'
  ]
)


pipeline {
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
      name: 'command',
      description: 'which npm command you want to run'
    )
    
  }
  agent any
  options {
    skipDefaultCheckout true
    ansiColor('xterm')
    timestamps()
  }
  stages {
    stage('Checkout Code') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: params.gitCommitHash ]],
          userRemoteConfigs: [[
            credentialsId: 'azdoGitCred',
            url: "git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/$params.gitRepoName"
          ]]
        ])
      }
    }
  
    stage('Npm run') {
      steps {
        npmPublish(
          command : 'build'
        )
      }
    }
  }
  
  post {
    always {
      cleanWs()
    }
  }
}
```

## Inputs

The following are the supported inputs for the library.


| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| command | Npm run command to execute. | `String` | `n/a` | yes |

