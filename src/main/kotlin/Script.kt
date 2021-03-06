package com.windea.mod.starbound.frchs

import com.fasterxml.jackson.core.json.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.windea.breezeframework.core.enums.text.*
import com.windea.breezeframework.core.extensions.*
import com.windea.breezeframework.data.serializers.*
import com.windea.breezeframework.data.serializers.json.*
import com.windea.breezeframework.data.serializers.json.JsonSerializer
import com.windea.breezeframework.data.serializers.yaml.*
import java.util.*
import kotlin.system.*

private const val originPath = "origin"
private const val translationsPath = "translations"
private const val packagePath = "package"

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
		"/shortdescription",
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
		"/shortDescription", //居然不一样？？？
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


fun main() {
	configure()

	val scanner = Scanner(System.`in`)
	while(true) {
		println("""
			************
			f: 从Github克隆FR项目到origin目录
			d: 删除origin目录下不必要的文件
			s: 选择origin目录下必要的文件，并将非patch文件转化为patch文件
			m: 根据过滤规则，将translations目录下的文件合并到origin目录下
			e: 将translations目录下的翻译文件提取到package目录下
			g: 打包package目录下的所有文件为release/FrChinese.pak
			D: 删除指定目录内的空目录
			C: 将翻译后文本改为XML文本。

			exit: 退出
			************
		""".trimIndent())
		when(scanner.nextLine()) {
			"f" -> fetchOrigin()
			"d" -> deleteFiles()
			"s" -> selectFiles()
			"m" -> mergeFiles()
			"e" -> extractFiles()
			"g" -> generatePak()
			"D" -> deleteEmptyDirectories()
			"C" -> convertOriginFormatToXmlFormat()
			"exit" -> exitProcess(0)
			else -> println("指令错误。")
		}
	}
}

/**特殊配置jackson。*/
private fun configure() {
	//如何保证字符串的输出格式的一致性？
	JacksonYamlSerializerConfig.configure {
		it.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) //不要以"---"开头
		it.enable(SerializationFeature.INDENT_OUTPUT) //良好输出
		it.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature()) //没用
		it.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES) //能不用引号就不用引号，多行时使用多行字符串
	}
	JacksonJsonSerializerConfig.configure {
		it.enable(SerializationFeature.INDENT_OUTPUT) //良好输出
		it.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature()) //以防脑子有坑的种族mod作者
		it.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature()) //允许注释
		it.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature()) //允许多行字符串
	}
}

/**从github克隆FR项目到本地。*/
private fun fetchOrigin() {
	//如果该地址已存在，则不从远程拉取Git仓库
	if(originPath.toPath().exists()) return

	//需要等待拉取完毕
	execBlocking { """git clone $frProjectPath ./$originPath""" }
	println("已克隆FR项目仓库到 $originPath 目录。")
}

/**在origin目录删除不必要的文件。*/
private fun deleteFiles() {
	//删除非必要的文件
	originPath.toFile().walk()
		.filter { file -> file.isFile && !file.name.endsWithIgnoreCase(notDeleteFileExtensions) }
		.forEach { file ->
			file.delete()
			println("已删除文件：${file.path}")
		}
	//删除空目录
	originPath.toFile().walkBottomUp()
		.filter { dir -> dir.isDirectory }
		.forEach { dir ->
			dir.delete()
		}
	println("已删除所有非必要的文件和目录。")
}

