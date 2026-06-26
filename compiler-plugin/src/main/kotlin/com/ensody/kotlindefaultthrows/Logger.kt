package com.ensody.kotlindefaultthrows

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.reportLog

internal data class Logger(val debug: Boolean, val configuration: CompilerConfiguration) {
    fun log(message: String) {
        if (debug) {
            configuration.reportLog(message)
        }
    }
}
