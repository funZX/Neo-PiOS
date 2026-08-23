# Neo-PiOS

A custom embedded Linux distribution built with [Yocto/OpenEmbedded](https://www.yoctoproject.org/) (**wrynose 6.0**) for the Raspberry Pi 4 (64-bit), using **OpenRC** instead of systemd.

## Features

- Machine `raspberrypi4-64`, distro `neopios`, kernel `linux-raspberrypi`
- OpenRC init (`INIT_MANAGER = "openrc"` via `meta-openrc`)
- Weston / X11 graphical session (`weston-xwayland`)
- openssh, python3, and common networking/debug tooling out of the box
- Reproducible builds: upstream layers pinned via git submodules + patches
- Fully containerized builds via rootless podman — no host pollution

## Repository layout

| Path | Purpose |
|------|---------|
| `layers/meta-neopios/` | First-party layer: distro config (`conf/distro/neopios.conf`) + image recipe (`core-image-neopios.bb`) |
| `layers/bitbake` | BitBake build tool |
| `layers/openembedded-core` | OE-Core: base metadata, classes, and the `oe-init-build-env` entrypoint |
| `layers/meta-openembedded` | Extra recipes (meta-oe, meta-perl, meta-python, meta-networking, meta-filesystems sublayers) |
| `layers/meta-openrc` | OpenRC init support (patched for wrynose via `layers/meta-openrc.patch`) |
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

# inside the container
source layers/openembedded-core/oe-init-build-env build
bitbake core-image-neopios
```

Notes:

- The first build downloads all sources and compiles everything from scratch — expect hours. Subsequent builds reuse `sstate-cache/` and `downloads/`, so they are much faster.
- `PODMAN_WORKDIR` is captured when `environment` is sourced, so always source it from the repo root.
- Images land in `build/tmp/deploy/images/raspberrypi4-64/`.

### Host-side helper (after `source environment`)

| Command | Description |
|---------|-------------|
| `bb.shell` | Interactive shell in the container |

### BitBake aliases (source `build/env-neopios.sh` inside the OE environment)

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
bitbake core-image-weston      # weston without the neopios extras
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
| `DISTRO_FEATURES` | + `opengl wayland x11` | `neopios.conf` |
| Opted-out features | `ptest vulkan multiarch` | `neopios.conf` (wrynose mechanism) |

Image content (`core-image-neopios`):

- Graphical: `x11` + `weston` image features, `weston-xwayland`
- Networking: `iproute2`, `dhcpcd`, `ifplugd`, `resolvconf`, `net-tools`, `iputils`
- System/tools: `openssh`, `sudo`, `python3`, `gdb`, `kmod`, `util-linux`, `procps`, `psmisc`, `icu`, `tzdata`
- Debug conveniences enabled: root login with empty password (`allow-empty-password allow-root-login empty-root-password`), `tools-debug`, `tools-profile`

## Booting the image

1. Flash the generated image file from `build/tmp/deploy/images/raspberrypi4-64/` onto an SD card (e.g. with `dd`, Raspberry Pi Imager, or `balenaEtcher`).
2. Insert the card into the Raspberry Pi 4 and power it on.
3. The system boots into Weston (graphical session) with OpenRC as the init system.
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
| Stale/stopped containers piling up | `bb.purge` |
| Layer changes vanished | Expected — `environment` resets submodules; encode changes as patches or move them into `meta-neopios` |
| Weird fetcher failures after repinning | Ensure the git link *and* the `SUBMODULES` list in `environment` point at the same revision |
