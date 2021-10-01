# Jenkins Liquibase Library

Jenkins reusable library for Liquibase.

```
library identifier: 'liquibase@1.0.0', retriever: modernSCM(
  [
    $class: 'GitSCMSource',
    remote: 'git@ssh.dev.azure.com:v3/AZBlue/OneAZBlue/jenkins.devops.liquibase',
    credentialsId: 'azdoGitCred'
  ]
)

pipeline {
  agent any

  stages {
    stage('Liquibase Execute') {
      when {
        expression { params.rollbackChanges == false }
      }

      steps {
        liquibase([
          databaseCredential: params.databaseCredential,
          connectionString: params.connectionString,

          changelogFile: params.changelogFile,
          command: params.command,

          globalArguments: [schemas: 'Hub']
        ])
      }
    }

    stage('Liquibase Rollback') {
      when {
        expression { params.rollbackChanges == true }
      }

      steps {
        liquibase([
          databaseCredential: params.databaseCredential,
          connectionString: params.connectionString,

          command: 'rollbackCount',

          globalArguments: [schemas: 'Hub'],
          arguments: [value: params.rollbackCount]
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
| liquibaseVersion | Version of Liquibase to use. | `String` | `4.3.5` | no |
| rootPath | Path where the changelogs are located. | `String` | `changelogs` | no |
| databaseCredential | Name of the Jenkins credential used to authenticate with the database. | `String` | `n/a` | yes |
| connectionString | Connection string for the database. | `String` | `n/a` | yes |
| changelogFile | Changelog file to use for the database changes. | `String` | `changelog.xml` | no |
| command | Liquibase command to execute. | `String` | `n/a` | yes |
| globalArguments | Map of Liquibase global arguments to use. These are arguments used before the command. | `Map` | `n/a` | no |
| arguments | Map of Liquibase arguments to use. These are arguments to use after the command. | `Map` | `n/a` | no |
