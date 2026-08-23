# neopios-weston-image.bb - Wayland-only strict image
#
# Strict Wayland image using weston compositor only.
# Requires DISTRO_FEATURE wayland; does not enable hybrid graphics.
# Shared payload: minimal in include/neopios-common.inc, hobby/edu/thin
# extra in include/neopios-extra.inc (NEOPIOS_EXTRA=1 by default);
# base boot + LICENSE via include/core-image-neopios.inc.
# Global DISTRO_FEATURES unchanged - enforcement is per-image
# via REQUIRED_DISTRO_FEATURES + features_check.

require include/core-image-neopios.inc
require include/neopios-common.inc
require include/neopios-extra.inc

SUMMARY = "Neo-PiOS Weston Wayland-only Image"
LICENSE = "MIT"

inherit core-image features_check

IMAGE_FEATURES:append = " weston"

IMAGE_INSTALL:append = " weston"

REQUIRED_DISTRO_FEATURES = "wayland"
