# Jenkins Postgres Library

Jenkins reusable library for Postgres.

```
library identifier: 'postgres@0.0.6', retriever: modernSCM(
  [
    $class: 'GitSCMSource',
    remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.postgresql',
    credentialsId: 'azdoGitCred'
  ]
)


pipeline {

  agent any
  options {
    skipDefaultCheckout true
    ansiColor('xterm')
    timestamps()
  }
  stages {
    
     stage('Postgres command') {
      steps {
        postgres(
          host: params.host,
          dbname: params.dbname,
          spCredential: params.spCredential,
          activeDirectoryAdmin: params.activeDirectoryAdmin,
          arguments: [command: "CREATE USER mustafa9 WITH PASSWORD 'password';"]
          
        )
      }
    }

    stage('Postgres command') {
      steps {
        postgres(
          host: 'psqlcalikcommond006.postgres.database.azure.com',
          dbname: 'postgress',
          psqlCredentials: 'spJenkinsVelocityDevTest',
          activeDirectoryAdminGroup: 'aclpgadmin@psqlcalikcommond006'
          arguments: [command: 'grant all privileges on database "bcbsaz-postgress" to mustafa9;']
          
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
| host | Postgress host name. | `String` | `n/a` | yes |
| dbname | Postgres database name | `String` | `n/a` | yes |
| psqlCredentials | Name of the service principal Jenkins credential used to authenticate with the postgres server. | `String` | `n/a` | yes |
| activeDirectoryAdmin | Active Directory group admin | `String` | `n/a` | yes |
| psqlCommand | postgress command you want to run | `String` | `n/a` | yes |
