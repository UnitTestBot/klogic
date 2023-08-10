@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.klogic.core.*
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitOne
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.numberZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.Symbol.Companion.toSymbol

typealias Digit = Symbol

private typealias OlegTerm = Term<OlegLogicNumber>
private typealias DigitTerm = Term<Digit>

/**
 * Logic number represented by list of [Digit]s, from the last digit to the first.
 */
data class OlegLogicNumber(val digits: Term<LogicList<Digit>>) : UnaryTerm<OlegLogicNumber, Term<LogicList<Digit>>>() {
    override val value: Term<LogicList<Digit>>
        get() = digits
    override val constructor: (Term<LogicList<Digit>>) -> OlegLogicNumber
        get() = ::OlegLogicNumber

    operator fun get(index: Int): DigitTerm = (digits as LogicList<Digit>)[index]

    fun toUInt(): UInt = digits.asReified().toList().foldIndexed(0u) { i, accumulator, current ->
        accumulator or (current.asReified().toString().toUInt() shl i)
    }

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
internal val numberTwo: OlegLogicNumber = (digitZero + digitOne.toLogicList()).toOlegLogicNumber()
internal val numberThree: OlegLogicNumber = (digitOne + digitOne.toLogicList()).toOlegLogicNumber()

/**
 * Checks whether the [number] is positive.
 */
context(RelationalContext)
fun posᴼ(number: OlegTerm): Goal = freshTypedVars<Digit, LogicList<Digit>> { head, tail ->
    number `===` (head + tail).toOlegLogicNumber()
}

/**
 * Checks whether [number] is greater than 1.
 */
context(RelationalContext)
fun greaterThan1ᴼ(number: OlegTerm): Goal =
    freshTypedVars<Digit, Digit, LogicList<Digit>> { head, tailHead, tail ->
        number `===` (head + (tailHead + tail)).toOlegLogicNumber()
    }

/**
 * Satisfies [b] + [x] + [y] = [r] + 2 * [c]
 */
context(RelationalContext)
fun fullAdderᴼ(b: DigitTerm, x: DigitTerm, y: DigitTerm, r: DigitTerm, c: DigitTerm): Goal = conde(
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
context(RelationalContext)
fun adderᴼ(d: DigitTerm, n: OlegTerm, m: OlegTerm, r: OlegTerm): Goal =
    { st ->
        conde(
            (digitZero `===` d) and (m `===` numberZero) and (n `===` r),
            (digitZero `===` d) and (n `===` numberZero) and (m `===` r) and posᴼ(m),
            (digitOne `===` d) and (m `===` numberZero) and adderᴼ(digitZero, n, numberOne, r),
            (digitOne `===` d) and (n `===` numberZero) and posᴼ(m) and adderᴼ(digitZero, numberOne, m, r),
            and(
                (n `===` numberOne),
                (m `===` numberOne),
                freshTypedVars<Digit, Digit> { a, c ->
                    and(
                        logicListOf(a, c).toOlegLogicNumber() `===` r,
                        fullAdderᴼ(d, digitOne, digitOne, a, c)
                    )
                }
            ),
            (n `===` numberOne) and genAdderᴼ(d, n, m, r),
            (m `===` numberOne) and greaterThan1ᴼ(n) and greaterThan1ᴼ(r) and adderᴼ(d, numberOne, n, r),
            greaterThan1ᴼ(n) and genAdderᴼ(d, n, m, r)
        )(st)
    }

/**
 * Satisfies [d] + [n] + [m] = [r], provided that [m] and [r] are greater than 1 and [n] is positive.
 */
context(RelationalContext)
fun genAdderᴼ(d: DigitTerm, n: OlegTerm, m: OlegTerm, r: OlegTerm): Goal =
    freshTypedVars<Digit, Digit, Digit, Digit, LogicList<Digit>, LogicList<Digit>, LogicList<Digit>> { a, b, c, e, x, y, z ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()
        val numberZ = z.toOlegLogicNumber()

        and(
            ((a + x).toOlegLogicNumber() `===` n),
            ((b + y).toOlegLogicNumber() `===` m),
            posᴼ(numberY),
            ((c + z).toOlegLogicNumber() `===` r),
            posᴼ(numberZ),
            (fullAdderᴼ(d, a, b, c, e)),
            adderᴼ(e, numberX, numberY, numberZ)
        )
    }

context(RelationalContext)
fun plusᴼ(n: OlegTerm, m: OlegTerm, result: OlegTerm): Goal = adderᴼ(digitZero, n, m, result)

context(RelationalContext)
fun minusᴼ(n: OlegTerm, m: OlegTerm, result: OlegTerm): Goal = plusᴼ(m, result, n)

// `=lo`
context(RelationalContext)
fun hasTheSameLengthᴼ(n: OlegTerm, m: OlegTerm): Goal = conde(
    (n `===` numberZero) and (m `===` numberZero),
    (n `===` numberOne) and (m `===` numberOne),
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { a, x, b, y ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()

        and(
            ((a + x).toOlegLogicNumber() `===` n),
            posᴼ(numberX),
            ((b + y).toOlegLogicNumber() `===` m),
            posᴼ(numberY),
            hasTheSameLengthᴼ(numberX, numberY)
        )
    }
)

// `<lo`
context(RelationalContext)
fun hasTheSmallerLengthᴼ(n: OlegTerm, m: OlegTerm): Goal = conde(
    (n `===` numberZero) and posᴼ(m),
    (n `===` numberOne) and greaterThan1ᴼ(m),
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { a, x, b, y ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()

        and(
            ((a + x).toOlegLogicNumber() `===` n),
            posᴼ(numberX),
            ((b + y).toOlegLogicNumber() `===` m),
            posᴼ(numberY),
            hasTheSmallerLengthᴼ(numberX, numberY)
        )
    }
)

// `<=lo`
context(RelationalContext)
fun hasTheSmallerOrSameLengthᴼ(n: OlegTerm, m: OlegTerm): Goal = conde(
    hasTheSameLengthᴼ(n, m),
    hasTheSmallerLengthᴼ(n, m)
)

// `<o`
context(RelationalContext)
fun lessThanᴼ(n: OlegTerm, m: OlegTerm): Goal = conde(
    hasTheSmallerLengthᴼ(n, m),
    hasTheSameLengthᴼ(n, m) and freshTypedVars<OlegLogicNumber> { x -> posᴼ(x) and plusᴼ(n, x, m) }
)

// `<=o`
context(RelationalContext)
fun lessThanOrEqualᴼ(n: OlegTerm, m: OlegTerm): Goal = conde(
    n `===` m,
    lessThanᴼ(n, m)
)

context(RelationalContext)
fun boundMulᴼ(q: OlegTerm, p: OlegTerm, n: OlegTerm, m: OlegTerm): Goal = conde(
    (q `===` numberZero) and posᴼ(p),
    freshTypedVars<Digit, Digit, Digit, Digit, LogicList<Digit>, LogicList<Digit>, LogicList<Digit>> { a0, a1, a2, a3, x, y, z ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()
        val numberZ = z.toOlegLogicNumber()

        and(
            q `===` (a0 + x).toOlegLogicNumber(),
            p `===` (a1 + y).toOlegLogicNumber(),
            conde(
                and(
                    n `===` numberZero,
                    m `===` (a2 + z).toOlegLogicNumber(),
                    boundMulᴼ(numberX, numberY, numberZ, numberZero)
                ),
                and(
                    n `===` (a3 + z).toOlegLogicNumber(),
                    boundMulᴼ(numberX, numberY, numberZ, m)
                )
            )
        )
    }
)

context(RelationalContext)
fun mulᴼ(n: OlegTerm, m: OlegTerm, p: OlegTerm): Goal = conde(
    (n `===` numberZero) and (p `===` numberZero),
    posᴼ(n) and (m `===` numberZero) and (p `===` numberZero),
    (n `===` numberOne) and posᴼ(m) and (m `===` p),
    greaterThan1ᴼ(n) and (m `===` numberOne) and (n `===` p),
    freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, z ->
        val numberX = x.toOlegLogicNumber()
        val numberZ = z.toOlegLogicNumber()

        and(
            (n `===` (digitZero + x).toOlegLogicNumber()),
            posᴼ(numberX),
            (p `===` (digitZero + z).toOlegLogicNumber()),
            posᴼ(numberZ),
            greaterThan1ᴼ(m),
            mulᴼ(numberX, m, numberZ)
        )
    },
    freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, y ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()

        and(
            (n `===` (digitOne + x).toOlegLogicNumber()),
            posᴼ(numberX),
            (m `===` (digitZero + y).toOlegLogicNumber()),
            posᴼ(numberY),
            mulᴼ(m, n, p)
        )
    },
    freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, y ->
        val numberX = x.toOlegLogicNumber()
        val numberY = y.toOlegLogicNumber()

