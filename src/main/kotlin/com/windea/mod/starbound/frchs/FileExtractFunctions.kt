package com.windea.mod.starbound.frchs

import com.windea.commons.kotlin.extension.*
import java.io.*
import kotlin.text.endsWith

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

//忽略：species

// fun main() {
// 	File("translations").walk()
// 		.forEach {
// 			if(it.name endsWith ".raceeffect.yml") {
// 				it.deleteOnExit()
// 			}
// 			if(it.name endsWith ".patch.yml") {
// 				it.renameTo(File(it.path.removeSuffix(".yml")))
// 			}
// 		}
// }
