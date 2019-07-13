package com.windea.mod.starbound.frchs

import com.windea.commons.kotlin.extension.*
import com.windea.commons.kotlin.loader.*
import java.io.*


private const val relTranslationsPath = "translations"

private val toDeleteFileExtensions = arrayOf(
	"jpg", "png",
	"lua", "lua.orig", "lua.patch",
	"ogg",
	"animation", "animation.patch",
	"weaponability", "weaponability.patch",
	"frames", "frames.patch",
	"particle", "particle.patch",
	"projectile", "projectile.patch",
	"recipe", "recipe.patch",
	"treasurepools", "treasurepools.patch",
	"raceeffect", "raceeffect.patch"
)

private val notToConvertFileNames = arrayOf(
	"000_template.species.patch.txt",
	"_metadata.yml",
	"_previewimage",
	"LICENSE"
)

private fun deleteFiles() {
	//不能直接判断包含"."扩展名
	File(relTranslationsPath).walk()
		.filter { it.isFile && it.name endsWith toDeleteFileExtensions }
		.forEach {
			println("删除文件：${it.path}")
			it.delete()
		}
	println("已删除所有非必要的文件。")
}

private fun convertFiles() {
	//读写操作应当在最后执行，否则无法成功重命名文件。
	File(relTranslationsPath).walk()
		.filter { it.isFile && it.name !in notToConvertFileNames }
		.forEach {
			println("转化文件：${it.path}")
			//需要转化注释和非法字符
			val yamlText = it.readText().replace("//", "#").replace("\t", "  ")
			val data = YamlLoader.instance().fromString(yamlText, Any::class.java)
			YamlLoader.instance().toFile(data, it.path)
		}
	println("已转化所有必要的文件到yaml文件。")
}


fun main() {
	println("""
		从指定目录中选择需要翻译的文件。
		危险操作，是否执行？（Y/N）
	""".trimIndent())
	if(readLine()?.toLowerCase() == "y") {
		deleteFiles()
		convertFiles()
	}
}
