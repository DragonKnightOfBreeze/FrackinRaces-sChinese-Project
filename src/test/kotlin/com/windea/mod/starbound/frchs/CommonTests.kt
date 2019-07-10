package com.windea.mod.starbound.frchs

import com.windea.commons.kotlin.loader.*

fun main() {
	val data = """
	description: |-
	  Abc
	  Abc
	  
	  Abc
	""".trimIndent()
	
	val yamlData = YamlLoader.instance().fromString(data)
	val path = "src/test/resources/test.yml"
	YamlLoader.instance().toFile(yamlData,path)
	println(yamlData)
}
