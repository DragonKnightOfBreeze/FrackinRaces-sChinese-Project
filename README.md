# 概述

Frakin Races的汉化mod。

项目地址：[Github](https://github.com/DragonKnightOfBreeze/FrackinRaces-sChinese-Project)。

Mod概述：[translations/_metadata](translations/_metadata)。

如想合作，请发Issue或Pull Request。
如有问题，请发Issue。

# 相关信息

* 作者：微风的龙骑士 风游迩  
* Steam：微风的龙骑士 风游迩
* Github：DragonKnightOfBreeze   

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
