package com.windea.mod.starbound.frchs

//操作：
//替换 op: add -> op: replace
//提取 value?, description, shotdescription, label, ...
//更改文件名，加上".patch"

//规则：
//.patch -> /N/value? || /[]/rawValue?
//.config -> /gui/title/value, /tooltipBoxes/[]/tooltip, /defaultTooltip, /upgrades/size[]/description
//.config -> /gui/windowtitle/title, /gui/windowtitle/subtitle, /gui/immunitiesLabel/value, /statuses/{}/name
//.statuseffect -> /label
//.activeitem, .item, .consumable, .tech -> /shotdescription, /description

//忽略：
///species
