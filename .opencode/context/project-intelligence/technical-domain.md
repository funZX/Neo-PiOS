<!-- Context: project-intelligence/technical | Priority: critical | Version: 1.0 | Updated: 2026-08-24 -->

# Technical Domain — Neo-PiOS

**Purpose**: Tech stack, recipe patterns, and development standards for the Neo-PiOS embedded Linux distribution.
**Last Updated**: 2026-08-24

## Quick Reference
**Update Triggers**: Yocto release change | New image variant | Pattern changes | Layer repinning
**Audience**: Developers, AI agents

## Primary Stack

| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| Build system | Yocto/OpenEmbedded | wrynose 6.0 | Reproducible embedded distro |
| Build tool | BitBake (podman container) | — | No host pollution; `source environment` → `bb.shell` |
| Target | raspberrypi4-64 | aarch64 | Single-machine focus |
| Init | OpenRC (meta-openrc) | 0.61 | Simple, fast; no systemd |
| Display | Wayland-only (Weston / Labwc) | 15.0 / wlroots 0.19 | Modern graphics; no X11 |
| Kernel | linux-raspberrypi | wrynose pin | Pi 4 BSP |
| Packages | opkg (`package_ipk`) | — | On-device install without rebuild |

## Code Patterns

### Image Recipe (canonical form)
```bitbake
require include/core-image-neopios.inc
require include/neopios-common.inc      # minimal payload + ssh-server-openssh
require include/neopios-extra.inc       # NEOPIOS_EXTRA=1 flag-guarded payload
SUMMARY = "Neo-PiOS <X> Wayland-only Image"
LICENSE = "MIT"
inherit core-image features_check
IMAGE_INSTALL:append = " <compositor>"
REQUIRED_DISTRO_FEATURES = "wayland"
```

### Distro Config
```bitbake
DISTRO_VERSION = "26.08"                          # YY.MM Ubuntu-style
DISTRO_FEATURES:append = " opengl wayland pam wifi vulkan"
DISTRO_FEATURES_OPTED_OUT:append = " ptest multiarch x11 pcmcia 3g"
INIT_MANAGER = "openrc"
```

### Flag-Guarded Payload
```bitbake
NEOPIOS_EXTRA ?= "1"
IMAGE_INSTALL:append = " ${@bb.utils.contains('NEOPIOS_EXTRA', '1', 'pkg1 pkg2', '', d)}"
```

### User Creation (extrausers)
```bitbake
IMAGE_CLASSES += "extrausers"
EXTRA_USERS_PARAMS = "useradd -p '<sha512-hash>' -s /bin/bash -m -U pi; "
```

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Image recipes | lowercase-hyphen `.bb` | `neopios-weston-image.bb` |
| Shared includes | kebab-case `.inc` in `include/` | `neopios-common.inc` |
| Bbappends | wildcard form | `weston_%.bbappend`, `openrc_%.bbappend` |
| Variables | UPPER_SNAKE_CASE | `NEOPIOS_EXTRA`, `DISTRO_VERSION` |
| Features/packages | lowercase-hyphen | `ssh-server-openssh` |
| Layers | `meta-<name>` | `meta-neopios` |
| Kernel cfg fragments | kebab-case `.cfg` | `wifi-brcmfmac.cfg` |

## Code Standards

- First-party changes **only** in `layers/meta-neopios/`
- Upstream layer changes encoded as `layers/<layer-name>.patch` (auto-applied after submodule sync)
- Use `:append` / `:remove` operators — never `+=` on `IMAGE_INSTALL`
- Optional payloads flag-guarded via `bb.utils.contains`
- Builds run **only inside podman container** (`source environment` → `bb.shell`)
- Verification = successful `bitbake` of affected target (no test suite exists)
- Workflow: changes on `develop` branch → PR against `master`; never push without approval

## Security Requirements

- Default user `pi` (password auth); root SSH password login denied in production images
- `pam` in DISTRO_FEATURES (required by sudo, weston VNC/openrc PAM)
- SSH via `ssh-server-openssh` IMAGE_FEATURE (service + keys), not raw package
- `x11` opted out of DISTRO_FEATURES — Wayland-only attack surface
- Dev-only relaxations (`allow-root-login`, `empty-root-password`) confined to `neopios-common-dev.inc`

## 📂 Codebase References

**Image recipes**: `layers/meta-neopios/recipes-core/images/*.bb` — canonical pattern above
**Shared payload**: `layers/meta-neopios/recipes-core/images/include/neopios-{common,extra,common-dev}.inc`
**Distro config**: `layers/meta-neopios/conf/distro/neopios.conf` — features, versioning, init manager
**Bbappends**: `recipes-graphics/wayland/weston_%.bbappend` (VNC), `recipes-init/openrc/openrc_%.bbappend` (PAM fix)
**Kernel Wi-Fi**: `recipes-kernel/linux/linux-raspberrypi_%.bbappend` + `wifi-brcmfmac.cfg`
**Build orchestration**: `environment` (podman + submodules), `docker/Dockerfile`, `build/conf/{local,bblayers}.conf`

## Related Files
- Navigation: `project-intelligence/navigation.md`
