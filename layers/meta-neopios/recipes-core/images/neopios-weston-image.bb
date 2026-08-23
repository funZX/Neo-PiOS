# neopios-weston-image.bb - Wayland-only strict image
#
# Strict Wayland image using weston compositor only.
# Requires DISTRO_FEATURE wayland; does not enable hybrid graphics.
# Shared payload (EXTRA_IMAGE_FEATURES, common IMAGE_INSTALL) lives in
# include/neopios-common.inc; base boot + LICENSE via include/core-image-neopios.inc.
# Global DISTRO_FEATURES unchanged - enforcement is per-image
# via REQUIRED_DISTRO_FEATURES + features_check.

require include/core-image-neopios.inc
require include/neopios-common.inc

SUMMARY = "Neo-PiOS Weston Wayland-only Image"
LICENSE = "MIT"

inherit core-image features_check

IMAGE_FEATURES:append = " weston"

IMAGE_INSTALL:append = " weston"

REQUIRED_DISTRO_FEATURES = "wayland"
