package org.bcbsaz.helm

import org.bcbsaz.helm.Upgrade
import org.bcbsaz.helm.Delete
import org.bcbsaz.helm.Error

public class HelmMethodFactory {

    public static HelmMethod getHelmMethod(String type) {
        
        switch (type) {
            case "upgrade":
                return createUpgrade()
            case "delete":
                return createDelete()
        }

        return createStepError()

    }

    public static Upgrade createUpgrade(){
        return new Upgrade();
    }

    public static Delete createDelete(){
        return new Delete();
    }


    public static Error createStepError(){
        return new Error();
    }
}