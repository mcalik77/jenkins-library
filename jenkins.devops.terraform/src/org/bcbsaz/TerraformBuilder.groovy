package org.bcbsaz

class TerraformBuilder implements Serializable {
  def steps

  private String rootPath
  private String debug

  private String environment

  private Map remoteBackend
  private String command

  private List varFiles
  private Map varValues

  private List arguments

  public TerraformBuilder(steps) {
    this.steps = steps
  }

  public TerraformBuilder setRootPath(String rootPath) {
    this.rootPath = rootPath
    return this
  }

  public TerraformBuilder setDebug(String debug) {
    if (! (debug in ['OFF', 'TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'])) {
      steps.error(
        """
          invalid debug value specified: $debug
            valid values are: TRACE, DEBUG, INFO, WARN, ERROR
        """
      )
    }

    this.debug = debug
    return this
  }

  public TerraformBuilder setEnvironment(String environment) {
    if (! environment) {
      steps.error('environment was not specified')
    }

    this.environment = environment
    return this
  }

  public TerraformBuilder setRemoteBackend(Map remoteBackend) {
    List expected_keys = [
      'resource_group_name',
      'storage_account_name',
      'container_name',
      'key'
    ]

    for (String key in expected_keys) {
      if (! (key in remoteBackend.keySet())) {
        steps.error("$key was not specified for the remoteBackend map")
      }
    }

    Integer nullValues = Collections.frequency(remoteBackend.values(), null)

    if (nullValues > 0) {
      steps.error(
        """
          one or more required value is missing for the remoteBackend map:

            remoteBackend = [
              resource_group_name:  'rgEnvCommonD001',
              storage_account_name: 'saterraformd001',
              container_name:       'terraformstate',
              key:                  'EdiCache'
            ]
        """
      )
    }

    this.remoteBackend = remoteBackend
    return this
  }

  public TerraformBuilder setCommand(String command) {
    if (! command) {
      steps.error('command was not specified')
    }

    this.command = command
    return this
  }

  public TerraformBuilder setVarFiles(List varFiles) {
    this.varFiles = varFiles
    return this
  }

  public TerraformBuilder setVarValues(Map varValues) {
    this.varValues = varValues
    return this
  }

  public TerraformBuilder setArguments(List arguments) {
    this.arguments = arguments
    return this
  }

  public Terraform build() {
    return new Terraform(
      steps,

      rootPath,
      debug,

      environment,

      remoteBackend,
      command,

      varFiles,
      varValues,

      arguments
    )
  }
}
