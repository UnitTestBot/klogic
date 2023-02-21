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
import org.klogic.utils.terms.PeanoLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z

/**
 * Represents classic Peano numbers.
 */
sealed class PeanoLogicNumber : CustomTerm<PeanoLogicNumber> {
    abstract fun toInt(): Int

    companion object {
        fun succ(number: Term<PeanoLogicNumber>): PositiveNaturalNumber = PositiveNaturalNumber(number)
    }
}

object ZeroNaturalNumber : PeanoLogicNumber() {
    val Z: ZeroNaturalNumber = ZeroNaturalNumber

    override val subtreesToUnify: Sequence<*>
        get() = emptySequence<Any?>()

    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<PeanoLogicNumber> = this

    override fun isUnifiableWith(other: CustomTerm<PeanoLogicNumber>): Boolean = other is ZeroNaturalNumber

    override fun toInt(): Int = 0

    override fun toString(): String = "0"
}

data class PositiveNaturalNumber(val previous: Term<PeanoLogicNumber>) : PeanoLogicNumber() {
    override val subtreesToUnify: Sequence<*>
        get() = sequenceOf(previous)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<PeanoLogicNumber> =
        PositiveNaturalNumber(subtrees.single() as Term<PeanoLogicNumber>)

    override fun isUnifiableWith(other: CustomTerm<PeanoLogicNumber>): Boolean = other is PositiveNaturalNumber

    override fun toInt(): Int {
        require(previous !is Var) {
            "$this number is not reified"
        }

        return 1 + previous.asReified().toInt()
    }

    override fun toString(): String = "S($previous)"
}

internal val zero: PeanoLogicNumber = Z
internal val one: PeanoLogicNumber = succ(zero)
internal val two: PeanoLogicNumber = succ(one)

fun Int.toPeanoLogicNumber(): PeanoLogicNumber = if (this <= 0) Z else succ((this - 1).toPeanoLogicNumber())

fun addᴼ(x: Term<PeanoLogicNumber>, y: Term<PeanoLogicNumber>, z: Term<PeanoLogicNumber>): Goal = conde(
    (x `===` Z) and (z `===` y),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
        (x `===` succ(a)) and (z `===` succ(b)) and addᴼ(a, y, b)
    }
)

fun mulᴼ(x: Term<PeanoLogicNumber>, y: Term<PeanoLogicNumber>, z: Term<PeanoLogicNumber>): Goal = conde(
    (x `===` Z) and (z `===` Z),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
        (x `===` succ(a)) and addᴼ(y, b, z) and mulᴼ(a, y, b)
    }
)

fun lessThanOrEqualᴼ(
    x: Term<PeanoLogicNumber>,
    y: Term<PeanoLogicNumber>,
    b: Term<LogicBool>
): Goal = conde(
    (x `===` Z) and (b `===` truᴼ),
    (x ineq Z) and (y `===` Z) and (b `===` falsᴼ),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and lessThanOrEqualᴼ(x1, y1, b)
    }
)

fun greaterThanOrEqualᴼ(
    x: Term<PeanoLogicNumber>,
    y: Term<PeanoLogicNumber>,
    b: Term<LogicBool>
): Goal = lessThanOrEqualᴼ(y, x, b)

fun greaterThanᴼ(
    x: Term<PeanoLogicNumber>,
    y: Term<PeanoLogicNumber>,
    b: Term<LogicBool>
): Goal = conde(
    (x ineq Z) and (y `===` Z) and (b `===` truᴼ),
    (x `===` Z) and (b `===` falsᴼ),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and greaterThanᴼ(x1, y1, b)
    }
)

fun lessThanᴼ(
    x: Term<PeanoLogicNumber>,
    y: Term<PeanoLogicNumber>,
    b: Term<LogicBool>
): Goal = greaterThanᴼ(y, x, b)

fun minMaxᴼ(
    a: Term<PeanoLogicNumber>,
    b: Term<PeanoLogicNumber>,
    min: Term<PeanoLogicNumber>,
    max: Term<PeanoLogicNumber>
): Goal = conde(
    (min `===` a) and (max `===` b) and lessThanOrEqualᴼ(a, b, truᴼ),
    (min `===` b) and (max `===` a) and greaterThanᴼ(a, b, truᴼ),
)

fun smallestᴼ(
    nonEmptyList: Term<LogicList<PeanoLogicNumber>>,
    smallestElement: Term<PeanoLogicNumber>,
    otherElements: Term<LogicList<PeanoLogicNumber>>
): Goal = conde(
    (nonEmptyList `===` logicListOf(smallestElement)) and (otherElements `===` nilLogicList()),
    freshTypedVars<PeanoLogicNumber, LogicList<PeanoLogicNumber>, PeanoLogicNumber, LogicList<PeanoLogicNumber>, PeanoLogicNumber> { head, tail, smallest1, tail1, max ->
        (otherElements `===` max + tail1) and
                (nonEmptyList `===` head + tail) and
                minMaxᴼ(head, smallest1, smallestElement, max) and
                smallestᴼ(tail, smallest1, tail1)
    }
)

fun sortᴼ(
    unsortedList: Term<LogicList<PeanoLogicNumber>>,
    sortedList: Term<LogicList<PeanoLogicNumber>>
): Goal = conde(
    (unsortedList `===` nilLogicList()) and (sortedList `===` nilLogicList()),
    freshTypedVars<PeanoLogicNumber, LogicList<PeanoLogicNumber>, LogicList<PeanoLogicNumber>> { smallest, unsortedOthers, sortedTail ->
        (sortedList `===` smallest + sortedTail) and sortᴼ(unsortedOthers, sortedTail) and smallestᴼ(unsortedList, smallest, unsortedOthers)
    }
)
