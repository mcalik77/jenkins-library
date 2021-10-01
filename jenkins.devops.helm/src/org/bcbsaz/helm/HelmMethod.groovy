package org.bcbsaz.helm

import org.bcbsaz.IStepExecutor
import org.bcbsaz.ioc.ContextRegistry
import org.bcbsaz.helm.Utilities

public interface HelmMethod {

    public Map execute(Map config)
}