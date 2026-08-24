# Neo-PiOS

A custom embedded Linux distribution built with [Yocto/OpenEmbedded](https://www.yoctoproject.org/) (**wrynose 6.0**) for the Raspberry Pi 4 (64-bit), using **OpenRC** instead of systemd.

## Features

- Machine `raspberrypi4-64`, distro `neopios`, kernel `linux-raspberrypi`
- OpenRC init (`INIT_MANAGER = "openrc"` via `meta-openrc`)
- Two Wayland display variants sharing a common payload (minimal `include/neopios-common.inc` + hobby/edu/thin `include/neopios-extra.inc` with `NEOPIOS_EXTRA=1` by default):
  - `neopios-weston-image` — Wayland-only Weston compositor (reference, `libweston`)
  - `neopios-labwc-image` — Wayland-only Labwc compositor (wlroots-based, Openbox-like stacking)
- openssh and essential networking/tooling out of the box (minimal via `neopios-common.inc` + `package-management`/`opkg`, hobby/edu/thin via `neopios-extra.inc`, dev adds debug via `neopios-common-dev.inc`)
- Reproducible builds: upstream layers pinned via git submodules + patches
- Fully containerized builds via rootless podman — no host pollution

## Repository layout

| Path | Purpose |
|------|---------|
| `layers/meta-neopios/` | First-party layer: distro config (`conf/distro/neopios.conf`) + display images (`recipes-core/images/neopios-weston-image.bb`, `neopios-labwc-image.bb`) + shared payload (minimal `recipes-core/images/include/neopios-common.inc`, hobby/edu/thin `recipes-core/images/include/neopios-extra.inc` (`NEOPIOS_EXTRA=1`), dev `recipes-core/images/include/neopios-common-dev.inc`) + base boot include (`recipes-core/images/include/core-image-neopios.inc`) |
| `layers/meta-neopios/recipes-core/images/neopios-weston-image.bb` | Wayland-only Weston image (IMAGE_FEATURES `weston`, REQUIRED_DISTRO_FEATURES `wayland`) |
| `layers/meta-neopios/recipes-core/images/neopios-labwc-image.bb` | Wayland-only Labwc image (wlroots-based, REQUIRED_DISTRO_FEATURES `wayland`) |
| `layers/meta-neopios/recipes-core/images/include/neopios-common.inc` | Minimal `EXTRA_IMAGE_FEATURES` (`package-management`) + `IMAGE_INSTALL` for both images |
| `layers/meta-neopios/recipes-core/images/include/neopios-extra.inc` | Hobby/DIY + Education + Thin Client extra (`NEOPIOS_EXTRA=1` default, `0` for minimal) — `python3`/`pip`/`pyserial`, `i2c-tools`/`libgpiod`, `git`/`vim`/`geany`, `freerdp`, `pulseaudio`, `wpa-supplicant`/`iw`/`rfkill` + `linux-firmware-bcm43455`/`brcmfmac` (Pi 4 Wi-Fi, `DISTRO_FEATURES wifi`) |
| `layers/meta-neopios/recipes-graphics/wayland/weston_%.bbappend` | Enables Weston's VNC backend (`PACKAGECONFIG:append = " vnc"` via `neatvnc`/`libpam`, `weston --backend=vnc` / `weston.ini [core] backend=vnc-backend.so`) for weston |
| `layers/meta-neopios/recipes-core/images/include/neopios-common-dev.inc` | Dev add-on extending minimal with `tools-debug`/`tools-profile`, `post-install-logging`, empty-password and extra tools (`gdb`, `net-tools`, `iptraf`) |
| `layers/bitbake` | BitBake build tool |
| `layers/openembedded-core` | OE-Core: base metadata, classes, and the `oe-init-build-env` entrypoint |
| `layers/meta-openembedded` | Extra recipes (meta-oe, meta-perl, meta-python, meta-networking, meta-filesystems sublayers) |
| `layers/meta-openrc` | OpenRC init support (tracking `master`, wrynose-compatible upstream) |
| `layers/meta-raspberrypi` | Raspberry Pi BSP (machine configs, boot firmware) |
| `layers/meta-wayland` | Wayland compositors (wlroots 0.19, labwc 0.9.7 on wrynose) |
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
bitbake neopios-weston-image    # Wayland-only Weston
bitbake neopios-labwc-image     # Wayland-only Labwc (wlroots)
```

Notes:

- The first build downloads all sources and compiles everything from scratch — expect hours. Subsequent builds reuse `sstate-cache/` and `downloads/`, so they are much faster.
- `PODMAN_WORKDIR` is captured when `environment` is sourced, so always source it from the repo root.
- Images land in `build/tmp/deploy/images/raspberrypi4-64/`.
- `DISTRO_FEATURES` stays `opengl wayland pam wifi` globally (wrynose 6.0, `INIT_MANAGER = "openrc"`, `MACHINE = "raspberrypi4-64"`); per-image enforcement is via `REQUIRED_DISTRO_FEATURES` + `features_check` (see table below).
- `NEOPIOS_EXTRA` (`neopios-extra.inc`) defaults to `1` (hobby/edu/thin extra on); set `NEOPIOS_EXTRA = "0"` in `build/conf/local.conf` or `NEOPIOS_EXTRA=0 bitbake <image>` for minimal.


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
| `DISTRO_FEATURES` | + `opengl wayland pam wifi` | `neopios.conf` (global) |
| Opted-out features | `ptest vulkan multiarch` | `neopios.conf` (wrynose mechanism) |

### Display images — comparison

Two Wayland variants share `include/neopios-common.inc` (minimal) + `include/neopios-extra.inc` (`NEOPIOS_EXTRA=1` hobby/edu/thin extra) and `include/core-image-neopios.inc` (boot + `LICENSE`). Each inherits `core-image` + `features_check` and enforces `REQUIRED_DISTRO_FEATURES = "wayland"`; global `DISTRO_FEATURES` stays `opengl wayland pam wifi`.

| Image | `IMAGE_FEATURES` | `REQUIRED_DISTRO_FEATURES` | Display stack | Packages (on top of common) | Use case |
|-------|------------------|-----------------------------|---------------|------------------------------|----------|
| `neopios-weston-image` | `weston` | `wayland` | Wayland-only, Weston (`libweston`) | `weston` | Pure Wayland kiosk / Weston reference |
| `neopios-labwc-image` | (none) | `wayland` | Wayland-only, Labwc (wlroots 0.19) | `labwc` | Wayland stacking (Openbox-like) via wlroots |

Common payload:

- Minimal (`neopios-common.inc` for both): `EXTRA_IMAGE_FEATURES = "package-management"` (`opkg`); `IMAGE_INSTALL` = `bash`, `coreutils`, `iproute2`, `iputils`, `dhcpcd`, `kmod`, `procps`, `psmisc`, `util-linux`, `openssh`, `sudo`, `tzdata-core`
- Hobby/DIY + Education + Thin Client (`neopios-extra.inc`, `NEOPIOS_EXTRA=1` default, `0` for minimal): `python3`/`python3-pip`/`python3-pyserial`, `i2c-tools`/`libgpiod`/`libgpiod-tools`, `git`/`vim`/`nano`/`htop`/`usbutils`/`geany`/`man`/`bash-completion`, `freerdp`, `pulseaudio`/`alsa-utils`, `wpa-supplicant`/`iw`/`rfkill` + `linux-firmware-bcm43455`/`kernel-module-brcmfmac` (Pi 4 Wi-Fi, `wifi` in `DISTRO_FEATURES`)
- Dev add-on (`neopios-common-dev.inc` extends minimal): adds `tools-debug`, `tools-profile`, `post-install-logging`, `allow-empty-password`/`allow-root-login`/`empty-root-password` and `gdb`, `iproute2-tc`/`ss`, `net-tools`, `iptraf`

Variant notes:

- `neopios-weston-image.bb`: `IMAGE_FEATURES:append = " weston"`, `IMAGE_INSTALL:append = " weston"`, `REQUIRED_DISTRO_FEATURES = "wayland"` — strict Wayland via libweston, VNC via `weston_%.bbappend` (`neatvnc`).
- `neopios-labwc-image.bb`: `IMAGE_INSTALL:append = " labwc"`, `REQUIRED_DISTRO_FEATURES = "wayland"` — wlroots 0.19, Openbox-like stacking, no X11.

## Booting the image

1. Flash the generated image file from `build/tmp/deploy/images/raspberrypi4-64/` onto an SD card (e.g. with `dd`, Raspberry Pi Imager, or `balenaEtcher`).
2. Insert the card into the Raspberry Pi 4 and power it on.
3. The system boots into the selected Wayland compositor (Weston for `neopios-weston-image`, Labwc for `neopios-labwc-image`) with OpenRC as the init system.
4. SSH access is available over the network — root login with an empty password is enabled by default (development convenience; tighten before any real deployment).

### First Boot — SSH Login

After the image boots, you can log in via SSH using one of these methods:

**By hostname (mDNS/ZeroConf, recommended):**
```sh
ssh root@neopios.local
```
> Requires `avahi-daemon` (included) and mDNS support on your client (Linux/macOS/Windows with Bonjour).

**By IP address:**
```sh
# Find the Pi's IP (check your router's DHCP lease table, or use nmap):
nmap -sn 192.168.1.0/24 | grep neopios

# Then connect:
ssh root@<IP_ADDRESS>
```

**Credentials:**
- **User:** `root`
- **Password:** *(empty — just press Enter)*

> ⚠️ **Security:** The default empty root password is for development convenience. **Change it immediately** after first login:
> ```sh
> passwd root
> ```

**If SSH fails:**
- Ensure the Pi is on the same network (wired Ethernet recommended for first boot)
- Check that `avahi-daemon` is running: `systemctl status avahi-daemon` (or `/etc/init.d/avahi-daemon status` on OpenRC)
- Verify SSH service: `/etc/init.d/sshd status`
- For Wi-Fi: configure `wpa_supplicant.conf` first (see [Network Configuration](#network-configuration))

### Flashing on Windows (Raspberry Pi Imager)

The easiest way to flash the `.wic.bz2` image on Windows is using the official **Raspberry Pi Imager**:

1. **Download Raspberry Pi Imager** from [raspberrypi.com/software](https://www.raspberrypi.com/software/) and install it.
2. **Insert your SD card** (minimum 4 GB, 8 GB+ recommended) into your Windows PC.
3. **Open Raspberry Pi Imager**.
4. Click **Choose OS** → scroll to the bottom → select **Use custom**.
5. In the file dialog, navigate to your build output directory and select the `.wic.bz2` file:
   ```
   build/tmp/deploy/images/raspberrypi4-64/neopios-weston-image-raspberrypi4-64-<date>.26.08.wic.bz2
   ```
   (or `neopios-labwc-image-...wic.bz2` for the Labwc variant)
6. Click **Choose Storage** → select your SD card.
   ⚠️ **Warning:** This will erase all data on the selected card.
7. Click **Write** and wait for the process to complete (writing + verification).
8. When finished, safely eject the SD card, insert it into your Raspberry Pi 4, and power it on.

> **Note:** Raspberry Pi Imager natively supports `.wic.bz2` (and `.wic.gz`, `.wic.xz`) — no manual decompression needed.

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
