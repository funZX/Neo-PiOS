# Enable VNC backend for Weston via neatvnc+aml (shared across all Neo-PiOS
# distribution images). Allows `weston --backend=vnc` and
# `weston.ini [core] backend=vnc-backend.so` for headless/VNC sharing.
# Depends on neatvnc + aml (async loop, from meta-oe/recipes-graphics/aml)
# + libpam (pam is in DISTRO_FEATURES for openrc).

PACKAGECONFIG:append = " vnc"
PACKAGECONFIG[vnc] = "-Dbackend-vnc=true,-Dbackend-vnc=false,neatvnc aml libpam"
