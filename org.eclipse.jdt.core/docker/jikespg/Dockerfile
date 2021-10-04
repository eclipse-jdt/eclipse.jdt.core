FROM ubuntu:20.04

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
COPY scripts/uid_entrypoint /usr/local/bin/uid_entrypoint
RUN chmod u+x /usr/local/bin/uid_entrypoint && \
    chgrp 0 /usr/local/bin/uid_entrypoint && \
    chmod g=u /usr/local/bin/uid_entrypoint /etc/passwd
ENTRYPOINT [ "uid_entrypoint" ]

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt upgrade -y && apt dist-upgrade -y && apt-get install -y --no-install-recommends \
      wget \
      curl \
      unzip \
      vim \
      gcc \
      g++ \
      make \
      git \
    && rm -rf /var/lib/apt/lists/* && apt autoremove -y

ENV HOME=/home
ENV DISPLAY :0

RUN git config --global http.sslverify false

RUN mkdir -p ${HOME}/git && cd ${HOME}/git \
  && git clone -b fixes-combined https://github.com/jikespg/jikespg.git \
  && cd jikespg/src; make clean; make

USER 10001
