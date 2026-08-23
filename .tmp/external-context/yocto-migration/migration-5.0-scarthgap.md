---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: migration 4.3 nanbield -> 5.0 scarthgap
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/migration-guides/migration-5.0.html
release: 5.0 scarthgap (LTS)
project_context: custom distro w/ openrc, raspberrypi4-64, weston/x11/wayland, podman/buildah, ipk, docker host ubuntu:22.04
---

# Migration notes 5.0 (scarthgap) — filtered for this project

## Host requirements (Docker image ubuntu:22.04)
- Newly supported distros: Rocky 9.
- No longer supported hosts: Fedora 37, Ubuntu 22.10, OpenSUSE Leap 15.3.
- **Ubuntu 22.04 remains a supported build host** in scarthgap → current Docker base OK for this step.

## ipk packaging changes (HIGH RELEVANCE — project uses ipk/opkg)
- opkg/ipk now uses **zstd compression instead of xz**. `.ipk` packages built with 5.0 require an opkg built with zstd enabled at build time; packages will NOT be usable on older systems whose opkg lacks zstd.
- Opkg's internal dependency solver is deprecated (warning if selected). Default is external `libsolv`. If `libsolv` was removed from opkg PACKAGECONFIG to use the internal solver, plan switch back to libsolv.

## Removed variables
- `SERIAL_CONSOLES_CHECK` removed → use `SERIAL_CONSOLES` (all consoles listed there are checked before getty starts). **Check custom distro / RPi machine config for SERIAL_CONSOLES_CHECK usage.**
- `PYTHON_PN` removed (Python 3 only now) — audit custom recipes/layers using `${PYTHON_PN}`.
- `CVE_CHECK_IGNORE` deprecated → replace with `CVE_STATUS`. **Relevant if meta-security or custom config sets CVE_CHECK_IGNORE.**
- Also removed: `DEPLOY_DIR_TAR`, `oldincludedir`, `USE_L10N`, `CVE_SOCKET_TIMEOUT`.

## Kernel
- OLDEST_KERNEL still "5.15" (older target kernels unsupported out of the box).
- `linux-yocto` 6.1 removed (6.6 provided). Project uses `linux-raspberrypi` from meta-raspberrypi — verify that layer's kernel version bump instead.

## systemd (low relevance — project uses openrc)
- nss-resolve plugin supported via PACKAGECONFIG; required (with `resolved`) by `systemd-resolved` feature.

## Misc relevant
- Warning shown if `virtual/` prefix used in runtime contexts (RDEPENDS/RPROVIDES) — audit meta-virtualization/custom recipes.
- ptest runtime testing now fails if no test results returned by a ptest.
- `cve-check` class no longer warns on remote patches (note only); CVE refs in remote patch filenames still picked up.
- PE/PR dropped from `-f{file,macro,debug}-prefix-map`; new `TARGET_DBGSRC_DIR` variable available.
- `bitbake-whatchanged` script removed; `sstate-cache-management.sh` replaced by Python version; `bmap-tools` recipe renamed to `bmaptool`.
- Poky DISTRO now writes a warning into /etc/motd — not applicable since project has a custom distro.
