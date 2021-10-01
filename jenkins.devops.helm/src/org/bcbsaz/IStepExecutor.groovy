package org.bcbsaz

/**
 * Interface for calling any necessary Jenkins steps. This will be mocked in unit tests.
 */
interface IStepExecutor {
    def sh(boolean returnStdout, boolean returnStatus , String command)
    void error(String message)
    Map readJSON(Map text)
    def println(String message)
    // String outputs(String returnStatus)
}