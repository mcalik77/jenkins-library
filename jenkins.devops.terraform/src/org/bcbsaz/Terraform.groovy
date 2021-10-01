package org.bcbsaz

class Terraform implements Serializable {
  def steps

  private String rootPath
  private String debug

  private String environment

  private Map remoteBackend
  private String command

  private List varFiles
  private Map varValues

  private List arguments

  public Terraform(
    steps,

    String rootPath,
    String debug,

    String environment,

    Map remoteBackend,
    String command,

    List varFiles,
    Map varValues,

    List arguments 
  ) {

    this.steps = steps

    this.rootPath = rootPath
    this.debug = debug

    this.environment = environment

    this.remoteBackend = remoteBackend
    this.command = command

    this.varFiles = varFiles
    this.varValues = varValues

    this.arguments = arguments
  }

  private azureLogin() {
    steps.sh(
      """#!/usr/bin/env bash
        az login --service-principal \
          --username "\$ARM_CLIENT_ID" \
          --password "\$ARM_CLIENT_SECRET" \
          --tenant "\$ARM_TENANT_ID"

        az account set --subscription "\$ARM_SUBSCRIPTION_ID"
      """
    )
  }

  private init() {
    String terraformArgs = ''

    for (String key in remoteBackend.keySet()) {
      terraformArgs += "-backend-config ${key}=${remoteBackend[key]} "
    }

    azureLogin()

    steps.sh(
      """#!/usr/bin/env bash

        mkdir --parents ~/.ssh
        chmod 700 ~/.ssh

        ssh-keyscan ssh.dev.azure.com > ~/.ssh/known_hosts
        export GIT_SSH_COMMAND="ssh -i \$sshKey"

        [[ $debug != OFF ]] && export TF_LOG=$debug

        cd $rootPath

        terraform init $terraformArgs

        terraform workspace new $environment || true
        terraform workspace select $environment
      """
    )
  }

  private String getTfVarFiles() {
    String tfVarFiles = ''

    if (this.varFiles) {
      for (String file in this.varFiles) {
        tfVarFiles += "-var-file environments/$file "
      }
    }

    return tfVarFiles
  }

  private String getTfVarValues() {
    String tfValues = ''
    String value = ''

    if (this.varValues) {
      for (String key in this.varValues.keySet()) {

        // In order to pass in special characters to Terraform,
        // Terraform requires single quote to be escaped in a certain way.
        //
        // Example:
        //
        //  Terraform expects \\\'
        //  Jenkins \\\\\\' = \\\'
        //
        //  Where \\ = \
        //
        // ANSI-C Quoting is used to escape speical characters using $'@#$' = @#$
        // We will need to espace backslash-escaped characters \\n = \n
        //
        // Where \\\\ = \\
        //
        value = varValues[key].replace("\\", "\\\\")
        value = value.replace("'", "\\\\\\'")

        tfValues += "-var \$'${key}=$value' "
      }
    }

    return tfValues
  }

  private String getTfArguments(List targets) {
    String options = ''

    if (targets) {
      for (String target in targets) {
        options += "--target $target "
      }
    }

    if (this.arguments) {
      for (String arg in this.arguments) {
        options += " $arg"
      }
    }

    return options
  }

  private Map getTerraformOutput() {
    Map outputs = [:]

    String tfOutput = ''

    if (this.command == 'apply') {
      tfOutput = steps.sh(
        returnStdout: true,

        script: """#!/usr/bin/env bash
          set -x

          cd $rootPath
          terraform output -json -no-color
        """
      )

      outputs = steps.readJSON(text: tfOutput)
    }

    return outputs
  }

  public Map executeCommand(Boolean autoApprove = false, List targets) {
    init()

    String options = ''

    if (this.command != 'plan' && autoApprove) {
      options = "-auto-approve"
    }

    String tfVarFiles = getTfVarFiles()
    String tfValues = getTfVarValues()

    options += getTfArguments(targets)

    steps.sh(
      """#!/usr/bin/env bash

        cd $rootPath
        terraform $command $tfVarFiles $tfValues $options
      """
    )

    return getTerraformOutput()
  }
}
