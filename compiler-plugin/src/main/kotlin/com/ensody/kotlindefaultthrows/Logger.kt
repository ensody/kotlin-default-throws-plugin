package com.ensody.kotlindefaultthrows

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal data class Logger(val debug: Boolean, val messageCollector: MessageCollector) {
    fun log(message: String) {
        if (debug) {
            messageCollector.report(CompilerMessageSeverity.WARNING, message)
        }
    }
}
