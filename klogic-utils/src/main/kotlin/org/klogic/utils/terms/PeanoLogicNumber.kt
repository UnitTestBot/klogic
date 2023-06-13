@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.terms

import org.klogic.core.*
import org.klogic.utils.terms.LogicList.Companion.logicListOf
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
        fun succ(number: PeanoTerm): NextNaturalNumber = NextNaturalNumber(number)
    }
}

private typealias PeanoTerm = Term<PeanoLogicNumber>

object ZeroNaturalNumber : PeanoLogicNumber() {
    val Z: ZeroNaturalNumber = ZeroNaturalNumber

    override val subtreesToUnify: Array<*> = emptyArray<Any?>()

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<PeanoLogicNumber> = this

    override fun toInt(): Int = 0

    override fun toString(): String = "0"
}

data class NextNaturalNumber(val previous: PeanoTerm) : PeanoLogicNumber() {
    override val subtreesToUnify: Array<*>
        get() = arrayOf(previous)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<PeanoLogicNumber> =
        NextNaturalNumber(subtrees.single() as PeanoTerm)

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

context(RelationalContext)
fun addᴼ(x: PeanoTerm, y: PeanoTerm, result: PeanoTerm): Goal = conde(
    (x `===` Z) and (result `===` y),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
        (x `===` succ(a)) and (result `===` succ(b)) and addᴼ(a, y, b)
    }
)

context(RelationalContext)
fun mulᴼ(x: PeanoTerm, y: PeanoTerm, result: PeanoTerm): Goal = conde(
    (x `===` Z) and (result `===` Z),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
        (x `===` succ(a)) and addᴼ(y, b, result) and mulᴼ(a, y, b)
    }
)

context(RelationalContext)
fun lessThanOrEqualᴼ(x: PeanoTerm, y: PeanoTerm, result: Term<LogicBool>): Goal = conde(
    (x `===` Z) and (result `===` truᴼ),
    (x ineq Z) and (y `===` Z) and (result `===` falsᴼ),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and lessThanOrEqualᴼ(x1, y1, result)
    }
)

context(RelationalContext)
fun greaterThanOrEqualᴼ(x: PeanoTerm, y: PeanoTerm, result: Term<LogicBool>): Goal = lessThanOrEqualᴼ(y, x, result)

context(RelationalContext)
fun greaterThanᴼ(x: PeanoTerm, y: PeanoTerm, result: Term<LogicBool>): Goal = conde(
    (x ineq Z) and (y `===` Z) and (result `===` truᴼ),
    (x `===` Z) and (result `===` falsᴼ),
    freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { x1, y1 ->
        (x `===` succ(x1)) and (y `===` succ(y1)) and greaterThanᴼ(x1, y1, result)
    }
)

context(RelationalContext)
fun lessThanᴼ(x: PeanoTerm, y: PeanoTerm, result: Term<LogicBool>): Goal = greaterThanᴼ(y, x, result)

context(RelationalContext)
fun minMaxᴼ(a: PeanoTerm, b: PeanoTerm, min: PeanoTerm, max: PeanoTerm): Goal = conde(
    (min `===` a) and (max `===` b) and lessThanOrEqualᴼ(a, b, truᴼ),
    (min `===` b) and (max `===` a) and greaterThanᴼ(a, b, truᴼ),
)

context(RelationalContext)
fun smallestᴼ(
    nonEmptyList: Term<LogicList<PeanoLogicNumber>>,
    smallestElement: PeanoTerm,
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

context(RelationalContext)
fun sortᴼ(unsortedList: Term<LogicList<PeanoLogicNumber>>, sortedList: Term<LogicList<PeanoLogicNumber>>): Goal = conde(
    (unsortedList `===` nilLogicList()) and (sortedList `===` nilLogicList()),
    freshTypedVars<PeanoLogicNumber, LogicList<PeanoLogicNumber>, LogicList<PeanoLogicNumber>> { smallest, unsortedOthers, sortedTail ->
        (sortedList `===` smallest + sortedTail) and sortᴼ(unsortedOthers, sortedTail) and smallestᴼ(unsortedList, smallest, unsortedOthers)
    }
)
