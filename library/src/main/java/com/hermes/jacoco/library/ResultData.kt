package com.hermes.jacoco.library

import java.io.Serializable

data class GenerateResult(var code: Int, var data: GenerateData) : Serializable

data class GenerateData(var msg: String, var localDirPath: String, var deviceDirPath: String) :
    Serializable

// 成功
const val SUCCESS = 0

// 反射异常
const val REFLECTION_FAIL = 1

// 关流异常
const val IO_FAIL = 2
