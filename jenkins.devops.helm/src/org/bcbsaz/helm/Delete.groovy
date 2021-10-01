package org.bcbsaz.helm

import org.bcbsaz.IStepExecutor
import org.bcbsaz.ioc.ContextRegistry
import org.bcbsaz.helm.Utilities
// import org.jenkinsci.plugins.pipeline.utility.steps.json

/**
 * Example class (without proper implementation) for using the MsBuild tool for building .NET projects.
 */
public class Delete implements HelmMethod, Serializable {

    def utility = new Utilities()    

     @Override
    public Map execute(Map config) {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()
        
        List default_args = ['']

        String args = (config.arguments == null) ? utility.getArgs('', default_args) : utility.getArgs(config.arguments, default_args)
    
        steps.sh(false, false, "set -x")
        steps.println """
        *******************************************
        ** Begin helm delete
        **   Command:           ${config.command}
        **    --keep-history:   ${config.keep_history}
        **   Arguments:         ${config.args}
        **   Release Name:      ${config.release_name}
        **   Chart Path:        ${config.chart_path}
        *******************************************
        """

        Integer returnStatus = steps.sh(true, true, """
            

            az login --service-principal -u '${config.ARM_CLIENT_ID}' -p '${config.ARM_CLIENT_SECRET}' -t '${config.ARM_TENANT_ID}'
            az account set --subscription '${config.ARM_SUBSCRIPTION_ID}'
            az aks get-credentials --resource-group ${config.aks_rg} --name ${config.aks_name}

            ${config.debug ? 'set -x': 'set +x' } 

            helm ${config.command}  \
                ${config.keep_history ? '--keep-history' : ''}  \
                --namespace ${config.namespace}  \
                ${args} \
                ${config.release_name} 
            """)
                
        steps.sh(true, false, "set -x")

        steps.println """
        *******************************************
        ** End helm delete
        *******************************************
        """
        // String result = returnStatus
        //  Map result = steps.readJSON(text: returnStatus.substring(returnStatus.indexOf('[') + 1, returnStatus.indexOf(']') - 1))
        // Map result = steps.readJSON(text: '{"name":"integration-api-edicache-command-op","namespace":"integration-dev-jesse","revision":"17","updated":"2020-06-29 13:46:18.962459 -0700 MST","status":"deployed","chart":"integration-api-edicache-command-op-jesse-1.0.0","app_version":""}')
        
        // Map result = [:]
        
        if (returnStatus != 0) {
            steps.error("Failed to execute Helm Delete. Check error information above.")
            return ["STATUS":"failed"]
            // steps.outputs(returnStatus)
        }else{
            return ["STATUS":"success"]
        }


    }
}