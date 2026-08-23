---
source: Official Yocto Project docs
library: yocto
package: yocto-migration
topic: host system requirements for 6.0 wrynose (Docker build container sizing)
fetched: 2026-08-23T00:00:00Z
official_docs: https://docs.yoctoproject.org/ref-manual/system-requirements.html
release: 6.0 wrynose (current docs, 6.0.2)
project_context: docker-based build container on ubuntu:22.04
---

# System Requirements (Yocto 6.0-tip docs) — filtered for Docker build container

## Supported distributions (6.0.2 wrynose)
Ubuntu 22.04 (LTS) IS in the supported list, along with: AlmaLinux 8/9, CentOS Stream 9/10, Debian 11/12/13, Fedora 42/43, openSUSE Leap 15.6/16.0, Rocky 8/9, Ubuntu 24.04 LTS, 25.04, 25.10, 26.04 LTS.
→ The existing ubuntu:22.04-based build container remains a supported host for wrynose.

## Required tool versions (host)
- Git >= 1.8.3.1
- tar >= 1.28
- Python >= 3.9.0
- GNU make >= 4.0
- gcc >= 10.1
(ubuntu:22.04 ships gcc 11.x, python 3.10, make 4.3 — satisfies all minimums.)
If a container fails a minimum, use a `buildtools` / `buildtools-extended` tarball (scripts/install-buildtools --release yocto-6.0.2).

## Required packages (Ubuntu/Debian headless build)
```
build-essential chrpath cpio debianutils diffstat file gawk gcc git iputils-ping \
libacl1 libcrypt-dev locales python3 python3-git python3-jinja2 python3-pexpect \
python3-pip python3-subunit socat texinfo unzip wget xz-utils zstd
```
Plus `en_US.UTF-8` locale enabled in the image (uncomment in /etc/locale.gen && locale-gen — non-interactive method documented for containers).

## Ubuntu 22.04-specific note
- On Debian 11 and Ubuntu 22.04 the distro `python3-websockets` package does not meet the minimum version. It is only needed for the sstate mirror CDN fragment (core/yocto/sstate-mirror-cdn). Options: buildtools tarball, or pip install websockets in a venv.
- Note `zstd` is in the required list (also needed by ipk/opkg zstd packaging introduced in 5.0).

## Disk/RAM guidance
- ~140 GB free disk for core-image-sato/qemux86-64 baseline; much more for complex images (weston + container runtime stacks).
- ~32 GB RAM workable for baseline builds on 4 cores.
