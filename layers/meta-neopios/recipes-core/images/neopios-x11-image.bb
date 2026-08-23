require include/core-image-neopios.inc
require include/neopios-common.inc

SUMMARY = "Neo-PiOS X11-only image (X11 without compositors)"
LICENSE = "MIT"

inherit core-image features_check

IMAGE_FEATURES += "splash x11 x11-base"

REQUIRED_DISTRO_FEATURES = "x11"

IMAGE_INSTALL:append = " packagegroup-core-x11-base"

QB_MEM = '${@bb.utils.contains("DISTRO_FEATURES", "opengl", "-m 512", "-m 256", d)}'
