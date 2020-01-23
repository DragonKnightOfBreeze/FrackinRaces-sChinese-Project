package com.windea.mod.starbound.frchs

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.json.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.windea.breezeframework.core.enums.core.*
import com.windea.breezeframework.core.extensions.*
import com.windea.breezeframework.data.serializers.*
import com.windea.breezeframework.data.serializers.json.*
import com.windea.breezeframework.data.serializers.json.JsonSerializer
import com.windea.breezeframework.data.serializers.yaml.*
import java.io.*

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
	"mmupgradegui.config.", "mmupgradegui.config.patch",
	"mmupgradegui.original.config", "mmupgradegui.original.config.patch",
	"statWindow.config", "statWindow.config.patch",
	"extraStatsWindow.config", "extraStatsWindow.config.patch"
)

private val selectRules = arrayOf(
	"*.{activeitem, consumable, item, beamaxe, object}" to arrayOf(
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
	"mmupgradegui.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"mmupgradegui.original.config" to arrayOf(
		"/upgrades/{size}/description"
	),
	"statWindow.config" to arrayOf(
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
	//deleteFiles()

	//选择origin目录下必要的文件，并将非patch文件转化为patch文件
	selectFiles()

	//根据过滤规则，将origin目录下的文件合并到translations目录下
	//mergeFiles()

	//将translations目录下的翻译文件提取到package目录下
	//extractFiles()

	//打包package目录下的所有文件为release/FrChinese.pak
	//命令：{starboundPath}\win32\asset_packer.exe "{projectPath}\package" "{projectPath}\FrChinese.pak"
	//generatePak()
}

private fun configure() {
	//如何保证字符串的输出格式的一致性？
	JacksonYamlSerializerConfig.configure {
		it.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) //不要以"---"开头
		it.enable(SerializationFeature.INDENT_OUTPUT)
		it.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
	}
	JacksonJsonSerializerConfig.configure {
		it.enable(SerializationFeature.INDENT_OUTPUT)
		it.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature()) //允许注释
		it.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature()) //允许多行字符串
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

private fun deleteFiles() {
	//删除非必要的文件
	originPath.toFile().walk()
		.filterNot { file -> !file.isFile || file.name endsWithIc notDeleteFileExtensions }
		.forEach { file ->
			file.delete()
			println("\t已删除文件：${file.path}")
		}
	//删除空目录
	originPath.toFile().walkBottomUp()
		.filter { dir -> dir.isDirectory }
		.forEach { dir ->
			dir.delete()
		}
	println("已删除所有非必要的文件和目录。")
}

@Suppress("UNCHECKED_CAST")
private fun selectFiles() {
	//选择、转化和重命名文件
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//如果以"-"开头，则说明已经选择过，跳过这次循环，这里需要关闭流
			if(file.reader().use { it.read().toChar() == '-'}) return@forEach

			//考虑到starbound json可能包含注释和多行字符串，需要特殊配置jackson
			//另外对于species文件，需要做进一步的处理
			val fileText = file.readText().handleFileText(file.name)
			val data = JsonSerializer.instance.load<Any>(fileText)

			//匹配路径时去除.patch后缀
			val rule = selectRules.first { file.name.removeSuffix(".patch").matchesBy(it.first, MatchType.EditorConfig) }.second
			val selectedData = when(data) {
				//认为是非patch的配置文件
				is Map<*, *> -> rule.flatMap { path ->
					data.deepQueryByPath(path)
				}
				//认为是patch文件
				is List<*> -> rule.flatMap { path ->
					//列表里面可能还嵌套一层列表，然后才是映射，这也可以？
					val isNested = data.firstOrNull() is List<*>
					(if(isNested) (data as List<List<*>>).flatten() else data).queryAndFilterByPath(path)
				}
				//认为不可能发生
				else -> throw IllegalArgumentException()
			}.filterNot { (it["rawValue"] as String?).isNullOrBlank() } //有必要考虑空字符串，因为有些种族mod作者脑子有坑
			//如果没有选择到任何数据，则删除该文件
			if(selectedData.isEmpty()) {
				file.delete()
			} else {
				YamlSerializer.instance.dump(selectedData, file)
			}
			println("\t已选择文件：${file.path}")
		}
	//重命名文件
	originPath.toFile().walk()
		.filter { file-> file.isFile }
		.forEach { file->
			//如果不是patch文件，则重命名为patch文件
			if(!file.name.endsWith("patch")) {
				println("\t已重命名文件：${file.path}")
				file.renameTo(("${file.path}.patch").toFile())
			}
		}
	//删除空目录
	originPath.toFile().walkBottomUp()
		.filter { dir -> dir.isDirectory }
		.forEach { dir ->
			dir.delete()
		}
	println("已删除所有非必要的文件和目录。")
	println("已选择所有必要的文件在 $originPath 目录。")
}

