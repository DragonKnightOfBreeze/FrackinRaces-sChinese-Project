package com.windea.mod.starbound.frchs.script

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.windea.breezeframework.core.enums.core.*
import com.windea.breezeframework.core.extensions.*
import com.windea.breezeframework.data.serializers.*
import com.windea.breezeframework.data.serializers.json.*
import com.windea.breezeframework.data.serializers.json.JsonSerializer
import com.windea.breezeframework.data.serializers.yaml.*

private const val originPath = "origin"
private const val translationsPath = "translations"
private const val packagePath = "package"

private const val drive = "D:"
private const val projectPath = "D:\\My Documents\\My Projects\\Managed\\FrackinRaces-sChinese-Project"
private const val starBoundPath = "D:\\Programs\\Steam\\steamapps\\common\\Starbound"
private const val frProjectPath = "https://github.com/sayterdarkwynd/FrackinRaces"

private const val pakPath = "release\\FrChinese.pak"
private const val assetPackerPath = "win32\\asset_packer.exe"

private val filterRules = arrayOf(
	"*.activeitem" to arrayOf(
		"/itemTags/-",
		"/shotdescription",
		"/description"
	),
	"*.{consumable, item, beamaxe, object}" to arrayOf(
		"/shotdescription",
		"/description"
	),
	"*.questtemplate" to arrayOf(
		"/title",
		"/text",
		"/completionText",
		"scriptConfig/descriptions/{step}"
	),
	"*.radiomessages" to arrayOf(
		"/{message}/text"
	),
	"*.species" to arrayOf(
		"/charCreationTooltip/description"
	),
	"statuseffect" to arrayOf(
		"/label"
	),
	"tech" to arrayOf(
		"/shotdescription",
		"/description"
	),
	"mmupgradegui.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"mmupgradegui.original.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"statsWindow.config" to arrayOf(
		"/gui/windowtitle/title",
		"/gui/windowtitle/subtitle",
		"/gui/immunitiesLabel/value",
		"/statuses/{statusName}/name"
	),
	"extraStatsWindow.config" to arrayOf(
		"/gui/title/value",
		"/tooltipBoxes/-/tooltip",
		"/defaultTooltip"
	)
)

private val selectFileExtensions = arrayOf(
	"activeitem", "activeitem.patch",
	"consumable", "consumable.patch",
	"item", "item.patch",
	"beamaxe", "beamaxe.patch",
	"object", "object.patch",
	"questtemplate", "questtemplate.patch",
	"radiomessages", "radiomessages.patch",
	"species", "species.patch",
	"statuseffect", "statuseffect.patch",
	"tech", "tech.patch",
	"mmupgradegui.config",
	"mmupgradegui.original.config",
	"statsWindow.config",
	"extraStatsWindow.config"
)

private val convertFileExtensions = arrayOf(
	"patch",
	"_metadata"
)

fun main() {
	configure()

	//从Github克隆FR项目到origin目录
	//命令：git clone https://github.com/sayterdarkwynd/FrackinRaces ./origin
	//getOrigin()

	//根据过滤规则，将origin目录下的文件合并到translations目录下
	//如果不是patch文件，需要改为patch文件
	//selectFiles()

	//将translations目录下的翻译文件提取到package目录下
	extractFiles()

	//打包package目录下的所有文件为release/FrChinese.pak
	//命令：
	//cd {starboundPath}\win32
	//asset_packer.exe "{projectPath}\package" "{projectPath}\FrChinese.pak"
	generatePak()
}

private fun configure() {
	//如何保证字符串的输出格式的一致性？
	JacksonYamlSerializerConfig.configure {
		it.enable(JsonParser.Feature.ALLOW_COMMENTS)
		it.enable(SerializationFeature.INDENT_OUTPUT)
		it.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
	}
	JacksonJsonSerializerConfig.configure {
		it.enable(SerializationFeature.INDENT_OUTPUT)
	}
}

private fun getOrigin() {
	//如果该地址已存在，则不从远程拉取Git仓库
	if(originPath.toPath().exists()) return

	//需要等待拉取完毕
	Runtime.getRuntime().exec("""
		git clone $frProjectPath ./$originPath
	""".trimIndent()).waitFor()
	println("已克隆FR项目仓库到 $originPath 目录。")
}

