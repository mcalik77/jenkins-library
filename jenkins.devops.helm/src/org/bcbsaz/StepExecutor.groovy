package org.bcbsaz

class StepExecutor implements IStepExecutor {
    private _steps

    StepExecutor(steps) {
        this._steps = steps
    }

    @Override
    def sh(boolean returnStdout, boolean returnStatus, String command) {
        this._steps.sh(returnStdout: returnStdout, returnStatus: returnStatus, script: "${command}")
    }

    @Override
    def println(String message) {
        this._steps.println(message)
    }

    @Override
    void error(String message) {
        this._steps.error(message)
    }

    @Override
    Map readJSON(Map text) {
        this._steps.readJSON(text)
    }

    // String outputs(String returnStatus) {
    //     this._steps.outputs(returnStatus)
    // }
}