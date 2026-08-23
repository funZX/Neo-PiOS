# neopios-common-dev.inc - Dev add-on for Neo-PiOS display images
#
# Extends neopios-common.inc with debug/profile conveniences and
# extra tooling for development images.
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
