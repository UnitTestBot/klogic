@file:Suppress("ClassName", "NonAsciiCharacters", "ObjectPropertyName")

package org.klogic.utils.terms

import org.klogic.core.*
import org.klogic.utils.terms.LogicFalsᴼ.falsᴼ
import org.klogic.utils.terms.LogicTruᴼ.truᴼ

sealed class LogicBool : EmptyTerm<LogicBool>() {
    abstract fun toBool(): Boolean

    override fun toString(): String = toBool().toString()

    companion object {
        fun Boolean.toLogicBool(): LogicBool = if (this) LogicFalsᴼ else LogicTruᴼ
    }
}

private typealias BoolTerm = Term<LogicBool>

object LogicFalsᴼ : LogicBool() {
    val falsᴼ: LogicFalsᴼ = this

    override fun toBool(): Boolean = false
}

object LogicTruᴼ : LogicBool() {
    val truᴼ: LogicTruᴼ = this

    override fun toBool(): Boolean = true
}

context(RelationalContext)
fun notᴼ(x: Term<LogicBool>, y: Term<LogicBool>): Goal = conde(
    (x `===` falsᴼ) and (y `===` truᴼ),
    (y `===` falsᴼ) and (x `===` truᴼ),
)

context(RelationalContext)
fun orᴼ(x: BoolTerm, y: BoolTerm, z: BoolTerm): Goal = conde(
    (x `===` falsᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` falsᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
    (x `===` truᴼ) and (y `===` falsᴼ) and (z `===` truᴼ),
    (x `===` truᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
)

context(RelationalContext)
fun andᴼ(x: BoolTerm, y: BoolTerm, z: BoolTerm): Goal = conde(
    (x `===` falsᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` falsᴼ) and (y `===` truᴼ) and (z `===` falsᴼ),
    (x `===` truᴼ) and (y `===` falsᴼ) and (z `===` falsᴼ),
    (x `===` truᴼ) and (y `===` truᴼ) and (z `===` truᴼ),
)