        and(
            (n `===` (digitOne + x).toOlegLogicNumber()),
            posᴼ(numberX),
            (m `===` (digitOne + y).toOlegLogicNumber()),
            posᴼ(numberY),
            oddMulᴼ(numberX, n, m, p)
        )
    }
)

context(RelationalContext)
fun oddMulᴼ(x: OlegTerm, n: OlegTerm, m: OlegTerm, p: OlegTerm): Goal = freshTypedVars<LogicList<Digit>> { q ->
    val number = q.toOlegLogicNumber()

    and(
        boundMulᴼ(number, p, n, m),
        mulᴼ(x, m, number),
        plusᴼ((digitZero + q).toOlegLogicNumber(), m, p)
    )
}


context(RelationalContext)
fun repeatedMulᴼ(n: OlegTerm, q: OlegTerm, nq: OlegTerm): Goal = conde(
    posᴼ(n) and (q `===` numberZero) and (nq `===` numberOne),
    (q `===` numberOne) and (n `===` nq),
    and(
        greaterThan1ᴼ(q),
        freshTypedVars<OlegLogicNumber, OlegLogicNumber> { q1, nq1 ->
            and(
                plusᴼ(q1, numberOne, q),
                repeatedMulᴼ(n, q1, nq1),
                mulᴼ(nq1, n, nq)
            )
        }
    )
)

