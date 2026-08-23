# neopios-xwayland-image.bb - Neo-PiOS Hybrid XWayland Image
#
# Provides Wayland/Weston with X11 via weston-xwayland on top of the
# common Neo-PiOS payload in include/neopios-common.inc.
#
# DISTRO_FEATURES stays "opengl wayland x11" globally; per-image
# enforcement is via REQUIRED_DISTRO_FEATURES + features_check.
# Follow wrynose 6.0 conventions, MACHINE=raspberrypi4-64, INIT_MANAGER=openrc.
# Yocto style: inherit core-image features_check, use IMAGE_FEATURES for
# display backends, IMAGE_INSTALL:append for XWayland compat.

require include/core-image-neopios.inc
require include/neopios-common.inc

SUMMARY = "Neo-PiOS XWayland Hybrid Image (Wayland/Weston + X11 via XWayland)"
DESCRIPTION = "Hybrid display image with Weston/Wayland and X11 compatibility via weston-xwayland. Retains common Neo-PiOS tooling from neopios-common.inc."

LICENSE = "MIT"

inherit core-image features_check

IMAGE_FEATURES:append = " x11 weston"

# XWayland compatibility - allows X11 clients on Weston
IMAGE_INSTALL:append = " weston-xwayland"

REQUIRED_DISTRO_FEATURES = "wayland x11"
