@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.and
import org.klogic.core.conde
import org.klogic.core.delay
import org.klogic.core.freshTypedVars
import org.klogic.utils.terms.Cons.Companion.logicListOf
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitOne
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.numberZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.Symbol.Companion.toSymbol

typealias Digit = Symbol

/**
 * Logic number represented by list of [Digit]s, from the last digit to the first.
 */
data class OlegLogicNumber(val digits: Term<LogicList<Digit>>) : CustomTerm<OlegLogicNumber> {
    override val subtreesToUnify: Sequence<*>
        get() = sequenceOf(digits)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<OlegLogicNumber> =
        OlegLogicNumber(subtrees.single() as Term<LogicList<Digit>>)

    operator fun get(index: Int): Term<Digit> = (digits as LogicList<Digit>)[index]

    override fun toString(): String = digits.toString()

    companion object {
        internal val digitZero: Digit = "0".toSymbol()
        internal val digitOne: Digit = "1".toSymbol()

        val numberZero: OlegLogicNumber = nilLogicList<Digit>().toOlegLogicNumber()

        fun UInt.toOlegLogicNumber(): OlegLogicNumber = OlegLogicNumber(toLogicList())

        fun Term<LogicList<Digit>>.toOlegLogicNumber(): OlegLogicNumber = OlegLogicNumber(this)

        private fun UInt.toLogicList(): LogicList<Digit> =
            when {
                this == 0u -> nilLogicList()
                this % 2u == 0u -> digitZero + (this / 2u).toLogicList()
                else -> digitOne + (this / 2u).toLogicList()
            }
    }
}

internal val numberOne: OlegLogicNumber = digitOne.toLogicList().toOlegLogicNumber()

/**
 * Checks whether the [number] is positive.
 */
fun posᴼ(number: Term<OlegLogicNumber>): Goal = freshTypedVars<Digit, LogicList<Digit>> { head, tail ->
    number `===` (head + tail).toOlegLogicNumber()
}

/**
 * Checks whether [number] is greater than 1.
 */
fun greaterThen1ᴼ(number: Term<OlegLogicNumber>): Goal =
    freshTypedVars<Digit, Digit, LogicList<Digit>> { head, tailHead, tail ->
        number `===` (head + (tailHead + tail)).toOlegLogicNumber()
    }

/**
 * Satisfies [b] + [x] + [y] = [r] + 2 * [c]
 */
fun fullAdderᴼ(b: Term<Digit>, x: Term<Digit>, y: Term<Digit>, r: Term<Digit>, c: Term<Digit>): Goal = conde(
    (digitZero `===` b) and (digitZero `===` x) and (digitZero `===` y) and (digitZero `===` r) and (digitZero `===` c),
    (digitOne `===` b) and (digitZero `===` x) and (digitZero `===` y) and (digitOne `===` r) and (digitZero `===` c),
    (digitZero `===` b) and (digitOne `===` x) and (digitZero `===` y) and (digitOne `===` r) and (digitZero `===` c),
    (digitOne `===` b) and (digitOne `===` x) and (digitZero `===` y) and (digitZero `===` r) and (digitOne `===` c),
    (digitZero `===` b) and (digitZero `===` x) and (digitOne `===` y) and (digitOne `===` r) and (digitZero `===` c),
    (digitOne `===` b) and (digitZero `===` x) and (digitOne `===` y) and (digitZero `===` r) and (digitOne `===` c),
    (digitZero `===` b) and (digitOne `===` x) and (digitOne `===` y) and (digitZero `===` r) and (digitOne `===` c),
    (digitOne `===` b) and (digitOne `===` x) and (digitOne `===` y) and (digitOne `===` r) and (digitOne `===` c)
)

/**
 * Adds a carry-in bit [d] to arbitrarily large numbers [n] and [m] to produce a number [r].
 */
fun adderᴼ(d: Term<Digit>, n: Term<OlegLogicNumber>, m: Term<OlegLogicNumber>, r: Term<OlegLogicNumber>): Goal = conde(
    (digitZero `===` d) and (m `===` numberZero) and (n `===` r),
    (digitZero `===` d) and (n `===` numberZero) and (m `===` r) and posᴼ(m),
    (digitOne `===` d) and (m `===` numberZero) and delay { adderᴼ(digitZero, n, numberOne, r) },
    (digitOne `===` d) and (n `===` numberZero) and posᴼ(m) and delay { adderᴼ(digitZero, m, numberOne, r) },
    and(
        (n `===` numberOne), (m `===` numberOne), freshTypedVars<Digit, Digit> { a, c ->
            (logicListOf(a, c).toOlegLogicNumber() `===` r) and fullAdderᴼ(d, digitOne, digitOne, a, c)
        }
    ),
    (n `===` numberOne) and genAdderᴼ(d, n, m, r),
    (m `===` numberOne) and greaterThen1ᴼ(n) and greaterThen1ᴼ(r) and delay { adderᴼ(d, numberOne, n, r) },
    greaterThen1ᴼ(n) and genAdderᴼ(d, n, m, r)
)

/**
 * Satisfies [d] + [n] + [m] = [r], provided that [m] and [r] are greater than 1 and [n] is positive.
 */
fun genAdderᴼ(d: Term<Digit>, n: Term<OlegLogicNumber>, m: Term<OlegLogicNumber>, r: Term<OlegLogicNumber>): Goal =
    freshTypedVars<Digit, Digit, Digit, Digit, LogicList<Digit>, LogicList<Digit>, LogicList<Digit>> { a, b, c, e, x, y, z ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()
        val numberZ = z.toOlegLogicNumber()

        ((a + x).toOlegLogicNumber() `===` n) and
                ((b + y).toOlegLogicNumber() `===` m) and
                posᴼ(numberY) and
                ((c + z).toOlegLogicNumber() `===` r) and
                posᴼ(numberZ) and
                (fullAdderᴼ(d, a, b, c, e)) and
                (adderᴼ(e, numberX, numberY, numberZ))
    }

fun plusᴼ(n: Term<OlegLogicNumber>, m: Term<OlegLogicNumber>, k: Term<OlegLogicNumber>): Goal =
    adderᴼ(digitZero, n, m, k)

fun minusᴼ(n: Term<OlegLogicNumber>, m: Term<OlegLogicNumber>, k: Term<OlegLogicNumber>): Goal = plusᴼ(m, k, n)
