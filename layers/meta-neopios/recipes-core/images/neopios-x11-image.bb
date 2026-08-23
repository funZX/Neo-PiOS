# neopios-x11-image.bb - X11-only image
#
# Strict X11 image without Wayland/Weston.
# Requires DISTRO_FEATURE x11; does not enable Wayland.
# Shared payload: minimal in include/neopios-common.inc, hobby/edu/thin
# extra in include/neopios-extra.inc (NEOPIOS_EXTRA=1 by default);
# base boot + LICENSE via include/core-image-neopios.inc.
# Global DISTRO_FEATURES unchanged - enforcement is per-image
# via REQUIRED_DISTRO_FEATURES + features_check.

require include/core-image-neopios.inc
require include/neopios-common.inc
require include/neopios-extra.inc

SUMMARY = "Neo-PiOS X11-only image (X11 without compositors)"
LICENSE = "MIT"

inherit core-image features_check

IMAGE_FEATURES += "splash x11 x11-base"

REQUIRED_DISTRO_FEATURES = "x11"

IMAGE_INSTALL:append = " packagegroup-core-x11-base tigervnc x11vnc"

QB_MEM = '${@bb.utils.contains("DISTRO_FEATURES", "opengl", "-m 512", "-m 256", d)}'
