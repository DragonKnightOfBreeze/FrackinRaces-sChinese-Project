# 概述

Frackin' Races汉化Mod。

Mod概述：见[translations/_metadata](translations/_metadata)。

如有问题，请发Issue。如想合作，请发Issue或Pull Request。

# 相关链接

* [本Mod的项目地址](https://github.com/DragonKnightOfBreeze/FrackinRaces-sChinese-Project)
* [FU汉化Mod的项目地址](https://gitlab.imsky.cc:666/ProjectSky/FrackinUniverse-sChinese-Project)
* [Frackin' Universe的项目地址](https://github.com/sayterdarkwynd/FrackinUniverse)
* [Frackin' Race的项目地址](https://github.com/sayterdarkwynd/FrackinRaces)

# 说明

* 翻译文件都在`translations`目录下，采用yaml格式，相比json格式更加简单，更加易于读写。 
* 对于每个翻译文件，`rawValue$`表示原本的英文文本，`value`表示翻译后的文本。
* 如果翻译后的文本是多行文本，请考虑使用多行字符串（即在`value: `后添加`|-`，然后另起一行并缩进，写上下一行的内容）。
* 翻译时，可以考虑以下特殊注释：
    * `##@ @NotTranslate` 未翻译。
    * `##@ @NotSure` 不确定原本含义的翻译。
    * `##@ @Fixed` 为了确保特殊名词之间的统一性，修改过后的已确定原本含义的翻译。
* 仅包含已翻译的种族资料文件（即`/translations/species`目录下的文件）。
* 如想翻译其他种族资料，请从FR项目自行添加，并转为yaml文件。
* 非创意工坊的Mod下载地址（下载后直接放到游戏的mods文件夹下即可）：`release/FR SChinese.pak`。
