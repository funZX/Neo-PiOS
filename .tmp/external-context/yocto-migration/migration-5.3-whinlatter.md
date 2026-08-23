---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: migration 5.2 walnascar -> 5.3 whinlatter
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/migration-guides/migration-5.3.html
release: 5.3 whinlatter
project_context: custom distro w/ openrc, raspberrypi4-64, weston/x11/wayland, podman/buildah, ipk, docker host ubuntu:22.04
---

# Migration notes 5.3 (whinlatter) — filtered for this project

## Poky repo master branch no longer updated (workflow change)
- Clone bitbake / openembedded-core / meta-yocto individually, or use new `bitbake-setup` tool. Old poky-repo checkout workflow ends after release EOLs.
- ACTION: update CI/build-container scripts that clone poky.git master or pin poky tarballs.

## WORKDIR changes round 2 (BREAKING)
- `S = "${WORKDIR}/something"` no longer supported → error. Use `S = "${UNPACKDIR}/something"`.
- Git fetcher now unpacks into `BB_GIT_DEFAULT_DESTSUFFIX` (= `${BP}` per OE-Core bitbake.conf) instead of `git/`.
- `S = "${WORKDIR}/git"` and `S = "${UNPACKDIR}/git"` should be REMOVED entirely (matches new default S). Subdir case: `S = "${UNPACKDIR}/${BP}/something"`.
- Mass-edit sed commands provided in guide:
  ```
  sed -i "/^S = \"\${WORKDIR}\/git\"/d" `find . -name '*.bb' -o -name '*.inc' -o -name '*.bbclass'`
  sed -i "s/^S = \"\${WORKDIR}\//S = \"\${UNPACKDIR}\//g" `find . -name '*.bb' -o -name '*.inc' -o -name '*.bbclass'`
  ```
- Hardcoded `git` paths elsewhere: in SRC_URI use `destsuffix=${BB_GIT_DEFAULT_DESTSUFFIX}` context; elsewhere replace with ${BP}.
- Git fetcher `tag=` param now verified against SRCREV — recommended for tag-based repos.

## Coding style warning (new noise)
- Warning for missing whitespace around `=` assignments (`FOO="bar"` etc.) — clean up custom layers.

## xserver-xorg / xwayland split (DIRECTLY RELEVANT — weston-xwayland image)
- `${PN}-xwayland` sub-package REMOVED from xserver-xorg; **xwayland is now its own recipe/package**.
- ACTION: update image recipes: replace `xserver-xorg-xwayland` style package refs with the new `xwayland` package; verify weston-xwayland feature wiring in chosen release's meta-raspberrypi/image config.

## mesa PACKAGECONFIG removals (RELEVANT — RPi4 graphics)
- mesa PACKAGECONFIG entries removed: `kmsro`, `osmesa`, `xa`. If custom config appends these, remove them; verify vc4/v3d driver config still intact for raspberrypi4-64.

## kernel-fitimage.bbclass removed (CONDITIONAL — only if FIT images used)
- Replaced by `kernel-fit-image` class; `fitImage` no longer valid KERNEL_IMAGETYPE. New flow:
  - `KERNEL_CLASSES += "kernel-fit-extra-artifacts"`
  - Build dedicated recipe e.g. `bitbake linux-yocto-fitimage` (custom kernel → create matching fitimage recipe)
  - If FIT replaces kernel in rootfs: RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = "", MACHINE_ESSENTIAL_EXTRA_RDEPENDS += "<kernel>-fitimage", KERNEL_DEPLOY_DEPEND = "<kernel>-fitimage:do_deploy"
- RPi4 typically boots without FIT; only act if project uses FIT images.
- Also removed: icecc.bbclass (distributed compile caching).

## *FLAGS native/nativesdk behavior change (SUBTLE BREAKAGE RISK)
- CPPFLAGS/CFLAGS/CXXFLAGS/LDFLAGS hard assignments removed from native/nativesdk classes. Consequence: existing `CFLAGS += "..."` in recipes now applies to target AND native AND nativesdk contexts (previously target-only).
- ACTION: audit custom recipes appending to *FLAGS; switch target-only intent to `TARGET_CFLAGS += "..."` etc.

## linux-firmware unlicensed firmware removal (RELEVANT — RPi wifi/bluetooth firmware)
- linux-firmware now EXCLUDES firmware without license info by default (internal REMOVE_UNLICENSED list, overridable).
- ACTION: boot-test RPi4 wifi/bt; if firmware missing, override REMOVE_UNLICENSED or add specific firmware packages.

## systemd predictable interface names policy change (networking behavior)
- With `pni-names` DISTRO_FEATURE enabled, upstream default NamePolicy applies instead of forced "mac" policy. Interface naming on eth0/wlan0 may change; restore mac policy via NamePolicy/AlternativeNamesPolicy settings if needed.

## Wic plugin filenames
- Dashes no longer allowed in wic plugin filenames → underscores (e.g. bootimg-partition.py → bootimg_partition.py). Upstream WKS auto-converts; rename CUSTOM plugins and update custom .wks --source references.

## Removed variables/recipes/PACKAGECONFIG/classes
- Variables: BUILDHISTORY_RESET, GPE_MIRROR.
- Recipes: libsoup-2.4 (→meta-oe), glibc-y2038-tests, python3-ndg-httpsclient, xf86-input-mouse, xf86-input-vmmouse, babeltrace (use babeltrace2), cwautomacros, rust-llvm.
- PACKAGECONFIG removed: dropbear enable-x11-forwarding (renamed x11), libxml2 ipv6, squashfs-tools reproducible, mesa kmsro/osmesa/xa, systemd dbus.
- Classes: kernel-fitimage.bbclass, icecc.bbclass.
- Misc: gdk-pixbuf GDK_PIXBUF_LOADERS variable dropped (use PACKAGECONFIG); util-linux-fcntl-lock package removed (flock --fcntl); nghttp2-proxy package removed; distro_alias.inc removed.

## Host requirements
- Added: Debian 13, Fedora 42, Ubuntu 25.04/25.10. Ubuntu 22.04 unaffected at this step.
