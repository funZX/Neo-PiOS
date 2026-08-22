IMAGE_DEPENDS = "virtual/kernel"
LICENSE = "MIT"

inherit core-image

IMAGE_INSTALL = " \
    packagegroup-core-boot \
    \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"