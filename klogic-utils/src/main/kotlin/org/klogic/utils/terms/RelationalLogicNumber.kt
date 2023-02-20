@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.and
import org.klogic.core.conde
import org.klogic.core.freshTypedVars
import org.klogic.utils.terms.RelationalLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z

sealed class RelationalLogicNumber : CustomTerm<RelationalLogicNumber> {
    abstract fun toInt(): Int

    companion object {
        fun succ(number: Term<RelationalLogicNumber>): SuccNaturalNumber = SuccNaturalNumber(number)
    }
}

object ZeroNaturalNumber : RelationalLogicNumber() {
    val Z: ZeroNaturalNumber = ZeroNaturalNumber

    override val subtreesToUnify: Sequence<*>
        get() = emptySequence<Any?>()

    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<RelationalLogicNumber> = this

    override fun isUnifiableWith(other: CustomTerm<RelationalLogicNumber>): Boolean = other is ZeroNaturalNumber

    override fun toInt(): Int = 0

    override fun toString(): String = "0"
}

data class SuccNaturalNumber(val previous: Term<RelationalLogicNumber>) : RelationalLogicNumber() {
    override val subtreesToUnify: Sequence<*>
        get() = sequenceOf(previous)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<RelationalLogicNumber> =
        SuccNaturalNumber(subtrees.single() as Term<RelationalLogicNumber>)

    override fun isUnifiableWith(other: CustomTerm<RelationalLogicNumber>): Boolean = other is SuccNaturalNumber

    override fun toInt(): Int {
        require(previous !is Var) {
            "$this number is not reified"
        }

        return 1 + (previous as RelationalLogicNumber).toInt()
    }

    override fun toString(): String = "S($previous)"
}

val zero: RelationalLogicNumber = Z
val one: RelationalLogicNumber = succ(zero)
val two: RelationalLogicNumber = succ(one)

fun Int.toRelationalLogicNumber(): RelationalLogicNumber =
    if (this <= 0) Z else succ((this - 1).toRelationalLogicNumber())

operator fun Term<RelationalLogicNumber>.inc(): SuccNaturalNumber = succ(this)

fun addᴼ(x: Term<RelationalLogicNumber>, y: Term<RelationalLogicNumber>, z: Term<RelationalLogicNumber>): Goal = conde(
    (x `===` Z) and (z `===` y),
    freshTypedVars<RelationalLogicNumber, RelationalLogicNumber> { a, b ->
        (x `===` succ(a)) and (z `===` succ(b)) and addᴼ(a, y, b)
    }
)

fun mulᴼ(x: Term<RelationalLogicNumber>, y: Term<RelationalLogicNumber>, z: Term<RelationalLogicNumber>): Goal = conde(
    (x `===` Z) and (z `===` Z),
    freshTypedVars<RelationalLogicNumber, RelationalLogicNumber> { a, b ->
        (x `===` succ(a)) and addᴼ(y, b, z) and mulᴼ(a, y, b)
    }
)