/**
 * Satisfies n = m * q + r, with 0 <= r < m.
 */
context(RelationalContext)
fun divᴼ(n: OlegTerm, m: OlegTerm, q: OlegTerm, r: OlegTerm): Goal = conde(
    (r `===` n) and (q `===` numberZero) and lessThanᴼ(n, m),
    and(
        (q `===` numberOne) and hasTheSameLengthᴼ(n, m) and plusᴼ(r, m, n),
        lessThanᴼ(r, m)
    ),
    and(
        hasTheSmallerLengthᴼ(m, n),
        lessThanᴼ(r, m),
        posᴼ(q),
        freshTypedVars<LogicList<Digit>, OlegLogicNumber, LogicList<Digit>, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, LogicList<Digit>> { nh, nl, qh, ql, qlm, qlmr, rr, rh ->
            val numberNh = nh.toOlegLogicNumber()
            val numberQh = qh.toOlegLogicNumber()
            val numberRh = rh.toOlegLogicNumber()

            and(
                splitᴼ(n, r, nl, nh),
                splitᴼ(q, r, ql, qh),
                conde(
                    and(
                        numberNh `===` numberZero,
                        numberQh `===` numberZero,
                        minusᴼ(nl, r, qlm),
                        mulᴼ(ql, m, qlm)
                    ),
                    and(
                        posᴼ(numberNh),
                        mulᴼ(ql, m, qlm),
                        plusᴼ(qlm, r, qlmr),
                        minusᴼ(qlmr, nl, rr),
                        splitᴼ(rr, r, numberZero, rh),
                        divᴼ(numberNh, m, numberQh, numberRh)
                    )
                )
            )
        }
    )
)

/**
 *  Splits a binary numeral at a given length:
 * (split o n r l h) holds if n = 2^{s+1} · l + h where s = ∥r∥ and h < 2^{s+1}.
 */
context(RelationalContext)
fun splitᴼ(n: OlegTerm, r: OlegTerm, l: OlegTerm, h: Term<LogicList<Digit>>): Goal = conde(
    (n `===` numberZero) and (h `===` nilLogicList()) and (l `===` numberZero),
    freshTypedVars<Digit, LogicList<Digit>> { b, n1 ->
        val concatenation = b + n1

        and(
            n `===` (digitZero + concatenation).toOlegLogicNumber(),
            r `===` numberZero,
            h `===` concatenation,
            l `===` numberZero
        )
    },
    freshTypedVars<LogicList<Digit>> { n1 ->
        and(
            n `===` (digitOne + n1).toOlegLogicNumber(),
            (r `===` numberZero),
            (n1 `===` h),
            (l `===` numberOne)
        )
    },
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { b, n1, a, r1 ->
        val concatenation = b + n1

        and(
            n `===` (digitZero + concatenation).toOlegLogicNumber(),
            r `===` (a + r1).toOlegLogicNumber(),
            l `===` numberZero,
            splitᴼ(concatenation.toOlegLogicNumber(), r1.toOlegLogicNumber(), numberZero, h)
        )
    },
    freshTypedVars<LogicList<Digit>, Digit, LogicList<Digit>> { n1, a, r1 ->
        and(
            n `===` (digitOne + n1).toOlegLogicNumber(),
            r `===` (a + r1).toOlegLogicNumber(),
            l `===` numberOne,
            splitᴼ(n1.toOlegLogicNumber(), r1.toOlegLogicNumber(), numberZero, h)
        )
    },
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>, LogicList<Digit>> { b, n1, a, r1, l1 ->
        val numberL1 = l1.toOlegLogicNumber()

        and(
            n `===` (b + n1).toOlegLogicNumber(),
            r `===` (a + r1).toOlegLogicNumber(),
            l `===` (b + l1).toOlegLogicNumber(),
            posᴼ(numberL1),
            splitᴼ(n1.toOlegLogicNumber(), r1.toOlegLogicNumber(), numberL1, h)
        )
    },
)

/**
 * Satisfies n = b ^ q + r, where 0 <= r <= n and q is the largest.
 */
