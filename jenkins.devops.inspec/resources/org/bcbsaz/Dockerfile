ARG INSPEC_VER
FROM chef/inspec:${INSPEC_VER}

ARG http_proxy='http://lp-nex-a01.corp.net.bcbsaz.com:3128'
ARG https_proxy='http://lp-nex-a01.corp.net.bcbsaz.com:3128'

ENV http_proxy="$http_proxy"
ENV https_proxy="$https_proxy"

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install --yes \
   apt-utils \
   bash \
   curl \
   git \
   ssh \
   wget \
   unzip \
 && rm --recursive --force /var/lib/apt/lists/* \
 && addgroup --gid 986 jenkins \
 && adduser \
   --disabled-password \
   --ingroup jenkins \
   --uid 990 \
   --gecos "" jenkins \
 && curl -sL https://aka.ms/InstallAzureCLIDeb | bash  

RUN wget --quiet https://releases.hashicorp.com/terraform/0.13.5/terraform_0.13.5_linux_amd64.zip \
 && unzip terraform_0.13.5_linux_amd64.zip \
 && mv terraform /usr/bin \
 && rm terraform_0.13.5_linux_amd64.zip

USER jenkins:jenkins