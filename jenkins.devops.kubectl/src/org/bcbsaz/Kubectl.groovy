package org.bcbsaz

class Kubectl implements Serializable {
  def steps

  private String command
  private Map arguments

  public Kubectl(
    steps,
    command,
    arguments
  ) {

    this.steps = steps
    this.command = command
    this.arguments = arguments
  }

  private String getArguments() {
    String options = ''

    if (arguments) {
      for (String key in arguments.keySet()) {
        if (key == 'value') {
          options += "${arguments[key]}"
        }

        else {
          options += --${key} "${arguments[key]}"
        }
      }
    }

    return options
  }
  
  public executeCommand() {

    String options = getArguments()

    steps.sh(
      """#!/usr/bin/env bash
      kubectl $command $options
      """
    )
  }
}
