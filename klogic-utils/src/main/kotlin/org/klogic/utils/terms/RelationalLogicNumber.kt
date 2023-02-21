@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.and
import org.klogic.core.conde
import org.klogic.core.freshTypedVars
import org.klogic.utils.terms.Cons.Companion.logicListOf
import org.klogic.utils.terms.LogicFalsᴼ.falsᴼ
import org.klogic.utils.terms.LogicTruᴼ.truᴼ
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.RelationalLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z

/**
 * Represents classic Peano numbers.
 */
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

fun lessThanOrEqualᴼ(
    x: Term<RelationalLogicNumber>,
    y: Term<RelationalLogicNumber>,
    b: Term<LogicBool>
): Goal = conde(
    (x `===` Z) and (b `===` truᴼ),
    (x ineq Z) and (y `===` Z) and (b `===` falsᴼ),
    freshTypedVars<RelationalLogicNumber, RelationalLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and lessThanOrEqualᴼ(x1, y1, b)
    }
)

fun greaterThanOrEqualᴼ(
    x: Term<RelationalLogicNumber>,
    y: Term<RelationalLogicNumber>,
    b: Term<LogicBool>
): Goal = lessThanOrEqualᴼ(y, x, b)

fun greaterThanᴼ(
    x: Term<RelationalLogicNumber>,
    y: Term<RelationalLogicNumber>,
    b: Term<LogicBool>
): Goal = conde(
    (x ineq Z) and (y `===` Z) and (b `===` truᴼ),
    (x `===` Z) and (b `===` falsᴼ),
    freshTypedVars<RelationalLogicNumber, RelationalLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and greaterThanᴼ(x1, y1, b)
    }
)

fun lessThanᴼ(
    x: Term<RelationalLogicNumber>,
    y: Term<RelationalLogicNumber>,
    b: Term<LogicBool>
): Goal = greaterThanᴼ(y, x, b)

fun minMaxᴼ(
    a: Term<RelationalLogicNumber>,
    b: Term<RelationalLogicNumber>,
    min: Term<RelationalLogicNumber>,
    max: Term<RelationalLogicNumber>
): Goal = conde(
    (min `===` a) and (max `===` b) and lessThanOrEqualᴼ(a, b, truᴼ),
    (min `===` b) and (max `===` a) and greaterThanᴼ(a, b, truᴼ),
)

fun smallestᴼ(
    nonEmptyList: Term<LogicList<RelationalLogicNumber>>,
    smallestElement: Term<RelationalLogicNumber>,
    otherElements: Term<LogicList<RelationalLogicNumber>>
): Goal = conde(
    (nonEmptyList `===` logicListOf(smallestElement)) and (otherElements `===` nilLogicList()),
    freshTypedVars<RelationalLogicNumber, LogicList<RelationalLogicNumber>, RelationalLogicNumber, LogicList<RelationalLogicNumber>, RelationalLogicNumber> { head, tail, smallest1, tail1, max ->
        (otherElements `===` max + tail1) and
                (nonEmptyList `===` head + tail) and
                minMaxᴼ(head, smallest1, smallestElement, max) and
                smallestᴼ(tail, smallest1, tail1)
    }
)

fun sortᴼ(
    unsortedList: Term<LogicList<RelationalLogicNumber>>,
    sortedList: Term<LogicList<RelationalLogicNumber>>
): Goal = conde(
    (unsortedList `===` nilLogicList()) and (sortedList `===` nilLogicList()),
    freshTypedVars<RelationalLogicNumber, LogicList<RelationalLogicNumber>, LogicList<RelationalLogicNumber>> { smallest, unsortedOthers, sortedTail ->
        (sortedList `===` smallest + sortedTail) and sortᴼ(unsortedOthers, sortedTail) and smallestᴼ(unsortedList, smallest, unsortedOthers)
    }
)
