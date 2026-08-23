FROM ubuntu:26.04 AS builder

ENV TZ=Europe/Amsterdam
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

RUN apt-get clean
RUN apt-get update --fix-missing

RUN apt-get install -y --no-install-recommends \
    build-essential chrpath cpio debianutils diffstat file gawk gcc git \
    iputils-ping libacl1 libcrypt-dev locales python3 python3-git \
    python3-jinja2 python3-pexpect python3-pip python3-subunit socat \
    texinfo unzip wget xz-utils zstd python3-websockets \
	neovim tmux

ENV LANG=en_US.UTF-8
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && locale-gen

ARG DOCKER_USER=ubuntu
ARG DOCKER_UID=1001
ARG DOCKER_GID=1001
#RUN groupadd -g ${DOCKER_GID} ${DOCKER_USER} && \
#    useradd -m -u ${DOCKER_UID} -g ${DOCKER_GID} -s /bin/bash ${DOCKER_USER} && \
#    usermod -aG sudo ${DOCKER_USER}
    
RUN echo 'root:docker' | chpasswd
USER ${DOCKER_USER}

WORKDIR /home/${DOCKER_USER}/workspace
COPY --chown=${DOCKER_USER}:${DOCKER_USER} bashrc /home/${DOCKER_USER}/.bashrc

CMD ["/bin/bash"]

