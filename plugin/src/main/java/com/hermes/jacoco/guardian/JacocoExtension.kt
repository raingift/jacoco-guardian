package com.hermes.jacoco.guardian

import com.hermes.jacoco.const.fileFilter
import org.gradle.api.Project

open class JacocoExtension {

    /**
     * 指定jacoco的版本信息
     */
    var jacocoVersion: String? = "0.8.11"

    /**
     * 子 module 白名单配置信息; 单模块配置以及全量模块设置
     * 如果 includeProjects? is null, 不做特殊处理全部生效
     * 如果 includeProjects? is not null, 只对有效的模块处理
     */
    var includeProjects: List<String>? = null

    /**
     * 是否支持Jacoco 全量报表
     * true: 开启全量报告
     * false: 默认是仅支持单个模块的 Jacoco 报表
     */
    var enableJacocoFullReport: Boolean? = false

    /**
     * 开启调试
     * 1、是否开启覆盖率产物路径调试
     * 2、调试日志开启
     */
    var enableDebug: Boolean? = false

    /**
     * 是否启动Jacoco, 外部参数传递
     */
    var enableJacoco: Boolean? = false

    /**
     * 是否打开自动覆盖率
     * true: 打开自动覆盖率 (仪器化单测)
     * false: 禁用自动覆盖率，开启单测覆盖率 （本地单测）
     */
    var enableAutoCoverage: Boolean? = false

    /**
     * 配置需要开启覆盖率的 flavor name
     */
    var buildFlavorName: List<String>? = emptyList()

    /**
     * 自定义文件过滤器
     */
    var fileFilter: List<String>? = emptyList()
}

fun Project.jacocoConfig(block: JacocoExtension.() -> Unit) {
    extensions.configure(JacocoExtension::class.java, block)
}

/**
 * setting jacoco version
 */
fun getJacocoVersion(jacocoExt: JacocoExtension) =
    jacocoExt.jacocoVersion ?: "0.8.11"

/**
 * enable support auto coverage or not
 * 1:no depends unit 2:yes package
 */
fun enableAutoCoverage(jacocoExt: JacocoExtension) =
    jacocoExt.enableAutoCoverage.isTrue()

/**
 * enable support full coverage or not
 * 1:init task 2:root task 3:package task
 */
fun enableFullCoverage(jacocoExt: JacocoExtension) =
    jacocoExt.enableJacocoFullReport.isTrue()

/**
 * enable debug jacoco or not
 */
fun enableDebug(jacocoExt: JacocoExtension) = jacocoExt.enableDebug.isTrue()

/**
 * external injection file filtering
 */
fun getFileFilter(jacocoExt: JacocoExtension?) =
    jacocoExt?.fileFilter?.takeUnless { it.isEmpty() } ?: fileFilter

private fun Boolean?.isTrue() = this == true