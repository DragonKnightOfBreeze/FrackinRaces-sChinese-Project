plugins {
	id("org.jetbrains.kotlin.jvm") version "1.3.60"
}

group = "com.windea"
version = "1.0.9"

repositories {
	maven{
		url = uri("https://maven.pkg.github.com/dragonknightofbreeze/breeze-framework")
		credentials {
			//需要在这里配置你自己的GITHUB_API_KEY和相应的环境变量，否则可能无法访问这个仓库
			username = System.getenv("GITHUB_USERNAME")
			password = System.getenv("GITHUB_TOKEN")
		}
	}
	maven("https://maven.aliyun.com/nexus/content/groups/public")
	mavenCentral()
	jcenter()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation(kotlin("test-junit"))

	implementation("com.windea.breezeframework:breeze-core:1.0.12")
	implementation("com.windea.breezeframework:breeze-data:1.0.12")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.1")
}

tasks {
	compileKotlin {
		incremental = true
		javaPackagePrefix = "com.windea.mod.starbound.frchs"
		kotlinOptions {
			jvmTarget = "11"
		}
	}
	compileTestKotlin {
		incremental = true
		javaPackagePrefix = "com.windea.mod.starbound.frchs"
		kotlinOptions {
			jvmTarget = "11"
		}
	}
}





