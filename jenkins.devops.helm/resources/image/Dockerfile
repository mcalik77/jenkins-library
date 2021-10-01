FROM debian:latest

ARG HELM_VERSION

ENV HELM_TAR="helm-v${HELM_VERSION}-linux-amd64.tar.gz"
ENV HELM_URL="https://get.helm.sh/$HELM_TAR"

RUN apt-get update \
 && apt-get install --yes \
   curl \
   git \
   unzip \
   wget \
 && rm --recursive --force /var/lib/apt/lists/* \
 && addgroup --gid 986 jenkins \
 && adduser \
  --disabled-password \
  --ingroup jenkins \
  --uid 990 \
  --gecos "" jenkins \
 && curl --location https://aka.ms/InstallAzureCLIDeb | bash \
 && az aks install-cli \
 && curl --remote-name --location "$HELM_URL" \
 && tar --gzip --extract --file "$HELM_TAR" \
 && mv linux-amd64/helm /usr/local/bin

USER jenkins:jenkins