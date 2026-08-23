# neopios-common.inc - Shared payload for Neo-PiOS display images
#
# Common EXTRA_IMAGE_FEATURES and IMAGE_INSTALL payload shared across
# neopios-weston-image, neopios-xwayland-image and neopios-x11-image.
# Keeps distro global DISTRO_FEATURES (opengl wayland x11) unchanged;
# per-image enforcement is via REQUIRED_DISTRO_FEATURES in each .bb.
#
# Yocto style: no inherit here (handled in .bb), no REQUIRED_DISTRO_FEATURES,
# no LICENSE/SUMMARY (set in .bb or core-image-neopios.inc).
# Uses :append to extend IMAGE_INSTALL defined in include/core-image-neopios.inc
# which provides packagegroup-core-boot + CORE_IMAGE_EXTRA_INSTALL.

EXTRA_IMAGE_FEATURES = "allow-empty-password allow-root-login empty-root-password post-install-logging package-management tools-debug tools-profile"

IMAGE_INSTALL:append = " \
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
    icu \
    tzdata \
"
