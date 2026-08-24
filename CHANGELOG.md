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

## Upcoming (26.11)

### Planned
- [ ] SDK/populate_sdk validation
- [ ] Automated build/release pipeline

---

## Release Notes Links

- [26.08 Release](https://github.com/funZX/Neo-PiOS/releases/tag/26.08) (when published)