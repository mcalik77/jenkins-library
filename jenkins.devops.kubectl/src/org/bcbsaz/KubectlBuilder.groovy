package org.bcbsaz

class KubectlBuilder implements Serializable {
  def steps

  private String command
  private Map arguments

  public KubectlBuilder(steps) {
    this.steps = steps
  }

 
  public KubectlBuilder setCommand(String command) {
    if (! command) {
      steps.error('command was not specified')
    }

    this.command = command
    return this
  }


  public KubectlBuilder setArguments(Map arguments) {
    this.arguments = arguments
    return this
  }

  public Kubectl build() {
    return new Kubectl(
      steps,

      command,
      arguments
    )
  }
}
