# Neo-PiOS

A custom embedded Linux distribution built with [Yocto/OpenEmbedded](https://www.yoctoproject.org/) (**wrynose 6.0**) for the Raspberry Pi 4 (64-bit), using **OpenRC** instead of systemd.

## Features

- Machine `raspberrypi4-64`, distro `neopios`, kernel `linux-raspberrypi`
- OpenRC init (`INIT_MANAGER = "openrc"` via `meta-openrc`)
- Three display variants sharing a common payload (`include/neopios-common.inc`):
  - `neopios-weston-image` — Wayland-only (strict, no X11) — Weston compositor only
  - `neopios-xwayland-image` — hybrid XWayland (Wayland/Weston + X11 via `weston-xwayland`)
  - `neopios-x11-image` — X11-only (no Wayland/Weston) — `x11`/`x11-base` + `packagegroup-core-x11-base`
- openssh and essential networking/tooling out of the box (minimal via `neopios-common.inc` + `package-management`/`opkg`, dev adds debug via `neopios-common-dev.inc`)
- Reproducible builds: upstream layers pinned via git submodules + patches
- Fully containerized builds via rootless podman — no host pollution

## Repository layout

| Path | Purpose |
|------|---------|
| `layers/meta-neopios/` | First-party layer: distro config (`conf/distro/neopios.conf`) + display images (`recipes-core/images/neopios-weston-image.bb`, `neopios-xwayland-image.bb`, `neopios-x11-image.bb`) + shared payload (minimal `recipes-core/images/include/neopios-common.inc`, dev `recipes-core/images/include/neopios-common-dev.inc`) + base boot include (`recipes-core/images/include/core-image-neopios.inc`) |
| `layers/meta-neopios/recipes-core/images/neopios-weston-image.bb` | Wayland-only image (IMAGE_FEATURES `weston`, REQUIRED_DISTRO_FEATURES `wayland`) |
| `layers/meta-neopios/recipes-core/images/neopios-xwayland-image.bb` | Hybrid XWayland image (IMAGE_FEATURES `x11 weston`, `weston-xwayland`, REQUIRED_DISTRO_FEATURES `wayland x11`) |
| `layers/meta-neopios/recipes-core/images/neopios-x11-image.bb` | X11-only image (IMAGE_FEATURES `x11 x11-base`, `packagegroup-core-x11-base`, REQUIRED_DISTRO_FEATURES `x11`) |
| `layers/meta-neopios/recipes-core/images/include/neopios-common.inc` | Minimal `EXTRA_IMAGE_FEATURES` (`package-management`) + `IMAGE_INSTALL` for all three images |
| `layers/meta-neopios/recipes-core/images/include/neopios-common-dev.inc` | Dev add-on extending minimal with `tools-debug`/`tools-profile`, `post-install-logging`, empty-password and extra tools (`gdb`, `net-tools`, `iptraf`) |
| `layers/bitbake` | BitBake build tool |
| `layers/openembedded-core` | OE-Core: base metadata, classes, and the `oe-init-build-env` entrypoint |
| `layers/meta-openembedded` | Extra recipes (meta-oe, meta-perl, meta-python, meta-networking, meta-filesystems sublayers) |
| `layers/meta-openrc` | OpenRC init support (tracking `master`, wrynose-compatible upstream) |
| `layers/meta-raspberrypi` | Raspberry Pi BSP (machine configs, boot firmware) |
| `layers/<name>.patch` | Per-layer patches applied automatically after pinned checkout |
| `environment` | Host helper: builds the container image, syncs submodules, defines `bb.*` functions |
| `docker/` | Container image definition (podman, despite the directory name) |
| `build/` | BitBake build dir with tracked `conf/local.conf` + `conf/bblayers.conf` |

## Requirements

- Linux host with **podman** (rootless). All other build dependencies live inside the container image (`ubuntu:26.04` base), which is built automatically on first use.
- Generous disk space — a full build consumes tens of GB under `build/tmp*`, `build/sstate-cache/`, and `downloads/`.
- Plenty of RAM — if the build machine OOMs, add a 16 GiB swapfile:

  ```sh
  cd build && sudo ./mkswap.sh
  ```

## Building

All builds run inside a rootless podman container — never on the host directly.

```sh
# from the repo root
source environment        # builds the container image on first use; syncs submodules
bb.shell                  # interactive shell in the container (repo mounted at ~/workspace)

# inside the container (oe-init auto-sourced via ~/.bash_profile on bb.shell -l login)
bitbake neopios-weston-image    # Wayland-only (strict, no X11)
bitbake neopios-xwayland-image  # hybrid XWayland (Wayland/Weston + X11 via weston-xwayland)
bitbake neopios-x11-image       # X11-only (no Wayland/Weston)
```

Notes:

- The first build downloads all sources and compiles everything from scratch — expect hours. Subsequent builds reuse `sstate-cache/` and `downloads/`, so they are much faster.
- `PODMAN_WORKDIR` is captured when `environment` is sourced, so always source it from the repo root.
- Images land in `build/tmp/deploy/images/raspberrypi4-64/`.
- `DISTRO_FEATURES` stays `opengl wayland x11` globally (wrynose 6.0, `INIT_MANAGER = "openrc"`, `MACHINE = "raspberrypi4-64"`); per-image enforcement is via `REQUIRED_DISTRO_FEATURES` + `features_check` (see table below).


### Host-side helper (after `source environment`)

| Command | Description |
|---------|-------------|
| `bb.shell` | Interactive shell in the container |

### BitBake aliases (available at login via `~/.bash_profile` — inside `if [ -f ...oe-init-build-env ]` block, `oe-init-build-env` sourced automatically on login)

| Alias | Action |
|-------|--------|
| `bb` | bitbake |
| `bb.clean` | clean + cleansstate |
| `bb.edit <recipe>` | devtool modify |
| `bb.apply` | devtool update-recipe → writes changes back into `meta-neopios` |
| `bb.close` | devtool reset |
| `bb.linux` | kernel tasks (`bitbake linux -c ...`) |
| `bb.sysroot` | build target sysroot |
| `bb.sdk` | populate SDK installer |

### Other common targets

```sh
bitbake core-image-minimal     # tiny console-only image
bitbake core-image-weston      # upstream weston without the neopios extras
```

## Distro & image configuration

Key settings and where they live:

| Setting | Value | Defined in |
|---------|-------|------------|
| `MACHINE` | `raspberrypi4-64` | `build/conf/local.conf` |
| `DISTRO` | `neopios` | `build/conf/local.conf` |
| `INIT_MANAGER` | `openrc` | `meta-neopios/conf/distro/neopios.conf` |
| Kernel provider | `linux-raspberrypi` | `meta-neopios/conf/distro/include/neopios.inc` |
| `PACKAGE_CLASSES` | `package_ipk` | `build/conf/local.conf` |
| `DISTRO_FEATURES` | + `opengl wayland x11` | `neopios.conf` (global, unchanged) |
| Opted-out features | `ptest vulkan multiarch` | `neopios.conf` (wrynose mechanism) |

### Display images — comparison

Three variants share `include/neopios-common.inc` (`EXTRA_IMAGE_FEATURES` + common `IMAGE_INSTALL`) and `include/core-image-neopios.inc` (boot + `LICENSE`). Each inherits `core-image` + `features_check` and enforces its required distro features; global `DISTRO_FEATURES` stays `opengl wayland x11`.

| Image | `IMAGE_FEATURES` | `REQUIRED_DISTRO_FEATURES` | Display stack | Packages (on top of common) | Use case |
|-------|------------------|-----------------------------|---------------|------------------------------|----------|
| `neopios-weston-image` | `weston` | `wayland` | Wayland-only, Weston compositor | `weston` | Pure Wayland kiosk / Weston-only, strict no X11 |
| `neopios-xwayland-image` | `x11 weston` | `wayland x11` | Hybrid XWayland | `weston-xwayland` | Hybrid — Wayland + X11 apps via XWayland |
| `neopios-x11-image` | `x11 x11-base` (+ `splash`) | `x11` | X11-only | `packagegroup-core-x11-base` | Legacy X11 desktop, no Wayland/Weston |

Common payload:

- Minimal (`neopios-common.inc` for all three): `EXTRA_IMAGE_FEATURES = "package-management"` (`opkg`); `IMAGE_INSTALL` = `bash`, `coreutils`, `iproute2`, `iputils`, `dhcpcd`, `kmod`, `procps`, `psmisc`, `util-linux`, `openssh`, `sudo`, `tzdata-core`
- Dev add-on (`neopios-common-dev.inc` extends minimal): adds `tools-debug`, `tools-profile`, `post-install-logging`, `allow-empty-password`/`allow-root-login`/`empty-root-password` and `gdb`, `iproute2-tc`/`ss`, `net-tools`, `iptraf`

Variant notes:

- `neopios-weston-image.bb`: `IMAGE_FEATURES:append = " weston"`, `IMAGE_INSTALL:append = " weston"`, `REQUIRED_DISTRO_FEATURES = "wayland"` — strict Wayland, no `x11` feature, no `weston-xwayland`.
 - `neopios-xwayland-image.bb`: `IMAGE_FEATURES:append = " x11 weston"`, `IMAGE_INSTALL:append = " weston-xwayland"`, `REQUIRED_DISTRO_FEATURES = "wayland x11"` — hybrid.
- `neopios-x11-image.bb`: `IMAGE_FEATURES += "splash x11 x11-base"`, `IMAGE_INSTALL:append = " packagegroup-core-x11-base"`, `REQUIRED_DISTRO_FEATURES = "x11"` — X11-only, no Weston.

## Booting the image

1. Flash the generated image file from `build/tmp/deploy/images/raspberrypi4-64/` onto an SD card (e.g. with `dd`, Raspberry Pi Imager, or `balenaEtcher`).
2. Insert the card into the Raspberry Pi 4 and power it on.
3. The system boots into the selected display stack (Weston for `neopios-weston-image`/`neopios-xwayland-image`, X11 session for `neopios-x11-image`) with OpenRC as the init system.
4. SSH access is available over the network — root login with an empty password is enabled by default (development convenience; tighten before any real deployment).

## Customizing

- Distro/image changes belong in `layers/meta-neopios/` — that is the only layer this project owns.
- For quick recipe experiments inside the container, use the devtool workflow: `bb.edit <recipe>` → edit → `bb.apply` (writes back into `meta-neopios`) → `bb.close`.
- Changes to upstream layers must be encoded as `layers/<layer-name>.patch` — they are re-applied automatically after each submodule sync.
- ⚠️ Sourcing `environment` resets every submodule to its pinned revision (`git clean -fxd` + hard reset). Hand-edits inside submodule layers are wiped.
- When repinning a submodule, update **both** its git link and the hardcoded `SUBMODULES` list in `environment`.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Build killed / OOM | Add swap: `cd build && sudo ./mkswap.sh` |
| Disk-space abort from bitbake | Free space — disk monitoring halts builds below thresholds (`BB_DISKMON_DIRS` in `local.conf`) |
| Stale/stopped containers piling up | `podman rm $(podman ps -aq)` |
| Layer changes vanished | Expected — `environment` resets submodules; encode changes as patches or move them into `meta-neopios` |
| Weird fetcher failures after repinning | Ensure the git link *and* the `SUBMODULES` list in `environment` point at the same revision |
