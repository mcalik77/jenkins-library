package org.bcbsaz

class Flyway implements Serializable {
  def steps

  private String command

  public Flyway(
    steps,

    command

  ) {

    this.steps = steps
    this.command = command
    
  }

  public executeCommand(String command, String url, String locations, String schemas) {
    
    steps.sh(
      """#!/usr/bin/env bash
    
      flyway -licenseKey=\$licenseKey \
        -user=\$DB_USERNAME \
        -password=\$DB_PASSWORD \
        -url="$url" \
        -locations="$locations" \
        -schemas="$schemas" \
        $command 
      """
    )
  }
}

