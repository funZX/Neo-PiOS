# AGENTS.md

## What this repo is

Neo-PiOS: a custom embedded Linux distro built with Yocto/OpenEmbedded (**wrynose 6.0**) targeting `raspberrypi4-64`, using **OpenRC** init (not systemd). All upstream metadata lives in git submodules under `layers/`. First-party code is only:

- `layers/meta-neopios/` — distro config (`conf/distro/neopios.conf`) + display images (`recipes-core/images/neopios-weston-image.bb` Wayland-only, `neopios-xwayland-image.bb` hybrid XWayland [canonical, git mv successor to `core-image-neopios.bb`], `neopios-x11-image.bb` X11-only) + shared payload (`recipes-core/images/include/neopios-common.inc`)
- `environment`, `docker/`, `build/` — build orchestration

Choose a display variant per build: `neopios-weston-image` (Wayland-only, `REQUIRED_DISTRO_FEATURES = "wayland"`), `neopios-xwayland-image` (hybrid, `wayland x11` + `weston-xwayland`), or `neopios-x11-image` (X11-only, `x11` + `packagegroup-core-x11-base`). `DISTRO_FEATURES` stays `opengl wayland x11` globally; enforcement is per-image via `REQUIRED_DISTRO_FEATURES` + `features_check`.

## Build workflow (podman container, never the host)

Builds run in a rootless **podman** container (image `linux-build-wrynose`, auto-built from `docker/Dockerfile` on first use — the dir is named `docker/` but it's podman). From the **repo root** (`PODMAN_WORKDIR` is captured at source time):

```sh
source environment   # builds podman image if missing; inits + resets submodules
bb.shell             # interactive shell in the container (repo mounted at ~/workspace)
# inside the container:
source layers/openembedded-core/oe-init-build-env build
bitbake neopios-weston-image      # Wayland-only
bitbake neopios-xwayland-image    # hybrid XWayland (canonical)
bitbake neopios-x11-image         # X11-only
```

Host-side helper from `environment`: `bb.shell` (interactive shell in the container).

Pick the image that matches the desired display stack; `neopios-xwayland-image` is the replacement for the removed `core-image-neopios`.

## Critical gotchas

- **Sourcing `environment` is destructive**: `reset_submodules` runs `git clean -fxd` + `git reset --hard` + checkout of pinned SHAs in every submodule, then re-applies layer patches. Hand-edits inside `layers/<submodule>/` will be wiped. Put changes in `meta-neopios` or encode them as a patch.
- **Layer patch convention**: `layers/<layer-name>.patch` at repo root (e.g. `layers/meta-openrc.patch`) is applied automatically after pinned checkout.
- **Submodule pins live in two places**: the git link AND the hardcoded `SUBMODULES` list in `environment`. Update both when repinning.
- Builds are long, disk- and RAM-hungry. If OOM: `cd build && sudo ./mkswap.sh` adds a 16 GiB swapfile.

## Bitbake aliases

Source `build/env-neopios.sh` inside the OE environment:

- `bb` = bitbake; `bb.clean` = clean/cleansstate
- `bb.edit <recipe>` / `bb.apply` / `bb.close` = devtool modify / update-recipe / reset. `bb.apply` writes changes back into `../layers/meta-neopios` — recipe customization belongs there.
- `bb.linux` = kernel tasks, `bb.sysroot` = target sysroot, `bb.sdk` = populate_sdk

## Key configuration

- `MACHINE = "raspberrypi4-64"`, `DISTRO = "neopios"`, primary targets: `neopios-weston-image` (Wayland-only, `weston` + `REQUIRED_DISTRO_FEATURES wayland`), `neopios-xwayland-image` (hybrid, `x11 weston` + `weston-xwayland`, `REQUIRED wayland x11` — canonical successor to `core-image-neopios`), `neopios-x11-image` (X11-only, `x11 x11-base` + `packagegroup-core-x11-base`, `REQUIRED x11`); legacy `core-image-neopios` has been removed
- Kernel provider: `linux-raspberrypi`; init: `INIT_MANAGER = "openrc"` via meta-openrc
- `build/conf/local.conf` and `bblayers.conf` are tracked and pre-configured — don't regenerate them
- wrynose uses `DISTRO_FEATURES_OPTED_OUT` / `DISTRO_FEATURES_DEFAULTS` (the old `*_BACKFILL_CONSIDERED` vars are obsolete); neopios opts out of `ptest vulkan multiarch`
- Global `DISTRO_FEATURES` appends `opengl wayland x11` (unchanged); per-image `REQUIRED_DISTRO_FEATURES` + `features_check` selects the display stack without forking the distro

## Verification

No test suite, linter, or CI exists. Verification = a successful bitbake of the affected target. For display images, verify parsing and (optionally) full builds per variant:

```sh
bitbake -e neopios-weston-image   # should show wayland without x11
bitbake -e neopios-xwayland-image # should show wayland and x11
bitbake -e neopios-x11-image      # should show x11 without wayland
# optional full builds (resource-heavy):
bitbake neopios-weston-image
bitbake neopios-xwayland-image
bitbake neopios-x11-image
```

Build artifacts (`build/tmp*`, `sstate-cache`, `downloads/`) are gitignored. Choose the image that matches the change under test; `neopios-xwayland-image` is the default for hybrid display testing.
