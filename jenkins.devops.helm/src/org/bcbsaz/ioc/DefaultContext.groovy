package org.bcbsaz.ioc


import org.bcbsaz.IStepExecutor
import org.bcbsaz.StepExecutor

class DefaultContext implements IContext, Serializable {
    private _steps

    DefaultContext(steps) {
        this._steps = steps
    }

    @Override
    IStepExecutor getStepExecutor() {
        return new StepExecutor(this._steps)
    }
}