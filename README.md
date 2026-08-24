# Neo-PiOS

A custom embedded Linux distribution built with [Yocto/OpenEmbedded](https://www.yoctoproject.org/) (**wrynose 6.0**) for the Raspberry Pi 4 (64-bit), using **OpenRC** instead of systemd.

## Features

- Machine `raspberrypi4-64`, distro `neopios`, kernel `linux-raspberrypi`
- OpenRC init (`INIT_MANAGER = "openrc"` via `meta-openrc`)
- Two Wayland display variants sharing a common payload:
  - `neopios-weston-image` — Wayland-only Weston compositor
  - `neopios-labwc-image` — Wayland-only Labwc compositor
- openssh and essential networking/tooling out of the box
- Reproducible builds: upstream layers pinned via git submodules + patches
- Fully containerized builds via rootless podman — no host pollution

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
bb.shell                  # interactive shell in the container

# inside the container:
bitbake neopios-weston-image    # Wayland-only Weston
bitbake neopios-labwc-image     # Wayland-only Labwc
```

### Host-side helper

| Command | Description |
|---------|-------------|
| `bb.shell` | Interactive shell in the container |

### BitBake aliases

| Alias | Action |
|-------|--------|
| `bb` | bitbake |
| `bb.clean` | clean + cleansstate |
| `bb.edit <recipe>` | devtool modify |
| `bb.apply` | devtool update-recipe |
| `bb.close` | devtool reset |
| `bb.linux` | kernel tasks |
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
| `PACKAGE_CLASSES` | `package_ipk` | `build/conf/local.conf` |
| `DISTRO_FEATURES` | `opengl wayland pam wifi` | `meta-neopios/conf/distro/neopios.conf` |
| Opted-out features | `ptest vulkan multiarch x11 pcmcia 3g` | `meta-neopios/conf/distro/neopios.conf` |

### Display images — comparison

| Image | Display | Use case |
|-------|---------|----------|
| `neopios-weston-image` | Weston | Pure Wayland kiosk / Weston reference |
| `neopios-labwc-image` | Labwc | Wayland tiling window manager |

Both images share the same base packages. Set `NEOPIOS_EXTRA = "0"` in `build/conf/local.conf` for minimal factory image.

## Booting the image

1. Flash the generated image file from `build/tmp/deploy/images/raspberrypi4-64/` onto an SD card.
2. Insert the card into the Raspberry Pi 4 and power it on.
3. The system boots into the selected Wayland compositor with OpenRC as the init system.
4. SSH access is available over the network.

### First Boot — SSH Login

After the image boots, you can log in via SSH using the default user:

**By hostname:**
```sh
ssh pi@neopios.local
```

**By IP address:**
```sh
ssh pi@<IP_ADDRESS>
```

**Credentials:**
- **User:** `pi`
- **Password:** `Neo-PiOS`

> ⚠️ **Security:** Change the password immediately after first login:
> ```sh
> passwd
> ```

**If SSH fails:**
- Ensure the Pi is on the same network (wired Ethernet recommended for first boot)
- Check that `avahi-daemon` is running: `/etc/init.d/avahi-daemon status`
- Verify SSH service: `/etc/init.d/sshd status`
- For Wi-Fi: configure `wpa_supplicant.conf` first

### Flashing on Windows

The easiest way to flash the `.wic.bz2` image on Windows is using the official **Raspberry Pi Imager**:

1. **Download Raspberry Pi Imager** from [raspberrypi.com/software](https://www.raspberrypi.com/software/) and install it.
2. **Insert your SD card** into your Windows PC.
3. **Open Raspberry Pi Imager**.
4. Click **Choose OS** → **Use custom** → select the `.wic.bz2` file.
5. Click **Choose Storage** → select your SD card.
6. Click **Write**.

> **Note:** Raspberry Pi Imager natively supports `.wic.bz2` — no manual decompression needed.

## Customizing

- Distro/image changes belong in `layers/meta-neopios/` — that is the only layer this project owns.
- For quick recipe experiments inside the container, use the devtool workflow: `bb.edit <recipe>` → edit → `bb.apply` → `bb.close`.
- Changes to upstream layers must be encoded as `layers/<layer-name>.patch` — they are re-applied automatically after each submodule sync.
- ⚠️ Sourcing `environment` resets every submodule to its pinned revision. Hand-edits inside submodule layers are wiped.