@Suppress("UNCHECKED_CAST")
private fun selectFiles() {
	originPath.toFile().walk()
		.filter { file -> file.isFile && file.name endsWithIc selectFileExtensions }
		.forEach { file ->
			//对于非patch文件
			val rule = filterRules.first { file.name matchesPath it.first }.second
			//考虑到starbound json可能包含多行字符串和注释，这里按照yaml格式读取数据
			val data = YamlSerializer.instance.load<Any>(file)
			val selectedData = when(data) {
				//认为是非patch的配置文件
				is Map<*, *> -> rule.flatMap { path ->
					data.deepQuery(path).map { (k, v) ->
						mapOf(
							"op" to "replace",
							"path" to k.switchCaseBy(ReferenceCase.JsonSchema),
							"rawValue" to v,
							"translationAnnotation" to "NotTranslated"
						)
					}
				}
				//认为是patch文件
				is List<*> -> (data as List<Map<String, Any?>>).map {
					//TODO 这里还需要转换value
					mapOf(
						"op" to "replace",
						"path" to it["path"],
						"rawValue" to it["value"],
						"translationAnnotation" to "NotTranslated"
					)
				}
				//认为不可能发生
				else -> throw IllegalArgumentException()
			}

			//合并新文件和旧文件中的数据
			val oldFile = file.path.replace(originPath, translationsPath).toFile()
			val newData = if(oldFile.exists()) {
				val oldData = YamlSerializer.instance.load<List<Map<String, Any?>>>(oldFile)
				selectedData.innerJoin(oldData) { a, b -> a["path"] == b["path"] }.map { (a, b) ->
					mapOf(
						"op" to "replace",
						"path" to a["path"],
						"value" to b["value"],
						"rawValue" to a["rawValue"],
						"translationAnnotation" to when {
							//如果b中没有value属性，则表明未翻译
							b["value"] == null -> "NotTranslated"
							//如果b中rawValue属性与a中rawValue属性不匹配，则表明原文发生了改变
							b["rawValue"] != a["rawValue"] -> "Changed"
							//否则使用a中translationAnnotation属性
							else -> b["translationAnnotation"]
						},
						"translationNote" to b["translationNote"]
					).filterValueNotNull()
				}
			} else {
				selectedData
			}
			YamlSerializer.instance.dump(newData,oldFile)
		}
	println("已选择所有必要的文件到 $translationsPath 目录。")
}

@Suppress("UNCHECKED_CAST")
private fun extractFiles() {
	//复制全部文件到package目录下
	translationsPath.toFile().copyRecursively(packagePath.toFile(), true)

	//将yaml文件转化为json文件
	//相信package目录下除了notToConvertFileNames之外，全部需要转换为json文件
	packagePath.toFile().walk()
		.filter { file -> file.isFile && file.name endsWithIc convertFileExtensions }
		.forEach { file ->
			//转化yaml文件为json文件
			val data = YamlSerializer.instance.load<Any>(file)
			//去除不必要的附加信息
			val simplifiedData = when(data) {
				is List<*> -> {
					(data as List<MutableMap<String, *>>).onEach {
						it.remove("rawValue")
						it.remove("translationAnnotation")
						it.remove("translationNote")
					}
				}
				else -> {
					data
				}
			}
			JsonSerializer.instance.dump(simplifiedData, file)
			println("\t已转化文件：${file.path}")
		}
	println("已提取所有文件到 $packagePath 目录。")
}

private fun generatePak() {
	//最好不要在java命令行中使用cd命令
	Runtime.getRuntime().exec("""
		$starBoundPath\$assetPackerPath "$projectPath\$packagePath" "$projectPath\$pakPath"
	""".trimIndent())
	println("已生成pak包。")
}

private infix fun String.matchesPath(pattern: String): Boolean {
	return when {
		pattern.startsWith("*.{") && pattern.endsWith("}") -> {
			val suffixes = pattern.substring(3, pattern.length - 1).split(",").map { it.trim() }
			suffixes.any { this.endsWith(it) }
		}
		pattern.startsWith("*.") -> {
			val suffix = pattern.substring(2)
			this.endsWith(suffix)
		}
		else -> this == pattern
	}
}
