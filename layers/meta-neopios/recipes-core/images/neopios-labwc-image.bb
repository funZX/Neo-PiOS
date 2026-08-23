# neopios-labwc-image.bb - Wayland-only labwc image
#
# Labwc wlroots-based compositor, Wayland-only (no X11).
# Shared payload: minimal in include/neopios-common.inc, hobby/edu/thin
# extra in include/neopios-extra.inc (NEOPIOS_EXTRA=1 by default);
# base boot + LICENSE via include/core-image-neopios.inc.
# Global DISTRO_FEATURES stays "opengl wayland pam" — enforcement
# is per-image via REQUIRED_DISTRO_FEATURES + features_check.

require include/core-image-neopios.inc
require include/neopios-common.inc
require include/neopios-extra.inc

SUMMARY = "Neo-PiOS Labwc Wayland-only Image"
LICENSE = "MIT"

inherit core-image features_check

IMAGE_INSTALL:append = " labwc"

REQUIRED_DISTRO_FEATURES = "wayland"
