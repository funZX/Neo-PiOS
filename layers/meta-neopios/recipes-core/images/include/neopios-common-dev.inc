# neopios-common-dev.inc - Dev add-on for Neo-PiOS display images
#
# Extends neopios-common.inc (minimal production) with debug/profile
# conveniences and extra tooling. Keeps package-management (opkg) from
# minimal, adds tools-debug/tools-profile and empty-password for dev.
# Drops python3/icu (removed from minimal) and keeps dev extras lean
# (no ifplugd/resolvconf per request).
#
# Usage: add `require include/neopios-common-dev.inc` *instead of*
# `require include/neopios-common.inc` in a -dev image, or include
# both where minimal is already required.

require include/neopios-common.inc

EXTRA_IMAGE_FEATURES:append = " tools-debug tools-profile post-install-logging allow-empty-password allow-root-login empty-root-password"

IMAGE_INSTALL:append = " \
    gdb \
    iproute2-tc \
    iproute2-ss \
    net-tools \
    iptraf \
"
