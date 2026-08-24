# Neo-PiOS Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Ubuntu-style versioning](https://ubuntu.com/about/release-cycle) (YY.MM).

## [26.08] - 2026-08-24

### Added
- **Labwc compositor** — Wayland-only wlroots-based compositor as second display option (`neopios-labwc-image`)
- **Pi 4 Wi-Fi support** — brcmfmac driver + bcm43455 firmware via `linux-firmware-bcm43455`
- **Weston VNC backend** — headless/VNC access via `neatvnc` + `aml` (`weston --backend=vnc`)
- **meta-wayland layer** — added as submodule (codeberg.org/flk/meta-wayland, wrynose `3634f036`)
- **OpenRC PAM support** — `pam_openrc.so` packaged for OpenRC with PAM in DISTRO_FEATURES

### Changed
- **Wayland-only distro** — removed `x11` from `DISTRO_FEATURES` (now `opengl wayland pam wifi`)
- **Dropped X11 images** — removed `neopios-x11-image` and `neopios-xwayland-image`
- **Dropped epiphany browser** — requires X11, not compatible with Wayland-only
- **Dropped geany IDE** — removes GTK3 dependency stack (~35-60MB savings)
- **Dropped freerdp** — RDP client not needed for default education/hobby use (~20-30MB savings)
- **DISTRO_VERSION format** — changed to Ubuntu-style `YY.MM` (`26.08`)

### Fixed
- **Weston VNC build** — fixed aml pkg-config name (`aml1` not `aml`) and version constraint (`< 2.0.0` for aml 1.0.0)
- **OpenRC QA error** — `pam_openrc.so` now properly packaged when PAM enabled
- **meta-openrc submodule** — tracks upstream `master` (no local patch needed)

### Security
- **PAM enabled** — required for Weston VNC, sudo, shadow; `DISTRO_FEATURES` includes `pam`

---

## [26.05] - 2026-05-15 (Pre-release / Development)

### Added
- **Minimal production payload** — `neopios-common.inc` with core boot packages
- **Education/hobby extra payload** — `neopios-extra.inc` with `NEOPIOS_EXTRA` flag (default=1)
- **Weston compositor** — `neopios-weston-image` (Wayland reference compositor)
- **OpenRC init** — via meta-openrc layer (not systemd)
- **Package management** — opkg enabled by default
- **Python3 stack** — python3, pip, pyserial for education
- **GPIO tools** — i2c-tools, libgpiod for hardware projects
- **Dev tools** — git, vim, nano, htop, usbutils
- **Audio** — pulseaudio, alsa-utils
- **Thin client** — freerdp (later removed in 26.08)

### Changed
- **DISTRO_FEATURES** — `opengl wayland pam wifi` (global)
- **Image structure** — shared `neopios-common.inc` + optional `neopios-extra.inc`

### Fixed
- **meta-openrc integration** — local patch for OpenRC on Raspberry Pi

---

## [26.02] - 2026-02-20 (Initial Yocto Setup)

### Added
- **Yocto/OpenEmbedded build** — wrynose 6.0, Raspberry Pi 4 64-bit
- **Base layers** — openembedded-core, meta-openembedded, meta-raspberrypi, meta-openrc
- **meta-neopios layer** — distro config, image recipes, custom patches
- **Podman build container** — reproducible build environment
- **Submodule management** — pinned layers in `environment` script

---

## Upcoming (26.11)

### Planned
- [ ] Browser support (cog + wpe-webkit via meta-webkit)
- [ ] Modular extra payload flags (NEOPIOS_EXTRA_PYTHON, NEOPIOS_EXTRA_AUDIO, etc.)
- [ ] OTA update framework (swupdate integration)
- [ ] SDK/populate_sdk validation
- [ ] Automated build/release pipeline

### Under Consideration
- [ ] Rust toolchain (meta-rust)
- [ ] Container runtime (podman/cri-o on target)
- [ ] Hardware video decode (v4l2, ffmpeg)
- [ ] Secure boot / signed images

---

## Version History

| Version | Date | Codename | Key Changes |
|---------|------|----------|-------------|
| 26.08 | 2026-08-24 | — | Labwc, Wi-Fi, VNC, Wayland-only, size optimization |
| 26.05 | 2026-05-15 | — | Minimal payload, extra payload, Weston, OpenRC |
| 26.02 | 2026-02-20 | — | Initial Yocto setup, layer structure |

---

## Release Notes Links

- [26.08 Release](https://github.com/funZX/Neo-PiOS/releases/tag/26.08) (when published)
- [26.05 Development](https://github.com/funZX/Neo-PiOS/compare/26.02...26.05)
- [26.02 Initial](https://github.com/funZX/Neo-PiOS/commits/26.02)