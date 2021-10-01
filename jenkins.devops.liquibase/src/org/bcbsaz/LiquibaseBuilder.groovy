package org.bcbsaz

class LiquibaseBuilder implements Serializable {
  def steps

  private String rootPath

  private String connectionString
  private String changelogFile

  private String command

  private Map globalArguments
  private Map arguments

  public LiquibaseBuilder(steps) {
    this.steps = steps
  }

  public LiquibaseBuilder setRootPath(String rootPath) {
    this.rootPath = rootPath
    return this
  }

  public LiquibaseBuilder setConnectionString(String connectionString) {
    if (! connectionString) {
      steps.error('connectionString was not specified')
    }

    this.connectionString = connectionString
    return this
  }

  public LiquibaseBuilder setChangelogFile(String changelogFile) {
    this.changelogFile = changelogFile
    return this
  }

  public LiquibaseBuilder setCommand(String command) {
    if (! command) {
      steps.error('command was not specified')
    }

    this.command = command
    return this
  }

  public LiquibaseBuilder setGlobalArguments(Map globalArguments) {
    this.globalArguments = globalArguments
    return this
  }

  public LiquibaseBuilder setArguments(Map arguments) {
    this.arguments = arguments
    return this
  }

  public Liquibase build() {
    return new Liquibase(
      steps,

      rootPath,

      connectionString,
      changelogFile,

      command,

      globalArguments,
      arguments
    )
  }
}
