<!-- Context: core/yocto/migration-6.0 | Priority: high | Version: 1.0 | Updated: 2026-08-24 -->

# Migration: Yocto 6.0 (wrynose, LTS)

**Purpose**: Key changes from 5.x → 6.0 affecting Neo-PiOS (custom distro, OpenRC, RPi4).
**Last Updated**: 2026-08-24

## Quick Reference
**Update Triggers**: Upgrade to wrynose 6.x | New DISTRO_FEATURES | CVE workflow changes
**Audience**: Developers, AI agents

## Core Changes

| Area | Change | Impact on Neo-PiOS |
|------|--------|-------------------|
| INIT_MANAGER | Default changed `none` → `systemd` | ✅ Already `INIT_MANAGER = "openrc"` in neopios.conf — overrides default |
| DISTRO_FEATURES | `BACKFILL*` vars OBSOLETE → `DEFAULTS`/`OPTED_OUT` | ✅ Already using `DISTRO_FEATURES_OPTED_OUT:append = " ptest vulkan multiarch x11 pcmcia 3g"` |
| CVE checking | `cve-check` class REMOVED → `sbom-cve-check` | ⚠️ Future: replace `INHERIT += "cve-check"` with `OE_FRAGMENTS += "core/yocto/sbom-cve-check"` |
| Wic paths | `.wks` files MUST be in `<layer>/files/wic/` | ℹ️ Not using custom .wks yet — note for future |
| pkgconfig | `pkgconfig` recipe → `pkgconf`, vars not auto-exported | ℹ️ Ensure recipes inherit `pkgconfig` class |
| Host reqs | Ubuntu 22.04 still supported | ✅ Docker base valid |

## Removed (Watch For)

- **Classes**: `cve-check`, `cve-update-db-native`, `cve-update-nvd2-native`, `oelint`
- **Recipes**: `pkgconfig` (→ pkgconf), `systemd-compat-units`, `jquery`, `gstreamer1.0-vaapi`
- **PACKAGECONFIG**: `systemd sysvinit`, `mesa freedreno-fdperf`, `webkitgtk soup2`

## Codebase References

**Current usage**: `layers/meta-neopios/conf/distro/neopios.conf` — `INIT_MANAGER = "openrc"`, `DISTRO_FEATURES_OPTED_OUT`
**Future CVE workflow**: `.opencode/context/core/context-system/operations/` (sbom-cve-check fragment location TBD)

## Related Files
- Host requirements: `yocto/host-system-requirements.md` (if created)
- Migration 5.x: (optional, historical)
