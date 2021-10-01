package org.bcbsaz

class FlywayBuilder implements Serializable {
  def steps

  private String command

  public FlywayBuilder(steps) {
    this.steps = steps
  }

 
  public FlywayBuilder setCommand(String command) {
    if (! command) {
      steps.error('command was not specified')
    }

    this.command = command
    return this
  }

 
  
  public Flyway build() {
    return new Flyway(
     steps,
     
     command
    
    )
  }
}
