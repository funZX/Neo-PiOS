---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: migration 5.1 styhead -> 5.2 walnascar
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/migration-guides/migration-5.2.html
release: 5.2 walnascar
project_context: custom distro w/ openrc, raspberrypi4-64, weston/x11/wayland, podman/buildah, ipk, docker host ubuntu:22.04
---

# Migration notes 5.2 (walnascar) — filtered for this project

## debug-tweaks removed from IMAGE_FEATURES (HIGH RELEVANCE for dev images)
- `debug-tweaks` image feature REMOVED. Replace with explicit features:
  ```
  IMAGE_FEATURES += "allow-empty-password allow-root-login empty-root-password post-install-logging"
  ```
- ACTION: grep image recipes/config for `debug-tweaks` (common in weston dev images).

## Git fetcher: branch parameter now REQUIRED (BREAKING — all git SRC_URIs)
- `branch=` is mandatory in git:// SRC_URI entries; missing branch is now an ERROR (was warning).
- ACTION: audit ALL layers' recipes incl. meta-raspberrypi / meta-virtualization / meta-security versions chosen — they must already be walnascar-compatible branches; fix own recipes.

## Git fetcher: multiple revisions per URL removed
- `;branch=branchX,branchY;name=nameX,nameY` multi-rev syntax no longer supported → split into separate SRC_URI entries with distinct names/SRCREVs.

## BB_DANGLINGAPPENDS_WARNONLY removed (BREAKING for bbappends)
- Dangling .bbappend files are now hard errors; the "warn only" escape hatch is gone.
- ACTION: ensure every .bbappend in custom layers matches a recipe in the reconfigured layer set (especially when layer collections change across releases).

## Virtual toolchain provider renaming
- `virtual/${TARGET_PREFIX}gcc` → `virtual/cross-cc`, `virtual/${TARGET_PREFIX}binutils` → `virtual/cross-binutils`, `virtual/${TARGET_PREFIX}compilerlibs` → `virtual/compilerlibs`, nativesdk equivalents use `virtual/nativesdk-cross-*`. PREFERRED_PROVIDER_virtual/... assignments must be renamed likewise. Audit custom distro/machine config for old forms.

## autotools class changes
- Recipes inheriting autotools* MUST have a configure script (location in new CONFIGURE_SCRIPT var); do_configure fails otherwise.
- ACLOCALDIR/ACLOCALEXTRAPATH no longer used; m4 dirs not auto-found → `EXTRA_AUTORECONF += "-I path/to/m4"` if macros missing.

## UBOOT_ENTRYPOINT format
- Must now include leading `0x` (e.g. `UBOOT_ENTRYPOINT ?= "0x20008000"`). Check machine configs (meta-raspberrypi handles its own; verify any custom u-boot config).

## systemd changes (context for openrc distro)
- split-usr/unmerged-usr support removed (systemd 255); usrmerge PACKAGECONFIG implied.
- systemd.bbclass: service files referenced via `Also=` no longer auto-added to FILES — must be explicitly packaged.
- journald persistent logging via Storage=persistent + create-log-dirs restored.
- Without `pni-names` DISTRO_FEATURE, predictable interface names now actually disabled.

## Multiconfig
- BB_CURRENT_MC is "" (empty) instead of "default" for default multiconfig — update any logic comparing to "default".

## Removed variables/features/classes/recipes
- Variables removed: PACKAGE_SNAP_LIB_SYMLINKS, SETUPTOOLS_INSTALL_ARGS, **BB_DANGLINGAPPENDS_WARNONLY**.
- Feature removed: `ld-is-gold` DISTRO_FEATURE.
- Class removed: migrate_localcount.bbclass.
- Recipes removed/moved: liburi-perl→meta-perl, python3-isodate→meta-python, python3-iniparse removed, blktool removed, cargo-c-native→cargo-c target recipe, libnss-mdns renamed avahi-libnss-mdns.
- valgrind ptest support removed (glibc 2.41 regressions).

## Misc
- ZSTD_COMPRESSION_LEVEL is now plain integer (`3`, not `-3`).
- UBOOT_ENV no longer processed by kernel-fitimage.bbclass → new FIT_UBOOT_ENV variable for FIT images.
- devtool ide-sdk removed from eSDK.
- Host distros: Fedora 41 & CentOS Stream 9 added; CentOS Stream 8, Fedora 38, openSUSE Leap 15.4, **Ubuntu 20.04** dropped. Ubuntu 22.04 unaffected.
