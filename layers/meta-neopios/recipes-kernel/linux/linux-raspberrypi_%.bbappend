FILESEXTRAPATHS:prepend := "${THISDIR}:"
SRC_URI:append = " file://wifi-brcmfmac.cfg"
# Ensure brcmfmac firmware and wifi userspace are pulled via extra, but also
# ensure kernel module is auto-loaded
KERNEL_MODULE_AUTOLOAD:append = " brcmfmac"
