package org.bcbsaz.ioc

import org.bcbsaz.IStepExecutor

interface IContext {
    IStepExecutor getStepExecutor()
}