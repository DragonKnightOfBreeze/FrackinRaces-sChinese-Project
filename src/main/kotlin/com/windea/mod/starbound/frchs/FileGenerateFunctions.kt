package com.windea.mod.starbound.frchs

import com.windea.commons.kotlin.loader.*
import java.io.*

private const val relTranslationsPath = "translations"

private const val relPackagePath = "package"

private val convertFileNames = arrayOf("patch", "yml", "yaml")

private fun copyFiles() {
	File(relTranslationsPath).copyRecursively(File(relPackagePath), true)
}

private fun convertFiles() {
	File(relPackagePath).walk()
		.filter { it.extension in convertFileNames }
		.forEach {
			println("转化文件：${it.path}")
			val data = YamlLoader.instance().fromFile(it.path, Any::class.java)
			JsonLoader.instance().toFile(data, it.path)
		}
	println("已生成所有文件。")
}


fun main() {
	println("""
		生成mod包包含的文件到指定目录。
		是否执行？（Y/N）
	""".trimIndent())
	if(readLine()?.toLowerCase() == "y") {
		copyFiles()
		convertFiles()
	}
}
