package org.bcbsaz

class Postgres implements Serializable {
  def steps

  private Map arguments

  public Postgres(
    steps,
    arguments

  ) {
   
    this.steps = steps
    this.arguments = arguments

  }

  private String getArguments() {
    String options = ''
    String newArgument = ''

    if (arguments) {
      for (String key in arguments.keySet()) {
        newArgument = arguments[key].replace('"', '\\"')
        if (key == 'value') {
          options += "${newArgument} "
        }

        else {
         options += "--${key} \"${newArgument}\""
        }
      }
    }

    return options
  }

  private azureLogin() {
    steps.sh(
      """#!/usr/bin/env bash
        az login --service-principal \
          --username "\$ARM_CLIENT_ID" \
          --password "\$ARM_CLIENT_SECRET" \
          --tenant "\$ARM_TENANT_ID"

        az account set --subscription "\$ARM_SUBSCRIPTION_ID"
      """
    )
  }

  public executeCommand(String host, String port, String dbname, String activeDirectoryAdmin, String sslmode) {
   
    azureLogin()
    
    String options = getArguments()
    // String psqlCommandnew = psqlCommand.replace("'", "\'")
    // String optionslast = options.replace('"', '\\"')
    // String psqlCommanddollar = psqlCommandlast.replace('$', '\\\$')
    
    steps.sh(
      """ #!/usr/bin/env bash 
      az login --service-principal \
          --username "\$ARM_CLIENT_ID" \
          --password "\$ARM_CLIENT_SECRET" \
          --tenant "\$ARM_TENANT_ID"

        az account set --subscription "\$ARM_SUBSCRIPTION_ID"
        echo \$options
        export PGPASSWORD=\$(az account get-access-token --resource-type oss-rdbms | jq -r '.accessToken')
        psql \"host=$host port=$port dbname=$dbname user=$activeDirectoryAdmin  sslmode=$sslmode \" \
        $options
        """
    )
  }
}