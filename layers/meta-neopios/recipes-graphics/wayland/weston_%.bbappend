# Enable VNC backend for Weston via neatvnc+aml (shared across all Neo-PiOS
# distribution images). Allows `weston --backend=vnc` and
# `weston.ini [core] backend=vnc-backend.so` for headless/VNC sharing.
# Depends on neatvnc + aml (async loop, from meta-oe/recipes-graphics/aml
# filebase 'aml1' -> aml1.pc, hence patched meson to use 'aml1')
# + libpam (pam is in DISTRO_FEATURES for openrc).

FILESEXTRAPATHS:prepend := "${THISDIR}/weston:"
SRC_URI:append = " file://0001-meson-fix-aml-pkgconfig-name.patch"

PACKAGECONFIG:append = " vnc"
PACKAGECONFIG[vnc] = "-Dbackend-vnc=true,-Dbackend-vnc=false,neatvnc aml libpam"
