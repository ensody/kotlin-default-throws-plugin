package com.ensody.kotlindefaultthrows

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.isInvokeSuspendOfLambda
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

/** Compatibility interface for supporting old and new Kotlin versions with the same code. */
public interface DefaultThrowsPluginRegistrarCompat {
    // TODO: Remove this once we switch to Kotlin >=2.3
    public val pluginId: String
}

@AutoService(CompilerPluginRegistrar::class)
public class DefaultThrowsPluginRegistrar : CompilerPluginRegistrar(), DefaultThrowsPluginRegistrarCompat {
    override val pluginId: String = "com.ensody.kotlindefaultthrows"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val logger = Logger(debug = true, messageCollector = messageCollector)
        IrGenerationExtension.registerExtension(
            IrDefaultThrowsExtension(logger),
        )
    }
}

internal class IrDefaultThrowsExtension(
    val logger: Logger,
) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        moduleFragment.transformChildren(ElementTransformer(pluginContext, logger), data = null)
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class ElementTransformer(
    val context: IrPluginContext,
    val logger: Logger,
) : IrElementTransformerVoidWithContext() {

    val throwsFqName = FqName("kotlin.Throws")
    val jvmThrowsFqName = FqName("kotlin.jvm.Throws")
    val throwsClassId = ClassId(StandardClassIds.BASE_KOTLIN_PACKAGE, Name.identifier("Throws"))
    val jvmThrowsClassId = ClassId(StandardClassIds.BASE_JVM_PACKAGE, Name.identifier("Throws"))
    val throwsClass: IrClassSymbol = context.finderForBuiltins().findClass(throwsClassId)
        ?: context.finderForBuiltins().findClass(jvmThrowsClassId)
        ?: error("Throws class not found")
    val throwsConstructor = throwsClass.constructors.single()

    val throwableClass: IrClassSymbol = context.irBuiltIns.throwableClass
    val arrayClass: IrClassSymbol = context.irBuiltIns.arrayClass

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (!declaration.isSuspend ||
            declaration.isFakeOverride ||
            declaration.isInvokeSuspendOfLambda() ||
            declaration.hasAnnotation(throwsFqName) ||
            declaration.hasAnnotation(jvmThrowsFqName)
        ) {
            return super.visitFunctionNew(declaration)
        }
        val builder = DeclarationIrBuilder(context, declaration.symbol)
        val annotation = builder.irCallConstructor(throwsConstructor, emptyList()).apply {
            val classType = throwableClass.defaultType
            val classRef = builder.kClassReference(classType).apply {
                symbol = throwableClass
                type = classType
            }
            arguments[0] = builder.irVararg(elementType = classRef.type, values = listOf(classRef))
        }
        context.metadataDeclarationRegistrar.addMetadataVisibleAnnotationsToElement(declaration, annotation)
        return declaration
    }
}