/**在origin目录选择、重命名和过滤文件。*/
@Suppress("UNCHECKED_CAST")
private fun selectFiles() {
	//选择、转化和重命名文件
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//如果以"-"开头，则说明已经选择过，跳过这次循环，这里需要关闭流
			if(file.reader().use { it.read().toChar() == '-' }) return@forEach

			//考虑到starbound json可能包含注释和多行字符串，需要特殊配置jackson
			//另外对于species文件，需要做进一步的处理
			val data = JsonSerializer.instance.load<Any>(file)
			//匹配路径时去除.patch后缀
			val rule = selectRules.first { file.name.removeSuffix(".patch") matches it.first.toRegexBy(MatchType.EditorConfigPath) }.second
			val selectedData = when(data) {
				//认为是非patch的配置文件
				is Map<*, *> -> rule.flatMap { path ->
					data.deepQueryByPath(path)
				}
				//认为是patch文件
				is List<*> -> rule.flatMap { path ->
					//列表里面可能还嵌套一层列表，然后才是映射，这也可以？
					val isNested = data.firstOrNull() is List<*>
					(if(isNested) (data as List<List<*>>).flatten() else data).deepQueryAndFilterByPath(path)
				}
				//认为不可能发生
				else -> throw IllegalStateException()
			}.filterNot { (it["rawValue"] as String?).isNullOrBlank() } //有必要考虑空字符串，因为有些种族mod作者脑子有坑
			//如果没有选择到任何数据，则删除该文件
			if(selectedData.isEmpty()) {
				file.delete()
			} else {
				//以防某些种族mod作者脑子有坑
				val fixedSelectedData = when {
					//规范species文档中的翻译文本，去除每行缩进，去除最后一行空行
					file.name.contains("species") -> selectedData.map { map ->
						map.toMutableMap().also { m ->
							m["rawValue"] = m["rawValue"].toString().lines().dropLastBlank().joinToString("\n") { it.trim() }
						}
					}
					else -> selectedData
				}
				YamlSerializer.instance.dump(fixedSelectedData, file)
			}
			println("已选择文件：${file.path}")
		}
	//重命名文件
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//如果不是patch文件，则重命名为patch文件
			if(!file.name.endsWith(".patch")) {
				println("已重命名文件：${file.path}")
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

/**在origin目录参照translations目录中的对应文件合并文件内容。*/
private fun mergeFiles() {
	originPath.toFile().walk()
		.filter { file -> file.isFile }
		.forEach { file ->
			//合并两个文件中的数据
			val translationFile = file.path.replace("$originPath\\", "$translationsPath\\").toFile()
			//如果translations目录下的对应文件不存在，则直接返回
			if(!translationFile.exists()) return@forEach

			val originData = YamlSerializer.instance.load<List<Map<String, Any?>>>(file)
			val translationData = YamlSerializer.instance.load<List<Map<String, Any?>>>(translationFile)
			val mergedData = if(translationFile.exists()) {
				originData.innerJoin(translationData) { a, b -> a["path"] == b["path"] }.map { (a, b) ->
					linkedMapOf(
						"op" to "replace",
						"path" to a["path"],
						"value" to b["value"],
						"rawValue" to a["rawValue"]?.handleSingleQuote(),
						"translationAnnotation" to when {
							//如果b中rawValue属性与a中rawValue属性不匹配，则表明原文发生了改变
							b["rawValue"] != a["rawValue"] -> "Changed"
							//否则使用a中translationAnnotation属性
							else -> b["translationAnnotation"]
						},
						"translationNote" to b["translationNote"]
					).filterValues { it != null } //舍弃值为null的键值对
				}
			} else {
				originData
			}
			//暂时不主动改变translationFile中的数据
			YamlSerializer.instance.dump(mergedData, file)
			println("已合并文件：${file.path}\n <- ${translationFile.path}")
		}
	println("已合并所有必要的文件从 $translationsPath 到 $originPath 目录。")
}

/**从translations目录提取文件到package目录。*/
@Suppress("UNCHECKED_CAST")
private fun extractFiles() {
	//复制全部文件到package目录下
	translationsPath.toFile().copyRecursively(packagePath.toFile(), true)

	//将patch文件由yaml文件转化为json文件
	packagePath.toFile().walk()
		.filter { file -> file.isFile && file.name.endsWith(".patch") }
		.forEach { file ->
			val data = YamlSerializer.instance.load<List<Map<String, Any?>>>(file)
			//去除不必要的附加信息
			val simplifiedData = data.map {
				linkedMapOf(
					"op" to it["op"],
					"path" to it["path"],
					//如果找不到翻译后文本或者原文已更改，则采用rawValue
					"value" to when {
						it["value"] == null -> it["rawValue"]?.handleSingleQuote()
						it["translationAnnotation"] =="Changed" -> it["rawValue"]?.handleSingleQuote()
						else -> it["value"]?.toOriginText()
					}
				)
			}
			JsonSerializer.instance.dump(simplifiedData, file)
			println("已转化文件：${file.path}")
		}
	//将_metadata文件由yaml文件转化为json文件
	"$packagePath\\_metadata".toFile().also { file ->
		val metadata = YamlSerializer.instance.load<Any>(file)
		JsonSerializer.instance.dump(metadata, file)
	}
	println("已提取所有文件到 $packagePath 目录。")
}

/**生成mod包。*/
private fun generatePak() {
	//最好不要在java命令行中使用cd命令
	exec { """$starBoundPath\$assetPackerPath "$projectPath\$packagePath" "$projectPath\$pakPath"""" }
	println("已生成pak包。")
}


/**删除translations目录内的空目录。*/
private fun deleteEmptyDirectories() {
	translationsPath.toFile().walkBottomUp()
		.filter { dir -> dir.isDirectory }
		.forEach { dir ->
			dir.delete()
		}
	println("已删除所有空文件夹在 $translationsPath 目录。")
}

/**将translations目录下的.patch文件的value值的颜色标记语法改为xml标签。*/
private fun convertOriginFormatToXmlFormat() {
	translationsPath.toFile().walk()
		.filter { file -> file.isFile && file.name.endsWith(".patch") }
		.forEach { file ->
			val data = YamlSerializer.instance.load<List<MutableMap<String, Any?>>>(file)
			data.forEach { it["value"] = it["value"]?.toXmlText(); }
			YamlSerializer.instance.dump(data, file)
		}
	println("已将翻译后文本更改为xml文本在 $translationsPath 目录。")
}

/**对于非patch文件，根据路径查询翻译文本组。*/
private fun Map<*, *>.deepQueryByPath(path: String): List<Map<String, Any?>> {
	return this.deepQuery<Any?>(path).map { (k, v) ->
		linkedMapOf(
			"op" to "replace",
			"path" to k,
			"rawValue" to v?.handleSingleQuote()
		)
	}
}

/**对于patch文件根据路径查询并过滤翻译文本组。*/
@Suppress("UNCHECKED_CAST")
private fun List<*>.deepQueryAndFilterByPath(path: String): List<Map<String, Any?>> {
	return (this as List<Map<String, Any?>>).flatMap {
		val pathValue = it["path"].toString()
		val value = it["value"]
		//NOTE 由于代码逻辑的问题，暂时无法处理路径部分匹配的情况
		when {
			//如果路径匹配或者相等，说明value属性的值就是我们要找的值
			pathValue == path || pathValue matches path.toRegexBy(MatchType.PathReference) -> {
				listOf(linkedMapOf(
					"op" to "replace",
					"path" to it["path"],
					"rawValue" to it["value"]?.handleSingleQuote()
				))
			}
			//如果路径部分相等，但value属性是列表，说明我们需要进一步到value属性中寻找我们要找的值
			pathValue in path && value is List<*> -> {
				value.deepQuery<Any?>(path.removePrefix(pathValue)).map { (k, v) ->
					linkedMapOf(
						"op" to "replace",
						"path" to "$pathValue/$k",
						"rawValue" to v?.handleSingleQuote()
					)
				}
			}
			//同上
			pathValue in path && value is Map<*, *> -> {
				value.deepQuery<Any?>(path.removePrefix(pathValue)).map { (k, v) ->
					linkedMapOf(
						"op" to "replace",
						"path" to "$pathValue/$k",
						"rawValue" to v?.handleSingleQuote()
					)
				}
			}
			else -> listOf()
		}
	}
}

/**处理单引号。*/
private fun Any.handleSingleQuote(): String {
	return this.toString().replace("''", "'").replace("''", "'")
}

/**将颜色标签改为Xml标记语法。*/
private fun Any.toXmlText(): String {
	return this.toString().replace("^#","^_")
		.replace("\\^(.*?);(.*?)\\^reset;".toRegex(), "<$1>$2</$1>").replace("\\^(.*?);".toRegex(), "<$1>")
}

/**将颜色标签改为原始的颜色标记语法。*/
private fun Any.toOriginText(): String {
	return this.toString().replace("<_","<#")
		.replace("</.*?>".toRegex(), "^reset;").replace("<(.*?)>".toRegex(), "^$1;")
}
