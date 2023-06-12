package org.klogic.autogeneration

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Max number of parameters in Java, according to the [JLS](https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.3.3).
 */
// TODO a value > 23 leads to an error with clashing JVM signatures
private const val MAX_NUMBER_OF_PARAMETERS: Int = 23

class FreshProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allAnnotatedWithFresh = resolver.getSymbolsWithAnnotation(Fresh::class.qualifiedName!!).toList()
        require(allAnnotatedWithFresh.size <= 1) {
            "Expected not more than one function annotated with ${Fresh::class} annotation but ${allAnnotatedWithFresh.size} found"
        }

        val (valid, invalid) = allAnnotatedWithFresh.partition { it.validate() }
        if (invoked) {
            return invalid
        }

        val annotatedFreshFunction = valid.filterIsInstance<KSFunctionDeclaration>().singleOrNull()

        if (annotatedFreshFunction == null) {
            invoked = true
            return invalid
        }

        val allCoreFiles = resolver.getAllFiles().filter {
            it.packageName == annotatedFreshFunction.containingFile?.packageName
        }
        annotatedFreshFunction.accept(FreshVisitor(allCoreFiles.toList().toTypedArray(), logger), Unit)

        invoked = true
        return invalid
    }

    @Suppress("unused")
    inner class FreshVisitor(val allCoreFiles: Array<KSFile>, private val logger: KSPLogger) : KSVisitorVoid() {
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val packageName = function.packageName.asString()
            val generatedFileName = "Fresh"

            val fileSpec = FileSpec.builder(packageName, generatedFileName).apply {
                for (i in 2..MAX_NUMBER_OF_PARAMETERS) {
                    val freshFewVariablesFunction = generateCreatingFewFreshVariables(function, i)
                    addFunction(freshFewVariablesFunction)
                }
            }.build()

            fileSpec.writeTo(codeGenerator, aggregating = true)
        }

        private fun generateCreatingFewFreshVariables(
            originalFreshFunction: KSFunctionDeclaration,
            numberOfParameters: Int
        ): FunSpec {
            require(numberOfParameters > 0) {
                "Cannot generate fresh for $numberOfParameters variables"
            }

            val typeParametersWithBounds = (1..numberOfParameters).map {
                val name = "T$it"
                val typeVariable = TypeVariableName.invoke(name)
                val upperBound = /*TERM_TYPE.asClassName()*/
                    ClassName(packageName = "org.klogic.core", "Term").parameterizedBy(typeVariable)

                typeVariable.copy(bounds = listOf(upperBound)) to upperBound
            }
            val typeParameters = typeParametersWithBounds.map { it.first }
            val typeBounds = typeParametersWithBounds.map { it.second }

            val parameterType =
                LambdaTypeName.get(receiver = null, parameters = typeBounds.toTypedArray(), returnType = GOAL_TYPE_NAME)
            val functionParameter = ParameterSpec.builder("f", parameterType).build()

            return FunSpec.builder(originalFreshFunction.simpleName.asString()).apply {
                val body = "${functionParameter.name}(${typeParameters.joinToString(", ") { "st.freshTypedVar()" }})"

                addTypeVariables(typeParameters)
                    .addParameter(functionParameter)
                    .returns(GOAL_TYPE_NAME)
                    .addCode("""
                        | return delay {
                        |   { st: State -> $body(st) }
                        | }
                    """.trimMargin())
//                    .beginControlFlow("return delay")
//                    .nextControlFlow("st: State -> $body")
//                    .endControlFlow()
//                    .endControlFlow()
            }.build()
        }
    }

    companion object {
        private val GOAL_TYPE_NAME: ClassName = ClassName("org.klogic.core", "Goal")
//        private val TERM_TYPE: KClass<Term<*>> = Term::class
//        private val STATE_TYPE: KClass<State> = State::class

//        private val DELAY_METHOD_NAME = ::delay.name
//        private val FRESH_TYPED_VAR_METHOD_NAME = State::freshTypedVar.name
    }
}

class FreshProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): FreshProcessor =
        FreshProcessor(environment.codeGenerator, environment.logger)
}
