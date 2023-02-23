@file:Suppress("ClassName", "NonAsciiCharacters", "ObjectPropertyName")

package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.and
import org.klogic.core.conde
import org.klogic.utils.terms.LogicFalsᴼ.falsᴼ
import org.klogic.utils.terms.LogicTruᴼ.truᴼ

sealed class LogicBool : CustomTerm<LogicBool> {
    abstract fun toBool(): Boolean

    override fun toString(): String = toBool().toString()

    companion object {
        fun Boolean.toLogicBool(): LogicBool = if (this) LogicFalsᴼ else LogicTruᴼ
    }
}

object LogicFalsᴼ : LogicBool() {
    val falsᴼ: LogicFalsᴼ = this

    override val subtreesToUnify: Sequence<*> = emptySequence<Any?>()

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicBool> = this

    override fun isUnifiableWith(other: CustomTerm<LogicBool>): Boolean = other is LogicFalsᴼ

    override fun toBool(): Boolean = false
}

object LogicTruᴼ : LogicBool() {
    val truᴼ: LogicTruᴼ = this

    override val subtreesToUnify: Sequence<*> = emptySequence<Any?>()

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicBool> = this

    override fun isUnifiableWith(other: CustomTerm<LogicBool>): Boolean = other is LogicTruᴼ

    override fun toBool(): Boolean = true
}

fun notᴼ(x: Term<LogicBool>, y: Term<LogicBool>): Goal = conde(
    (x `===` falsᴼ) and (y `===` truᴼ),
    (y `===` falsᴼ) and (x `===` truᴼ),
)

fun orᴼ(x: Term<LogicBool>, y: Term<LogicBool>, z: Term<LogicBool>): Goal = conde(
    (x `===` falsᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` falsᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
    (x `===` truᴼ) and (y `===` falsᴼ) and (z `===` truᴼ),
    (x `===` truᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
)

fun andᴼ(x: Term<LogicBool>, y: Term<LogicBool>, z: Term<LogicBool>): Goal = conde(
    (x `===` falsᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` falsᴼ) and (y `===` truᴼ) and (z `===` falsᴼ),
    (x `===` truᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` truᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
)
