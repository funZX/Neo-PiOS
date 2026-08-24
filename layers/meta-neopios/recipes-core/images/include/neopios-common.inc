# neopios-common.inc - Shared minimal payload for Neo-PiOS display images
#
# Minimal production payload shared across all Neo-PiOS distribution
# images (weston, xwayland, x11 variants).
# Keeps distro global DISTRO_FEATURES (opengl wayland pam wifi) unchanged;
# per-image enforcement is via REQUIRED_DISTRO_FEATURES in each .bb.
#
# Yocto style: no inherit here (handled in .bb), no REQUIRED_DISTRO_FEATURES,
# no LICENSE/SUMMARY (set in .bb or core-image-neopios.inc).
# Uses :append to extend IMAGE_INSTALL defined in include/core-image-neopios.inc
# which provides packagegroup-core-boot + CORE_IMAGE_EXTRA_INSTALL.

EXTRA_IMAGE_FEATURES = "package-management ssh-server-openssh"

# Default user: pi / Neo-PiOS
IMAGE_CLASSES += "extrausers"
EXTRA_USERS_PARAMS = "useradd -p '$6$PzWThDaC3XUx7eAq$G9ugomfvrSb/yJbE6xWqnUeKI4YCgjh/g/QUSS5pUkVSW3RUwgp7y6tq6yjRZj0lfIaQGWMdCxNY2xZNTE0f31' -s /bin/bash -m -U pi; "

IMAGE_INSTALL:append = " \
    bash \
    coreutils \
    iproute2 \
    iputils \
    dhcpcd \
    avahi-daemon \
    kmod \
    procps \
    psmisc \
    util-linux \
    sudo \
    tzdata-core \
"
