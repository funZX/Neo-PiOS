# Enable VNC backend for Weston via neatvnc (shared across all Neo-PiOS
# distribution images). Allows `weston --backend=vnc` and
# `weston.ini [core] backend=vnc-backend.so` for headless/VNC sharing.
# Depends on neatvnc + libpam (pam is in DISTRO_FEATURES for openrc).

PACKAGECONFIG:append = " vnc"
