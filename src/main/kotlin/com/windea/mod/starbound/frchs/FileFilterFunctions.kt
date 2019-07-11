package com.windea.mod.starbound.frchs

import com.windea.commons.kotlin.extension.*
import com.windea.commons.kotlin.loader.*
import java.nio.file.*

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
	"raceeffect","raceeffect.patch"
)

private val notToConvertFileNames = arrayOf(
	"000_template.species.patch.txt",
	"_metadata.yml",
	"_previewimage",
	"LICENSE"
)


//不能直接判断包含"."扩展名
private fun deleteFiles(reTranslationsPath: String) {
	Path.of(reTranslationsPath).toFile().walk()
		.filter { it.isFile && it.name endsWith toDeleteFileExtensions }
		.forEach {
			println("删除文件：${it.path}")
			it.delete()
		}
	println("已删除所有非必要的文件。")
}

//读写操作应当在最后执行，否则无法成功重命名文件。
//操你妈的json格式都能写错！
//需要转化注释和非法字符
private fun convertFiles(reTranslationsPath: String) {
	Path.of(reTranslationsPath).toFile().walk()
		.filter { it.isFile && it.name !in notToConvertFileNames }
		.forEach {
			println("转化文件：${it.path}")
			val dataText = it.readText().replace("//", "#").replace("\t", "  ")
			val data = YamlLoader.instance().fromString(dataText, Any::class.java)
			YamlLoader.instance().toFile(data, it.path)
		}
	println("已转化所有必要的文件到yaml文件。")
}

fun main() {
	val relTranslationsPath = "translations"
	
	println("""
		准备进行操作：从FrackinRaces仓库重新加载需要翻译的文件。
		危险操作，请再次确定是否执行！（Y/N）
	""".trimIndent())
	if(readLine()?.toLowerCase() == "y") {
		deleteFiles(relTranslationsPath)
		convertFiles(relTranslationsPath)
	}
}
