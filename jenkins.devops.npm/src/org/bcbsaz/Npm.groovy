package org.bcbsaz

class Npm implements Serializable {
  def steps

 
  public Npm(steps) {

    this.steps         = steps
  }


  public run(String command) {
    if (! command) {
      steps.error("command was not specified in script")
    }
    steps.sh(
      """#!/usr/bin/env bash
        npm install
        npm install sonar-scanner
        npm run $command
      """
    )
  }

}