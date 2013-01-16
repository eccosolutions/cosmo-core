
cosmo-core is the core functionality extracted from OSAF's "Cosmo" Chandler server.

For release notes, see CHANGELOG.md



To Cut A Release
================

mvn release:prepare -Prelease,central -Dresume=false -DautoVersionSubmodules=true

For more info on cutting a release and deploying to Maven Central, see:

https://github.com/whirlwind-match/fuzzydb/wiki/Release-Instructions


