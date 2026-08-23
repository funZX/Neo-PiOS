# AGENTS.md

## What this repo is

Neo-PiOS: a custom embedded Linux distro built with Yocto/OpenEmbedded (**wrynose 6.0**) targeting `raspberrypi4-64`, using **OpenRC** init (not systemd). All upstream metadata lives in git submodules under `layers/`. First-party code is only:

- `layers/meta-neopios/` — distro config (`conf/distro/neopios.conf`) + main image recipe (`recipes-core/images/core-image-neopios.bb`)
- `environment`, `docker/`, `build/` — build orchestration

## Build workflow (podman container, never the host)

Builds run in a rootless **podman** container (image `linux-build-wrynose`, auto-built from `docker/Dockerfile` on first use — the dir is named `docker/` but it's podman). From the **repo root** (`PODMAN_WORKDIR` is captured at source time):

```sh
source environment   # builds podman image if missing; inits + resets submodules
bb.shell             # interactive shell in the container (repo mounted at ~/workspace)
# inside the container:
source layers/openembedded-core/oe-init-build-env build
bitbake core-image-neopios
```

Host-side helpers from `environment`: `bb.run "<cmd>"`, `bb.exec <cmd>`, `bb.shell`, `bb.purge`.

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

- `MACHINE = "raspberrypi4-64"`, `DISTRO = "neopios"`, primary target: `core-image-neopios` (weston/x11, openssh, python3)
- Kernel provider: `linux-raspberrypi`; init: `INIT_MANAGER = "openrc"` via meta-openrc
- `build/conf/local.conf` and `bblayers.conf` are tracked and pre-configured — don't regenerate them
- wrynose uses `DISTRO_FEATURES_OPTED_OUT` / `DISTRO_FEATURES_DEFAULTS` (the old `*_BACKFILL_CONSIDERED` vars are obsolete); neopios opts out of `ptest vulkan multiarch`

## Verification

No test suite, linter, or CI exists. Verification = a successful bitbake of the affected target (`bitbake core-image-neopios` for image changes). Build artifacts (`build/tmp*`, `sstate-cache`, `downloads/`) are gitignored.
