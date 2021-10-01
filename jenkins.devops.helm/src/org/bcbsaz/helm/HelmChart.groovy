package org.bcbsaz.helm

class HelmChart implements Serializable {
  def steps

  private String repositoryType
  private String chartName
  private String chartVersion

  private String azureRegistry
  private String nexusRepository

  public HelmChart(
    steps,

    String repositoryType,
    String chartName,
    String chartVersion,

    String azureRegistry,
    String nexusRepository
  ) {

    this.steps = steps

    this.repositoryType = repositoryType
    this.chartName = chartName
    this.chartVersion = chartVersion

    this.azureRegistry = azureRegistry
    this.nexusRepository = nexusRepository
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

  private getAcrUsername() {
    return steps.sh(
      returnStdout: true,

      script: """#!/usr/bin/env bash

        az acr credential show \
          --name "$azureRegistry" \
          --query username \
          --output tsv
      """
    ).trim()
  }

  private getAcrPassword() {
    return steps.sh(
      returnStdout: true,

      script: """#!/usr/bin/env bash

        az acr credential show \
          --name "$azureRegistry" \
          --query passwords[0].value \
          --output tsv
      """

    ).trim()
  }

  private helmLogin() {
    String acrUsername = this.getAcrUsername()
    String acrPassword = this.getAcrPassword()

    steps.sh(
      """#!/usr/bin/env bash

        export HELM_EXPERIMENTAL_OCI=1

        echo "$acrPassword" | helm registry login "${azureRegistry}.azurecr.io" \
          --username "$acrUsername" \
          --password-stdin
      """
    )
  }

  private String getChartPackageName() {
    return steps.sh(
      returnStdout: true,

      script: """#!/usr/bin/env bash

        CHART_NAME=`
          grep \
            --word-regexp \
            --extended-regexp \
            '^name' "infrastructure/helm/${chartName}/Chart.yaml" | \
            awk -F':' '{print \$2}'
        `

        echo "\${CHART_NAME##*( )}"
      """

    ).trim()
  }

  private String getChartPackageVersion() {
    return steps.sh(
      returnStdout: true,

      script: """#!/usr/bin/env bash

        if [[ $chartVersion == 'null' ]]; then
          CHART_VERSION=`
            grep \
              --word-regexp \
              --extended-regexp \
              '^version' "infrastructure/helm/${chartName}/Chart.yaml" | \
              awk -F':' '{print \$2}'
          `

        else
          CHART_VERSION="$chartVersion"
        fi

        echo "\${CHART_VERSION##*( )}"
      """

    ).trim()
  }

  private packageChart(String chartVersion) {
    steps.sh(
      """#!/usr/bin/env bash

        HELM_ARGS="--version $chartVersion"

        sed --in-place "s/^version.*\$/version: $chartVersion/g" \
          "infrastructure/helm/$chartName/Chart.yaml"

        helm package "infrastructure/helm/$chartName" \$HELM_ARGS
      """
    )
  }

  public nexusPublishChart(String nexusCredential) {
    steps.withCredentials([
      steps.usernamePassword(
        credentialsId: nexusCredential,
        usernameVariable: 'NEXUS_USERNAME',
        passwordVariable: 'NEXUS_PASSWORD'
      )
    ]) {

      String chartPackageName = this.getChartPackageName()
      String chartPackageVersion = this.getChartPackageVersion()

      this.packageChart(chartPackageVersion)

      String chartPackage = "${chartPackageName}-${chartPackageVersion}.tgz"

      steps.sh(
        """#!/usr/bin/env bash

          curl $nexusRepository \
            --user "\${NEXUS_USERNAME}:\${NEXUS_PASSWORD}" \
            --upload-file "$chartPackage"
        """
      )
    }
  }

  public acrPublishChart(String servicePrincipalCredential) {
    if (! servicePrincipalCredential) {
      steps.error('servicePrincipalCredential was not specified')
    }

    steps.withCredentials([
      steps.azureServicePrincipal(
        credentialsId: servicePrincipalCredential,
        subscriptionIdVariable: 'ARM_SUBSCRIPTION_ID',
        clientIdVariable: 'ARM_CLIENT_ID',
        clientSecretVariable: 'ARM_CLIENT_SECRET',
        tenantIdVariable: 'ARM_TENANT_ID'
      )
    ]) {

      this.azureLogin()

      String chartPackageName = this.getChartPackageName()
      String chartPackageVersion = this.getChartPackageVersion()

      this.packageChart(chartPackageVersion)

      String chartPackage = "${chartPackageName}:$chartPackageVersion"

      this.helmLogin()

      steps.sh(
        """#!/usr/bin/env bash

          export HELM_EXPERIMENTAL_OCI=1

          cd "infrastructure/helm/$chartName"

          helm chart save . "${azureRegistry}.azurecr.io/helm/$chartPackage"
          helm chart push "${azureRegistry}.azurecr.io/helm/$chartPackage"
        """
      )
    }
  }
}