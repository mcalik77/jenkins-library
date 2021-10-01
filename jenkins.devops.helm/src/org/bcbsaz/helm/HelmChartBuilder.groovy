package org.bcbsaz.helm

class HelmChartBuilder implements Serializable {
  def steps

  private String repositoryType
  private String chartName
  private String chartVersion

  private String azureRegistry
  private String nexusRepository

  public HelmChartBuilder(steps) {
    this.steps = steps
  }

  public HelmChartBuilder setRepositoryType(String repositoryType) {
    this.repositoryType = repositoryType
    return this
  }

  public HelmChartBuilder setChartName(String chartName) {
    if (! chartName) {
      steps.error('chartName was not specified')
    }

    this.chartName = chartName
    return this
  }

  public HelmChartBuilder setChartVersion(String chartVersion) {
    this.chartVersion = chartVersion
    return this
  }

  public HelmChartBuilder setAzureRegistry(String azureRegistry) {
    if (this.repositoryType == 'acr' && ! azureRegistry) {
      steps.error('azureRegistry was not specified for repositoryType acr')
    }

    this.azureRegistry = azureRegistry
    return this
  }

  public HelmChartBuilder setNexusRepository(String nexusRepository) {
    this.nexusRepository = nexusRepository
    return this
  }

  public HelmChart build() {
    return new HelmChart(
      steps,

      repositoryType,
      chartName,
      chartVersion,

      azureRegistry,
      nexusRepository
    )
  }
}