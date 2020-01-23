# 概述

Frackin' Races汉化Mod。

Mod概述：见项目文件`translations/_metadata`。

非创意工坊的Mod下载地址：见项目文件`release/FrChinese.pak`，或者项目的release页面。

如有问题，请发Issue。如想合作，请发Issue或Pull Request。

# 相关链接

* [本Mod的项目地址](https://github.com/DragonKnightOfBreeze/FrackinRaces-sChinese-Project)
* [FU汉化Mod的项目地址](https://github.com/ProjectSky/FrackinUniverse-sChinese-Project)
* [Frackin' Universe的项目地址](https://github.com/sayterdarkwynd/FrackinUniverse)
* [Frackin' Race的项目地址](https://github.com/sayterdarkwynd/FrackinRaces)

# 说明

* 翻译文件存储在`translations`目录下，采用yaml格式。
* 翻译文件的格式说明：
  * `op` 操作的类型。应当为replace。
  * `path` 操作的目录路径。
  * `value` 翻译后文本。一般为中文，未翻译时为英文。
  * `rawValue` 原始文本。一般为英文。
  * `translationAnnotation` 翻译注解。用作备注。
    * `Translated` 已翻译。
    * `NotTranslated` 未翻译。
    * `Unsure` 未确定原本含义的翻译。
    * `Fixed` 为了确保特殊名词之间的统一性，修改后的已确定原本含义的翻译。  
    * `Changed` 原文已发生改变，但是尚未同步翻译。
  * `translationNote` 翻译笔记。用作备注。
* 如果翻译文本是多行文本，请考虑使用多行字符串（即在`value: `后添加`|-`，然后另起一行缩进，写上下一行的内容）。
* 原则上，不会翻译更多的种族描述文本（因为实在是太多了）。
  
