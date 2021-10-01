package org.bcbsaz

class PostgresBuilder implements Serializable {
  def steps

  private Map arguments
  
  public PostgresBuilder(steps) {
    this.steps = steps
  }

 
  public PostgresBuilder setArguments(Map arguments) {
    this.arguments = arguments
    return this
  }

  public Postgres build() {
    return new Postgres(
      steps,
      arguments
    )
  }
}