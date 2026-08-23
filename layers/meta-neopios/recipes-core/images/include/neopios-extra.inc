# neopios-extra.inc - Hobby/DIY + Education + Thin Client extra payload
#
# Extends minimal (neopios-common.inc) with hobby/education/thin-client
# packages. Flag-guarded by NEOPIOS_EXTRA (defaults to 1). Set
# NEOPIOS_EXTRA = "0" in build/conf/local.conf or via
# `NEOPIOS_EXTRA=0 bitbake <image>` to build minimal without extra.
# Requires DISTRO_FEATURES unchanged; per-image
# REQUIRED_DISTRO_FEATURES stays in each .bb.

NEOPIOS_EXTRA ?= "1"

IMAGE_INSTALL:append = " \
    ${@bb.utils.contains('NEOPIOS_EXTRA', '1', ' \
        python3 python3-pip python3-pyserial \
        i2c-tools libgpiod libgpiod-tools \
        git vim nano htop usbutils \
        geany man man-pages bash-completion \
        epiphany freerdp remmina gtk-vnc \
        ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'tigervnc x11vnc', '', d)} \
        pulseaudio alsa-utils \
    ', '', d)} \
"