private fun mergeFiles() {
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//合并两个文件中的数据
			val translationFile = file.path.replace(originPath, translationsPath).toFile()
			val originData = YamlSerializer.instance.load<List<Map<String, Any?>>>(file)
			val translationData = YamlSerializer.instance.load<List<Map<String, Any?>>>(translationFile)
			val mergedData = if(translationFile.exists()) {
				originData.innerJoin(translationData) { a, b -> a["path"] == b["path"] }.map { (a, b) ->
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
					).filterValueNotNull() //舍弃值为null的键值对
				}
			} else {
				originData
			}
			YamlSerializer.instance.dump(mergedData, translationFile)
			println("\t已合并文件：${file.path} -> ${translationFile.path}")
		}
	println("已合并所有必要的文件从 $originPath 到 $translationsPath 目录。")
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
					(data as List<Map<String, *>>).map {
						mapOf(
							"op" to  it["op"],
							"path" to it["path"],
							//如果标注为原文已改变，则采用rawValue而非value
							"value" to (if(it["translationAnnotation"] == "Changed") it["rawValue"] else it["value"])
						)
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


private fun Map<*, *>.deepQueryByPath(path: String): List<Map<String, Any?>> {
	return this.deepQuery<Any?>(path.replace("-", "[]")).map { (k, v) ->
		mapOf(
			"op" to "replace",
			"path" to k,
			"rawValue" to v,
			"translationAnnotation" to "NotTranslated"
		)
	}
}

@Suppress("UNCHECKED_CAST")
private fun List<*>.queryAndFilterByPath(path: String): List<Map<String, Any?>> {
	return (this as List<Map<String, Any?>>).flatMap {
		val pathValue = it["path"].toString()
		val value = it["value"]
		when {
			//如果路径匹配或者相等，说明value属性的值就是我们要找的值
			pathValue == path || pathValue.matchesBy(path, MatchType.PathReference) -> {
				listOf(mapOf(
					"op" to "replace",
					"path" to it["path"],
					"rawValue" to it["value"],
					"translationAnnotation" to "NotTranslated"
				))
			}
			//TODO 如果path不包含pathValue，说明不匹配（但是可能部分匹配）
			pathValue !in path -> listOf()
			//如果路径不匹配，但value属性是列表，说明我们需要进一步到value属性中勋章我们要找的值
			value is List<*> -> {
				value.deepQuery<Any?>(path.removePrefix(pathValue).replace("-", "[]")).map { (k, v) ->
					mapOf(
						"op" to "replace",
						"path" to k,
						"rawValue" to v,
						"translationAnnotation" to "NotTranslated"
					)
				}
			}
			//同上
			value is Map<*, *> -> {
				value.deepQuery<Any?>(path.removePrefix(pathValue).replace("-", "[]")).map { (k, v) ->
					mapOf(
						"op" to "replace",
						"path" to "$pathValue/$k",
						"rawValue" to v,
						"translationAnnotation" to "NotTranslated"
					)
				}
			}
			else -> listOf()
		}
	}
}

private fun String.handleFileText(fileName:String):String{
	return this.replace("\t", "  ").let{
		//对于species文件需要特殊对待
		if(fileName.contains("species")){
			it.lines().joinToString("\n") { s->s.trim() }
		}else{
			it
		}
	}
}
