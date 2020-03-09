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
* 原则上，不会翻译更多的种族描述文本（因为实在是太多了）。
* 翻译文件的格式说明：
  * `op` 操作的类型。应当为replace。
  * `path` 操作的目录路径。
  * `value` 翻译后文本。一般为中文，未翻译时为英文。
  * `rawValue` 原始文本。一般为英文。
  * `translationAnnotation` 翻译注解。用作备注。
    * `Changed` 原文已发生改变，但是尚未同步翻译。
    * `Unsure` 未确定原本含义的翻译。
    * `Fixed` 为了确保特殊名词之间的统一性，修改后的已确定原本含义的翻译。  
  * `translationNote` 翻译笔记。用作备注。

# 协助翻译指南

* 请首先百度学习：Github，Git，Json，Yaml，XML。
* 推荐使用（提供IDE+文件比较+翻译功能）：IntelliJ IDEA, Capslock+。
* 推荐安装的IDEA插件：Kotlin，Translation。
* 本项目采用Yaml格式进行汉化，相对Json格式要更加简洁清晰。
* 通过启动`Script.kt`脚本，可以执行一些常用命令。
  * 具体的命令和说明，请参阅输出的命令行说明。
* 通过使用IDEA的目录比较功能，对照比较和修订`origin`和``translations``目录中的对应文件，可以大大加快和简化翻译流程。
  * 目录比较功能的打开方式：选中`origin`目录右击打开菜单，选择`Compare With...`，接着选择`translations`目录。
  * 注意排除需要保留的文件（`_previewimage`，`LICENSE`和`_metadata`），操作方法：点击对应的叉号，直到没有任何图标为止。
* 通过修改或添加`translations`目录中的翻译文档中，与`rawValue`属性对应的`value`属性，即可完成对应文本的翻译。
  * 注意`value`是处理后的待翻译/已翻译文本，并非游戏中直接显示的待翻译/已翻译文本。
  * 原始的特殊颜色标记语法可以改用XML标签标记语法，便于翻译。
  * 例如，`^red;红色^reset;的^#fff;文本^reset;`可以被`<red>红色</red>的<_fff>文本</_fff>`替代。
  * 请参照其他同类文本完成翻译，部分文本的翻译格式可能与原文的原始格式有所不同（词语顺序、缩进、换行等）。 
  * 请保持翻译文档中的字符串属性的显示格式（单行，单引号包围，折行，多行等）。
  * 请根据情况选择对应的`translationAnnotation`，并编写必要的`translationNote`。
  * 除了`value`，`translationAnnotation`和`translationNote`属性之外，不要修改其他内容。
  