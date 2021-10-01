package org.bcbsaz.helm

import org.bcbsaz.IStepExecutor
import org.bcbsaz.ioc.ContextRegistry
import org.bcbsaz.helm.Utilities

public class Error implements HelmMethod, Serializable {


    def utility = new Utilities()

     @Override
    public Map execute(Map config){
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        steps.error("Command ${config.command} not supported, must be one of ['upgrade']")
        return [:]

    }
}