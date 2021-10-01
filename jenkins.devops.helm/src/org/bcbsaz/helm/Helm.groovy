package org.bcbsaz.helm 



public class Helm {
    private Map _config
    Helm(Map config){
        _config = config
    }
    public Map getConfig() {
        return this._config
    }

    public Map execute(HelmMethod method) {
        method.execute(getConfig())
    }
}