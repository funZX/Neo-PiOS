# AGENTS.md

## What this repo is

Neo-PiOS: a custom embedded Linux distro built with Yocto/OpenEmbedded (**wrynose 6.0**) targeting `raspberrypi4-64`, using **OpenRC** init (not systemd). All upstream metadata lives in git submodules under `layers/`. First-party code is only:

- `layers/meta-neopios/` — distro config (`conf/distro/neopios.conf`) + display images (`recipes-core/images/neopios-weston-image.bb` Wayland-only Weston, `neopios-labwc-image.bb` Wayland-only Labwc) + shared payload (minimal `recipes-core/images/include/neopios-common.inc`, hobby/edu/thin `recipes-core/images/include/neopios-extra.inc` with `NEOPIOS_EXTRA=1` default, dev `recipes-core/images/include/neopios-common-dev.inc`)
- `environment`, `docker/`, `build/` — build orchestration

Choose a display variant per build: `neopios-weston-image` (Wayland-only Weston, `REQUIRED_DISTRO_FEATURES = "wayland"`), or `neopios-labwc-image` (Wayland-only Labwc wlroots, `REQUIRED_DISTRO_FEATURES = "wayland"`). `DISTRO_FEATURES` stays `opengl wayland pam` globally; enforcement is per-image via `REQUIRED_DISTRO_FEATURES` + `features_check`. Minimal via `neopios-common.inc`; `NEOPIOS_EXTRA=1` (default) adds hobby/edu/thin (`neopios-extra.inc`), `0` for minimal factory image.

## Build workflow (podman container, never the host)

Builds run in a rootless **podman** container (image `linux-build-wrynose`, auto-built from `docker/Dockerfile` on first use — the dir is named `docker/` but it's podman). From the **repo root** (`PODMAN_WORKDIR` is captured at source time):

```sh
source environment   # builds podman image if missing; inits + resets submodules
bb.shell             # interactive shell in the container (repo mounted at ~/workspace)
# inside the container (oe-init auto-sourced via ~/.bash_profile on bb.shell -l login):
bitbake neopios-weston-image      # Wayland-only Weston
bitbake neopios-labwc-image       # Wayland-only Labwc (wlroots)
```

Host-side helper from `environment`: `bb.shell` (interactive shell in the container).

Pick the image that matches the desired display stack.

## Critical gotchas

- **Sourcing `environment` is destructive**: `reset_submodules` runs `git clean -fxd` + `git reset --hard` + checkout of pinned SHAs in every submodule, then re-applies layer patches. Hand-edits inside `layers/<submodule>/` will be wiped. Put changes in `meta-neopios` or encode them as a patch.
- **Layer patch convention**: `layers/<layer-name>.patch` at repo root is applied automatically after pinned checkout (if present; `meta-openrc` currently tracks upstream `master` without patch).
- **Submodule pins live in two places**: the git link AND the hardcoded `SUBMODULES` list in `environment`. Update both when repinning.
- Builds are long, disk- and RAM-hungry. If OOM: `cd build && sudo ./mkswap.sh` adds a 16 GiB swapfile.

## Bitbake aliases

Available at login via `~/.bash_profile` (PS1 + aliases inside `if [ -f ...oe-init-build-env ]` block — `oe-init-build-env` sourced automatically on `bb.shell -l` login shells):

- `bb` = bitbake; `bb.clean` = clean/cleansstate
- `bb.edit <recipe>` / `bb.apply` / `bb.close` = devtool modify / update-recipe / reset. `bb.apply` writes changes back into `../layers/meta-neopios` — recipe customization belongs there.
- `bb.linux` = kernel tasks, `bb.sysroot` = target sysroot, `bb.sdk` = populate_sdk

## Key configuration

- `MACHINE = "raspberrypi4-64"`, `DISTRO = "neopios"`, primary targets: `neopios-weston-image` (Wayland-only Weston, `REQUIRED_DISTRO_FEATURES = "wayland"`), `neopios-labwc-image` (Wayland-only Labwc wlroots, `REQUIRED_DISTRO_FEATURES = "wayland"`)
- Kernel provider: `linux-raspberrypi`; init: `INIT_MANAGER = "openrc"` via meta-openrc
- `build/conf/local.conf` and `bblayers.conf` are tracked and pre-configured — don't regenerate them
- wrynose uses `DISTRO_FEATURES_OPTED_OUT` / `DISTRO_FEATURES_DEFAULTS` (the old `*_BACKFILL_CONSIDERED` vars are obsolete); neopios opts out of `ptest vulkan multiarch`
- Global `DISTRO_FEATURES` appends `opengl wayland pam`; per-image `REQUIRED_DISTRO_FEATURES` + `features_check` selects the display stack without forking the distro

## Verification

No test suite, linter, or CI exists. Verification = a successful bitbake of the affected target. For display images, verify parsing and (optionally) full builds per variant:

```sh
bitbake -e neopios-weston-image   # should show wayland without x11
bitbake -e neopios-labwc-image    # should show wayland without x11
# optional full builds (resource-heavy):
bitbake neopios-weston-image
bitbake neopios-labwc-image
```

Build artifacts (`build/tmp*`, `sstate-cache`, `downloads/`) are gitignored. Choose the image that matches the change under test.
