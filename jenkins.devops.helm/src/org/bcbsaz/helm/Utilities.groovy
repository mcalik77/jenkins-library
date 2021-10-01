#!/usr/bin/groovy
package org.bcbsaz.helm

    String getVars(Object vars) {
        String varValues = ''

        String specialCharRegex = /[\W_&&[^\s]]/;
        
        vars.each {varValues = varValues + ' --set ' + it.key + '=' + "'" + it.value.toString().replaceAll(specialCharRegex, /\\$0/) + "'"}

        return varValues
    }
    
    String getVarFiles(String path, Object vars) {
        String varFiles = '' 
        vars.each {varFiles = varFiles + ' --values=' + path + '/' + it}
        return varFiles
    }
    
    String getArgs(String consumerArgs, List defArgs) {
        
        defArgs.each { (consumerArgs.indexOf(it) == -1) ? (consumerArgs = consumerArgs + ' ' + it) : ''}
        return consumerArgs
    }