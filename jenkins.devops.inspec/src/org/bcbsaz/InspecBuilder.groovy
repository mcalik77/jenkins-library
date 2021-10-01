package org.bcbsaz

class InspecBuilder implements Serializable {
  def steps

  String platform


  public InspecBuilder(steps) {
    this.steps = steps
  }

  public InspecBuilder setPlatform(String platform) {
    this.platform = platform
    return this
  }

public Inspec build() {
    Inspec inspec = new Inspec(steps,platform)
    return inspec
  }
}  
