FROM ubuntu:26.04

ENV TZ=Europe/Bucharest
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

RUN apt-get clean
RUN apt-get update --fix-missing

RUN apt-get install -y \
    build-essential chrpath cpio debianutils diffstat file gawk gcc git \
    iputils-ping libacl1 libcrypt-dev locales python3 python3-git \
    python3-jinja2 python3-pexpect python3-pip python3-subunit socat \
    texinfo unzip wget xz-utils zstd python3-websockets \
	neovim tmux

ENV LANG=en_US.UTF-8
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && locale-gen

ARG DOCKER_USER=ubuntu
ARG DOCKER_UUID=1001
ARG DOCKER_UGID=1001
RUN groupadd -g ${DOCKER_UGID} ${DOCKER_USER} && \
    useradd -m -g ${DOCKER_UGID} -s /bin/bash -u ${DOCKER_UUID} ${DOCKER_USER}
    
RUN echo 'root:docker' | chpasswd
USER ${DOCKER_USER}

COPY bashrc /home/${DOCKER_USER}/.bashrc
COPY job.sh /home/${DOCKER_USER}/job.sh
COPY exec.sh /home/exec.sh

CMD ["/home/exec.sh"]

