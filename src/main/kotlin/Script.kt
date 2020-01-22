package com.windea.mod.starbound.frchs

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
private const val frProjectPath = "https://github.com/sayterdarkwynd/FrackinRaces.git"

private const val pakPath = "release\\FrChinese.pak"
private const val assetPackerPath = "win32\\asset_packer.exe"

private val notDeleteFileExtensions = arrayOf(
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
	"mmupgradegui.config.","mmupgradegui.config.patch",
	"mmupgradegui.original.config","mmupgradegui.original.config.patch",
	"statWindow.config","statWindow.config.patch",
	"extraStatsWindow.config","extraStatsWindow.config.patch"
)

private val selectRules = arrayOf(
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
		"scriptConfig/descriptions/{descriptionName}"
	),
	"*.radiomessages" to arrayOf(
		"/{messageName}/text"
	),
	"*.species" to arrayOf(
		"/charCreationTooltip/description"
	),
	"*.statuseffect" to arrayOf(
		"/label"
	),
	"*.tech" to arrayOf(
		"/shotdescription",
		"/description"
	),
	"*.mmupgradegui.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"*.mmupgradegui.original.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"*.statsWindow.config" to arrayOf(
		"/gui/windowtitle/title",
		"/gui/windowtitle/subtitle",
		"/gui/immunitiesLabel/value",
		"/statuses/{statusName}/name"
	),
	"*.extraStatsWindow.config" to arrayOf(
		"/gui/title/value",
		"/tooltipBoxes/-/tooltip",
		"/defaultTooltip"
	)
)

private val notConvertFileExtensions = arrayOf(
	"_previewimage",
	"_LICENSE"
)


fun main() {
	configure()

	//从Github克隆FR项目到origin目录
	//命令：git clone https://github.com/sayterdarkwynd/FrackinRaces ./origin
	//getOrigin()

	//删除origin目录下不必要的文件
	deleteFiles()

	//根据过滤规则，将origin目录下的文件合并到translations目录下
	//如果不是patch文件，需要改为patch文件
	//selectFiles()

	//将translations目录下的翻译文件提取到package目录下
	//extractFiles()

	//打包package目录下的所有文件为release/FrChinese.pak
	//命令：{starboundPath}\win32\asset_packer.exe "{projectPath}\package" "{projectPath}\FrChinese.pak"
	//generatePak()
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

private fun deleteFiles(){
	//删除非必要的文件
	originPath.toFile().walk()
		.filterNot { file -> !file.isFile || file.name endsWithIc notDeleteFileExtensions }
		.forEach {
			file->file.delete()
			println("\t已删除文件：${file.path}")
		}
	//删除空目录
	originPath.toFile().walkBottomUp()
		.filter { dir -> dir.isDirectory }
		.forEach { dir ->
			dir.delete()
			println("\t已删除目录：${dir.path}")
		}
	println("已删除所有非必要的文件和目录。")
}

@Suppress("UNCHECKED_CAST")
private fun selectFiles() {
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//考虑到starbound json可能包含多行字符串和注释，这里按照yaml格式读取数据
			//匹配路径时去除.patch后缀
			val rule = selectRules.first { file.name.removeSuffix(".patch") matchesPath it.first }.second
			val data = YamlSerializer.instance.load<Any>(file)
			val selectedData = when(data) {
				//认为是非patch的配置文件
				is Map<*, *> -> rule.flatMap { path ->
					data.deepQueryByPath(path)
				}
				//认为是patch文件
				is List<*> -> rule.flatMap{path->
					data.queryAndFilterByPath(path)
				}
				//认为不可能发生
				else -> throw IllegalArgumentException()
			}

			//如果没有选择到任何数据，则结束这次循环
			if(selectedData.isEmpty()) return@forEach

			//合并两个文件中的数据
			val translationFile = file.path.replace(originPath, translationsPath).addSuffix(".patch").toFile()
			val mergedData = if(translationFile.exists()) {
				val translationData = YamlSerializer.instance.load<List<Map<String, Any?>>>(translationFile)
				selectedData.innerJoin(translationData) { a, b -> a["path"] == b["path"] }.map { (a, b) ->
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
			YamlSerializer.instance.dump(mergedData,translationFile)
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
		.filterNot { file -> !file.isFile || file.name endsWithIc notConvertFileExtensions }
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


private fun Map<*,*>.deepQueryByPath(path:String): List<Map<String, Any?>> {
	return this.deepQuery(path).map { (k, v) ->
		mapOf(
			"op" to "replace",
			"path" to k.switchCaseBy(ReferenceCase.JsonSchema),
			"rawValue" to v,
			"translationAnnotation" to "NotTranslated"
		)
	}
}

@Suppress("UNCHECKED_CAST")
private fun List<*>.queryAndFilterByPath(path:String):List<Map<String,Any?>>{
	return (this as List<Map<String,Any?>>).flatMap {
		val pathValue = it["path"].toString()
		val value = it["value"]
		if(pathValue matchesReference path){
			//如果路径匹配，说明value属性的值就是我们要找的值
			listOf(mapOf(
				"op" to "replace",
				"path" to it["path"],
				"rawValue" to it["value"],
				"translationAnnotation" to "NotTranslated"
			))
		}else if(value is List<*>){
			//如果路径不匹配，但value属性是列表，说明我们需要进一步到value属性中勋章我们要找的值
			value.deepQuery(path.removePrefix(pathValue)).map { (k, v) ->
				mapOf(
					"op" to "replace",
					"path" to k.switchCaseBy(ReferenceCase.JsonSchema),
					"rawValue" to v,
					"translationAnnotation" to "NotTranslated"
				)
			}
		}else if(value is Map<*,*>){
			//同上
			value.deepQuery(path.removePrefix(pathValue)).map { (k, v) ->
				mapOf(
					"op" to "replace",
					"path" to pathValue + "/" + k.switchCaseBy(ReferenceCase.JsonSchema),
					"rawValue" to v,
					"translationAnnotation" to "NotTranslated"
				)
			}
		}else{
			//说明不匹配
			listOf()
		}
	}
}

private infix fun String.matchesReference(pattern:String):Boolean{
	//仅供项目使用，不考虑所有情况
	val subPaths = this.removePrefix("/").split("/")
	val subPatterns = pattern.removePrefix("/").split("/")
	return subPaths.zip(subPatterns).all {(a,b)->
		when{
			b.startsWith("{") && b.endsWith("}") -> true
			b == "-" || b.startsWith("[") && b.endsWith("]") -> true
			else -> a== b
		}
	}
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
