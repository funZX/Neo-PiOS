# Fix QA issue when pam in DISTRO_FEATURES: openrc installs pam_openrc.so
# but FILES does not ship it (libpam via PACKAGECONFIG[pam])
FILES:${PN}:append = " ${base_libdir}/security/pam_openrc.so"
