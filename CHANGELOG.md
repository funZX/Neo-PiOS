# Neo-PiOS Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Ubuntu-style versioning](https://ubuntu.com/about/release-cycle) (YY.MM).

## [28.08] - 2026-08-24

### Neo-PiOS 28.08 — First Release

**Wayland-only embedded Linux for Raspberry Pi 4 (64-bit)**

#### Display Options
- **neopios-weston-image** — Weston compositor (Wayland reference implementation)
- **neopios-labwc-image** — Labwc (wlroots-based, lightweight tiling window manager)

#### Core Features
- **Wayland-only graphics stack** — no X11, `DISTRO_FEATURES = "opengl wayland pam wifi"`
- **OpenRC init system** — simple, fast, no systemd
- **VNC backend** — headless access via `weston --backend=vnc` (neatvnc + aml)
- **Pi 4 Wi-Fi** — onboard brcmfmac + bcm43455 firmware
- **Package management** — opkg for on-device package installation

#### Education & Hobby Payload (NEOPIOS_EXTRA=1 default)
- **Python 3** — python3, pip, pyserial for scripting and education
- **GPIO/I2C** — i2c-tools, libgpiod for hardware projects
- **Development tools** — git, vim, nano, htop, usbutils
- **Audio** — pulseaudio, alsa-utils
- **Documentation** — man pages, bash-completion

#### Minimal Variant
- Set `NEOPIOS_EXTRA = "0"` for factory image (~110-170MB rootfs)
- Boot + display compositor only, no extra packages

#### Build System
- Yocto/OpenEmbedded (wrynose 6.0)
- Reproducible podman build container
- Pinned layer submodules

---

## Upcoming (28.11)

### Planned
- [ ] SDK/populate_sdk validation
- [ ] Automated build/release pipeline

---

## Release Notes Links

- [28.08 Release](https://github.com/funZX/Neo-PiOS/releases/tag/28.08) (when published)