context(RelationalContext)
@Suppress("NAME_SHADOWING")
fun logᴼ(n: OlegTerm, b: OlegTerm, q: OlegTerm, r: OlegTerm): Goal = conde(
    (n `===` numberOne) and posᴼ(b) and (q `===` numberZero) and (r `===` numberZero),
    (q `===` numberZero) and lessThanᴼ(n, b) and plusᴼ(r, numberOne, n),
    (q `===` numberOne) and greaterThan1ᴼ(b) and hasTheSameLengthᴼ(n, b) and plusᴼ(r, b, n),
    (q `===` numberOne) and posᴼ(q) and plusᴼ(r, numberOne, n),
    (b `===` numberZero) and posᴼ(q) and (r `===` n),
    (b `===` numberTwo) and freshTypedVars<Digit, Digit, LogicList<Digit>> { a, ad, dd ->
        val numberDd = dd.toOlegLogicNumber()

        and(
            posᴼ(numberDd),
            n `===` (a + (ad + dd)).toOlegLogicNumber(),
            exp2ᴼ(n, nilLogicList(), q),
            freshTypedVars<LogicList<Digit>> { s ->
                splitᴼ(n, numberDd, r, s)
            }
        )
    },
    and(
        freshTypedVars<Digit, Digit, Digit, LogicList<Digit>> { a, ad, add, ddd ->
            conde(
                b `===` numberThree,
                b `===` (a + (ad + (add + ddd))).toOlegLogicNumber()
            )
        },
        hasTheSmallerLengthᴼ(b, n),
        freshTypedVars<OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber> { bw1, bw, nw, nw1, ql1, ql, s ->
            and(
                exp2ᴼ(b, nilLogicList(), bw1),
                plusᴼ(bw1, numberOne, bw),
                hasTheSmallerLengthᴼ(q, n),
                freshTypedVars<OlegLogicNumber, OlegLogicNumber> { q1, bwq1 ->
                    and(
                        plusᴼ(q, numberOne, q1),
                        mulᴼ(bw, q1, bwq1),
                        lessThanᴼ(nw1, bwq1)
                    )
                },
                exp2ᴼ(n, nilLogicList(), nw1),
                plusᴼ(nw1, numberOne, nw),
                divᴼ(nw, bw, ql1, s),
                plusᴼ(ql, numberOne, ql1),
                hasTheSmallerOrSameLengthᴼ(ql, q),
                freshTypedVars<OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber, OlegLogicNumber> { bql, qh, s, qdh, qd ->
                    and(
                        repeatedMulᴼ(b, ql, bql),
                        divᴼ(nw, bw1, qh, s),
                        plusᴼ(ql, qdh, qh),
                        plusᴼ(ql, qd, q),
                        lessThanOrEqualᴼ(qd, qdh),
                        freshTypedVars<OlegLogicNumber, OlegLogicNumber, OlegLogicNumber> { bqd, bq1, bq ->
                            and(
                                repeatedMulᴼ(b, qd, bqd),
                                mulᴼ(bql, bqd, bq),
                                mulᴼ(b, bq, bq1),
                                plusᴼ(bq, r, n),
                                lessThanᴼ(n, bq1)
                            )
                        }
                    )
                }
            )
        }
    )
)

context(RelationalContext)
fun exp2ᴼ(n: OlegTerm, b: Term<LogicList<Digit>>, q: OlegTerm): Goal {
    val numberB = b.toOlegLogicNumber()

    return conde(
        (n `===` numberOne) and (q `===` numberZero),
        and(
            greaterThan1ᴼ(n) and (q `===` numberOne),
            freshTypedVars<OlegLogicNumber> { s ->
                splitᴼ(n, numberB, s, logicListOf(digitOne))
            }
        ),
        freshTypedVars<LogicList<Digit>, LogicList<Digit>> { q1, b2 ->
            val numberQ1 = q1.toOlegLogicNumber()

            and(
                q `===` (digitZero + q1).toOlegLogicNumber(),
                posᴼ(numberQ1),
                hasTheSmallerLengthᴼ(numberB, n),
                appendᴼ(b, digitOne + b, b2),
                exp2ᴼ(n, b2, numberQ1)
            )
        },
        freshTypedVars<LogicList<Digit>, LogicList<Digit>, LogicList<Digit>, OlegLogicNumber> { q1, nh, b2, s ->
            val numberQ1 = q1.toOlegLogicNumber()
            val numberNh = nh.toOlegLogicNumber()

            and(
                q `===` (digitOne + q1).toOlegLogicNumber(),
                posᴼ(numberQ1),
                posᴼ(numberNh),
                splitᴼ(n, numberB, s, nh),
                appendᴼ(b, digitOne + b, b2),
                exp2ᴼ(numberNh, b2, numberQ1)
            )
        }
    )
}

context(RelationalContext)
fun expᴼ(b: OlegTerm, q: OlegTerm, n: OlegTerm): Goal = logᴼ(n, b, q, numberZero)
