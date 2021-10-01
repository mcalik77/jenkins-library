package org.bcbsaz

class Liquibase implements Serializable {
  def steps

  private String rootPath

  private String connectionString
  private String changelogFile

  private String command

  private Map globalArguments
  private Map arguments

  public Liquibase(
    steps,

    rootPath,

    connectionString,
    changelogFile,

    command,

    globalArguments,
    arguments
  ) {

    this.steps = steps

    this.rootPath = rootPath

    this.connectionString = connectionString
    this.changelogFile = changelogFile

    this.command = command

    this.globalArguments = globalArguments
    this.arguments = arguments
  }

  private String getGlobalArguments() {
    String options = ''

    if (globalArguments) {
      for (String key in globalArguments.keySet()) {
        options += "--${key} ${globalArguments[key]} "
      }
    }

    return options
  }

  private String getArguments() {
    String options = ''

    if (arguments) {
      for (String key in arguments.keySet()) {
        if (key == 'value') {
          options += "${arguments[key]} "
        }

        else {
          options += "--${key} ${arguments[key]} "
        }
      }
    }

    return options
  }

  public executeCommand() {
    String globalArgs = getGlobalArguments()
    String options = getArguments()

    steps.sh(
      """#!/usr/bin/env bash

      cd $rootPath

      liquibase \
        --url "$connectionString" \
        --changeLogFile $changelogFile \
        --username \$DB_USERNAME \
        --password \$DB_PASSWORD \
        $globalArgs $command $options
      """
    )
  }
}
