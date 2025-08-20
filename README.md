# jacocoguardian
jacoco lib api and plugin wrapper

# 依赖以及配置demo 示例如下：
## 根配置的 build.gradle.kts 中增加
```groovy or kts

buildscript {
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        classpath("com.github.raingift:jacoco-guard-plugin:0.0.1")
    }
}

apply plugin: "com.hermes.code.jacoco"

jacocoConfig {

    enableJacoco = true

    includeProjects = [":module_xxx:xxx", ":module_B:xxx"]
}

```
## 工程分两个部分
### 1. jacoco-guard-plugin
jacoco的插件能力分 本地测试能力以及远端设备能力

### 2. jacoco-guard-lib
jacoco针对 远端采集能力的适配
```kotlin

val jacocoHelper = JacocoHelper(
    localDirPath = "xxx", // 本地路径
    deviceDirPath = "yyy" // 设备路径
).apply {
    setLogger(object : ILogger {
        override fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        override fun e(tag: String, msg: String) {
            Log.e(tag, msg)
        }
    })
}


jacocoHelper.generateEcFile(true) {
    println(it.data.msg)
}

```
