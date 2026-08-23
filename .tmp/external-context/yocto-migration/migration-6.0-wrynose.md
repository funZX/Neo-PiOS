---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: migration 5.3 whinlatter -> 6.0 wrynose (LTS until April 2030)
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/migration-guides/migration-6.0.html
release: 6.0 wrynose (LTS)
project_context: custom distro w/ openrc, raspberrypi4-64, weston/x11/wayland, podman/buildah, ipk, docker host ubuntu:22.04
---

# Migration notes 6.0 (wrynose, LTS) — filtered for this project

## INIT_MANAGER default changed (DIRECTLY RELEVANT — openrc custom distro)
- Default `INIT_MANAGER` in OE-Core `defaultsetup.conf` changed from `none` to `systemd`. Affects users of default/nodistro setup; Poky still defaults SysVinit.
- Custom distro explicitly setting `INIT_MANAGER = "openrc"` overrides this — BUT verify meta-openrc layer still functions given the other init-related changes below.

## SysVinit compatibility in systemd dropped (CHECK openrc interplay)
- systemd + sysvinit DISTRO_FEATURES can no longer coexist (upstream systemd v260 dropping sysv compat). `systemd-compat-units` recipe removed; systemd `sysvinit` PACKAGECONFIG removed.
- ACTION: confirm custom openrc distro does not carry `sysvinit` in DISTRO_FEATURES alongside anything expecting systemd-sysv compatibility scripts.

## DISTRO_FEATURES / MACHINE_FEATURES mechanism overhaul (MAJOR for custom distro)
- Now OBSOLETE: `DISTRO_FEATURES_BACKFILL`, `DISTRO_FEATURES_BACKFILL_CONSIDERED`, `DISTRO_FEATURES_DEFAULT`, `MACHINE_FEATURES_BACKFILL`, `MACHINE_FEATURES_BACKFILL_CONSIDERED`.
- Replaced by: `DISTRO_FEATURES_DEFAULTS`, `DISTRO_FEATURES_OPTED_OUT`, `MACHINE_FEATURES_DEFAULTS`, `MACHINE_FEATURES_OPTED_OUT`.
- Key behavior change: if the custom distro assigns DISTRO_FEATURES WITHOUT using DISTRO_FEATURES_DEFAULT, default features are now added automatically → review and add unwanted ones to `DISTRO_FEATURES_OPTED_OUT`.
- New DISTRO_FEATURES enabled by default in bitbake.conf: `multiarch`, `opengl`, `ptest`, `vulkan`, `wayland`.
  - wayland/opengl now default-on (helps weston image) but review vulkan/ptest/multiarch for unwanted build scope on RPi4.

## Configuration templates / templateconf (LCONF_VERSION/CONF_VERSION area)
- `meta-poky/conf/templates/default` templates REMOVED; single source now OE-Core `meta/conf/templates/default`.
- ACTION: if TEMPLATECONF points at meta-poky template, repoint to `meta/conf/templates/default` or ship own distro template dir. Expect LCONF_VERSION/CONF_VERSION bumps requiring local.conf/bblayers.conf updates during first build after upgrade.

## BitBake fetcher removals (check meta-virtualization deps)
- `npm` and `npmsw` fetchers DISABLED (security concerns, unmaintained).
- Bazaar, OSC, CVS fetchers dropped.
- Any recipe using these (some node/go tooling in layers) will fail.

## CVE checking rewritten (RELEVANT — replaces cve-check; interacts w/ meta-security workflow)
- `cve-check` class REMOVED → replaced by `sbom-cve-check`:
  - Remove `INHERIT += "cve-check"`, add `OE_FRAGMENTS += "core/yocto/sbom-cve-check"`
  - Outputs: `.sbom-cve-check.yocto.json` (same format as old cve-check JSON) and `.sbom-cve-check.spdx.json`
  - Old `.cve.txt` summary opt-in: `SBOM_CVE_CHECK_EXPORT_VARS:append = " SBOM_CVE_CHECK_EXPORT_SUMMARY"`
  - `CVE_CHECK_SHOW_WARNINGS` removed → `SBOM_CVE_CHECK_SHOW_WARNINGS`
- `cve-update-db-native`, `cve-update-nvd2-native` recipes removed.
- `CVE_PRODUCT` no longer requires escaping special chars (`webkitgtk\+` → `webkitgtk+`) — audit assignments.
- SPDX 2.2 support removed (`create-spdx-2.2` class gone) → SPDX 3 via `create-spdx` class.
- vex class output renamed: `.json` → `.vex.json`.

## Wic changes
- WKS files MUST move under `<layer>/files/wic/` else build FAILS with instructions in error message. Does not affect wic plugins (stay in scripts/lib/wic/plugins/source/).
- `WIC_SECTOR_SIZE` deprecated → `WIC_CREATE_EXTRA_ARGS += "--sector-size <n>"` (still works, prints DEPRECATED warning).

## U-Boot config flow (uboot-config class)
- Multi-config UBOOT_CONFIG flags split into `UBOOT_CONFIG_IMAGE_FSTYPES`, `UBOOT_CONFIG_BINARY`, `UBOOT_CONFIG_MAKE_OPTS`, `UBOOT_CONFIG_FRAGMENTS`. Legacy syntax still works but will be REMOVED next release — migrate proactively. Single-config (UBOOT_MACHINE/UBOOT_BINARY) unchanged.

## pkgconfig variables no longer auto-exported
- PKG_CONFIG_PATH etc. exports moved from bitbake.conf into pkgconfig class. Recipes using these vars without inheriting pkgconfig must add `inherit pkgconfig`.

## native/cross DEBUG_BUILD stripping
- DEBUG_BUILD = "1" now strips only TARGET binaries (native/cross no longer stripped). Revert via:
  ```
  INHIBIT_SYSROOT_STRIP:class-cross = "${@oe.utils.vartrue('DEBUG_BUILD', '1', '', d)}"
  INHIBIT_SYSROOT_STRIP:class-native = "${@oe.utils.vartrue('DEBUG_BUILD', '1', '', d)}"
  ```

## Removed recipes (dependency-graph impact)
- jquery, systemd-compat-units, gstreamer1.0-vaapi, **pkgconfig (replaced by pkgconf)**, python3-pyzstd, cve-update-db-native, cve-update-nvd2-native, python3-roman-numerals-py (renamed).

## Removed PACKAGECONFIG options
- mesa freedreno-fdperf; libcxx no-atomics; **systemd sysvinit**; gstreamer1.0-plugins-good soup2; webkitgtk soup2.

## Removed classes
- `oelint` removed.

## Host requirements (Docker image ubuntu:22.04)
- Newly supported: Fedora 43, openSUSE Leap 16.0, Ubuntu 26.04 LTS. Dropped: Fedora 39/40/41, openSUSE Leap 15.5.
- **Ubuntu 22.04 (LTS) remains officially supported** in 6.0.x (see host-system-requirements.md) → existing Docker base stays valid; verify installed package list matches current requirement list (zstd now required, python3-websockets caveat on 22.04).

## Misc
- meson: non-functional meson_do_qa_configure dropped.
- oe-init-build-env VSCode setup support dropped; `bitbake-setup` recommended.
