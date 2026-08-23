require include/core-image-neopios.inc

SUMMARY = "Neo-PiOS Minimal Image"

EXTRA_IMAGE_FEATURES = "allow-empty-password allow-root-login empty-root-password post-install-logging package-management tools-debug tools-profile"
IMAGE_FEATURES:append = " x11 weston"

IMAGE_INSTALL:append = " \
\   
    weston-xwayland \
    buildah \
    podman \
\
    bash \
    coreutils \
    iproute2 \
    iproute2-tc \
    iproute2-ss \
    iputils \
    dhcpcd \
    ifplugd \
    resolvconf \
    kmod \
    net-tools \
    iptraf \
    procps \
    psmisc \
    util-linux \
    openssh \
    gdb \
    sudo \
    python3 \
\
    icu \
    tzdata \
"