#! /bin/bash

BB_COMMAND="bitbake"
alias bb=${BB_COMMAND}

alias bb.clean="bitbake -c clean -c cleansstate"
alias bb.edit="devtool modify"
alias bb.apply="devtool update-recipe -a ../layers/meta-neopios"
alias bb.close="devtool reset"
alias bb.shell="bitbake -c devshell"
alias bb.linux="bitbake linux -c"
alias bb.sysroot="bitbake -c build_target_sysroot build-sysroots"
alias bb.sdk="bitbake -c populate_sdk"
