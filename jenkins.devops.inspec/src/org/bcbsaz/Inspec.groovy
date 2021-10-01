package org.bcbsaz

class Inspec implements Serializable {
 def steps

  String platform = 'azure'

  public Inspec(steps, Map config = [:]) {
    this.steps = steps

    if (config.platform) {
      this.platform = config.platform
    } 
  }

public InspecCheck() {
  steps.sh(
    """#!/usr/bin/env bash

      cd infrastructure/terraform
      inspec check test/verify \
        --chef-license accept

    """
 )
}

public InspecExec(String platform) {
  steps.sh(
    """#!/usr/bin/env bash

      cd infrastructure/terraform
      inspec exec test/verify \
        --chef-license accept \
        -t ${platform}:// \
        --reporter cli \
        --no-distinct-exit 
        
    """
  )
 }
}