---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: migration 5.0 scarthgap -> 5.1 styhead
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/migration-guides/migration-5.1.html
release: 5.1 styhead
project_context: custom distro w/ openrc, raspberrypi4-64, weston/x11/wayland, podman/buildah, ipk, docker host ubuntu:22.04
---

# Migration notes 5.1 (styhead) — filtered for this project

## WORKDIR changes (BREAKING — affects all custom recipes & bbappends)
- `S = "${WORKDIR}"` **no longer supported → error**. Change to:
  ```
  S = "${UNPACKDIR}"
  ```
- Files from SRC_URI now unpack into `WORKDIR/sources-unpack/` instead of `WORKDIR/`.
- Audit ALL WORKDIR references in recipes: references to SRC_URI files in do_configure/do_compile/do_install and LIC_FILES_CHKSUM likely need `UNPACKDIR`. Common patterns:
  - `${WORKDIR}/${BP}` → `${S}`
  - `../` references in LIC_FILES_CHKSUM → UNPACKDIR
  - sed-command WORKDIR references usually left as-is
- Recipes with only `file://` SRC_URI entries may need explicit `S = "${UNPACKDIR}"` added (do_unpack_qa triggers when S isn't created).
- Building C files from UNPACKDIR without setting S to point at it does NOT work (debug prefix mapping).
- `devtool`/`recipetool`: support for `S = WORKDIR` and `oe-local-files` removed.
- `S = "${WORKDIR}/git"` and deeper subpaths still work as expected at this release (subdir moved back out of sources-unpack), but full removal comes in 5.3.

## Host requirements (Docker image)
- Newly supported: Ubuntu 24.10, Fedora 40, openSUSE Leap 15.5/15.6. Dropped: Ubuntu 23.04. Ubuntu 22.04 still fine.

## Go language changes (RELEVANT — podman/buildah are Go-based via meta-virtualization)
- go class dropped its custom do_unpack: go recipes must add `destsuffix=${GO_SRCURI_DESTSUFFIX}` to git SRC_URI entries.
- Go modules no longer compiled with `--linkmode=external`.

## Removed variables
- `TCLIBCAPPEND` removed (TMPDIR sharing across libc providers long supported).
- `VOLATILE_LOG_DIR` removed → use `FILESYSTEM_PERMS_TABLES` (default now includes `files/fs-perms-volatile-log.txt`; remove it to disable volatile log).
- `VOLATILE_TMP_DIR` removed → `FILESYSTEM_PERMS_TABLES` (`files/fs-perms-volatile-tmp.txt` default).

## QA / warnings promoted to errors (can break builds)
- Several `insane` class checks such as `buildpaths` promoted from warning to ERROR.
- ERROR_QA check `license-incompatible` renamed to `license-exception`.

## Recipe changes possibly touching dependency graph
- gobject-introspection: `giscanner` split into separate `gobject-introspection-tools` package.
- perf no longer uses libnewt TUI; openssl test suite only built with ptests.

## Removed recipes/classes (check third-party layer deps)
- liba52, libomxil, mpeg2dec, usbinit removed; libnewt, pytest-runner, python3-importlib-metadata, python3-pathlib2, python3-py, python3-rfc3986-validator, python3-toml, python3-tomli moved to meta-python/meta-oe.
- Class `siteconfig` removed.

## Other
- OLDEST_KERNEL still 5.15.
- systemd: new PACKAGECONFIG `bpf-framework` (precompiled eBPF for RestrictFileSystems/RestrictNetworkInterfaces).
