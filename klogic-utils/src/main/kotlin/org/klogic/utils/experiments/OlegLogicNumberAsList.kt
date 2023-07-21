@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.experiments

import org.klogic.core.*
import org.klogic.utils.terms.*
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.OlegLogicNumber.Companion.`1`
import org.klogic.utils.terms.OlegLogicNumber.Companion.`0`

/**
 * Logic number represented by list of [Digit]s, from the last digit to the first.
 */
typealias OlegLogicNumberAsList = LogicList<Digit>
private typealias OlegTermAsList = Term<OlegLogicNumberAsList>
private typealias DigitTerm = Term<Digit>

private val `()`: OlegLogicNumberAsList = nilLogicList()
internal val `(1)`: OlegLogicNumberAsList = `1`.toLogicList()
private val numberTwo: OlegLogicNumberAsList = (`0` + `1`.toLogicList())
private val numberThree: OlegLogicNumberAsList = (`1` + `1`.toLogicList())

private fun UInt.toLogicList(): LogicList<Digit> =
    when {
        this == 0u -> nilLogicList()
        this % 2u == 0u -> `0` + (this / 2u).toLogicList()
        else -> `1` + (this / 2u).toLogicList()
    }

fun UInt.toOlegLogicNumberAsList(): OlegLogicNumberAsList = toLogicList()

class LoggerListener(private val message: String) : UnificationListener {
    override fun onUnification(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?) {
        println(message)
    }
}

/**
 * Checks whether the [number] is positive.
 */
// Verified
context(RelationalContext)
fun posᴼ(number: OlegTermAsList): Goal {
    //println("poso($number)")

    return freshTypedVars<Digit, LogicList<Digit>> { head, tail ->
        //println("In poso: ($head + $tail) `===` $number")
        (head + tail) `===` number
    }
}

/**
 * Checks whether [number] is greater than 1.
 */
// Verified
context(RelationalContext)
fun greaterThan1ᴼ(number: OlegTermAsList): Goal {
    //println("greaterThan1ᴼ($number)")


    return freshTypedVars<Digit, Digit, LogicList<Digit>> { head, tailHead, tail ->
        //println("In gr1: ($head + ($tailHead + $tail)) `===` $number")
        (head + (tailHead + tail)) `===` number
    }
}

/**
 * Satisfies [b] + [x] + [y] = [r] + 2 * [c]
 */
// Verified
context(RelationalContext)
fun fullAdderᴼ(b: DigitTerm, x: DigitTerm, y: DigitTerm, r: DigitTerm, c: DigitTerm): Goal {
    //println("fullAdderᴼ($b, $x, $y, $r, $c)")


    return conde(
        and((`0` `===` b), (`0` `===` x), (`0` `===` y), (`0` `===` r), (`0` `===` c)),
        and((`1` `===` b), (`0` `===` x), (`0` `===` y), (`1` `===` r), (`0` `===` c)),
        and((`0` `===` b), (`1` `===` x), (`0` `===` y), (`1` `===` r), (`0` `===` c)),
        and((`1` `===` b), (`1` `===` x), (`0` `===` y), (`0` `===` r), (`1` `===` c)),
        and((`0` `===` b), (`0` `===` x), (`1` `===` y), (`1` `===` r), (`0` `===` c)),
        and((`1` `===` b), (`0` `===` x), (`1` `===` y), (`0` `===` r), (`1` `===` c)),
        and((`0` `===` b), (`1` `===` x), (`1` `===` y), (`0` `===` r), (`1` `===` c)),
        and((`1` `===` b), (`1` `===` x), (`1` `===` y), (`1` `===` r), (`1` `===` c)),
    )
}

/**
 * Adds a carry-in bit [d] to arbitrarily large numbers [n] and [m] to produce a number [r].
 */
// Verified
context(RelationalContext)
fun adderᴼ(d: DigitTerm, n: OlegTermAsList, m: OlegTermAsList, r: OlegTermAsList): Goal {
    //println("adderᴼ($n, $m, $r)")


    return conde(
        and(
            (`0` `===` d),
            (`()` `===` m),
            (n `===` r)
        ),
        and((`0` `===` d), (`()` `===` n), (m `===` r), posᴼ(m)),
        and((`1` `===` d), (`()` `===` m), delay { adderᴼ(`0`, n, `(1)`, r) }),
       and( (`1` `===` d), (`()` `===` n), posᴼ(m), delay { adderᴼ(`0`, `(1)`, m, r) }),
        and(
            (n `===` `(1)`), (m `===` `(1)`), freshTypedVars<Digit, Digit> { a, c ->
                and((a + c.toLogicList() `===` r), fullAdderᴼ(d, `1`, `1`, a, c))
            }
        ),
        and((n `===` `(1)`), genAdderᴼ(d, n, m, r)),
        and((m `===` `(1)`), greaterThan1ᴼ(n), greaterThan1ᴼ(r), delay { adderᴼ(d, `(1)`, n, r) }),
        and(greaterThan1ᴼ(n), genAdderᴼ(d, n, m, r))
    )
}

/**
 * Satisfies [d] + [n] + [m] = [r], provided that [m] and [r] are greater than 1 and [n] is positive.
 */
// Verified
context(RelationalContext)
fun genAdderᴼ(d: DigitTerm, n: OlegTermAsList, m: OlegTermAsList, r: OlegTermAsList): Goal =
    freshTypedVars<Digit, Digit, Digit, Digit, LogicList<Digit>, LogicList<Digit>, LogicList<Digit>> { a, b, c, e, x, y, z ->

        and(((a + x) `===` n),
                ((b + y) `===` m),
                posᴼ(y),
                ((c + z) `===` r),
                posᴼ(z),
                (fullAdderᴼ(d, a, b, c, e)),
                (adderᴼ(e, x, y, z)))
    }

// Verified
context(RelationalContext)
fun plusᴼ(n: OlegTermAsList, m: OlegTermAsList, k: OlegTermAsList): Goal = adderᴼ(`0`, n, m, k)

context(RelationalContext)
fun minusᴼ(n: OlegTermAsList, m: OlegTermAsList, result: OlegTermAsList): Goal = plusᴼ(m, result, n)

// `=lo`
context(RelationalContext)
fun hasTheSameLengthᴼ(n: OlegTermAsList, m: OlegTermAsList): Goal {
    //println("hasTheSameLengthᴼ($n, $m)")


    return conde(
        and((n `===` `()`), (m `===` `()`)),
        and((n `===` `(1)`), (m `===` `(1)`)),
        freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { a, x, b, y ->

            and(((a + x) `===` n), posᴼ(x),
                    ((b + y) `===` m), posᴼ(y),
                    hasTheSameLengthᴼ(x, y))
        }
    )
}

// `<lo`
context(RelationalContext)
fun hasTheSmallerLengthᴼ(n: OlegTermAsList, m: OlegTermAsList): Goal {
    //println("hasTheSmallerLengthᴼ($n, $m)")


    return conde(
        and((n `===` `()`), posᴼ(m)),
        and((n `===` `(1)`), greaterThan1ᴼ(m)),
        freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { a, x, b, y ->

            and(((a + x) `===` n), posᴼ(x),
                    ((b + y) `===` m), posᴼ(y),
                    hasTheSmallerLengthᴼ(x, y))
        }
    )
}

// `<=lo`
context(RelationalContext)
fun hasTheSmallerOrSameLengthᴼ(n: OlegTermAsList, m: OlegTermAsList): Goal {
    //println("hasTheSmallerOrSameLengthᴼ($n, $m)")

    return conde(
        hasTheSameLengthᴼ(n, m),
        hasTheSmallerLengthᴼ(n, m)
    )
}

// `<o`
context(RelationalContext)
fun lessThanᴼ(n: OlegTermAsList, m: OlegTermAsList): Goal {
    //println("lessThan($n, $m)")


    return conde(
        hasTheSmallerLengthᴼ(n, m),
        and(hasTheSameLengthᴼ(n, m), freshTypedVars<OlegLogicNumberAsList> { x -> and(posᴼ(x), plusᴼ(n, x, m)) })
    )
}

// `<=o`
context(RelationalContext)
fun lessThanOrEqualᴼ(n: OlegTermAsList, m: OlegTermAsList): Goal {
    //println("lessThanOrEqualᴼ($n, $m)")

    return conde(
        n `===` m,
        lessThanᴼ(n, m)
    )
}

// Verified
context(RelationalContext)
fun mulᴼ(n: OlegTermAsList, m: OlegTermAsList, p: OlegTermAsList): Goal {
    //println("mulo($n, $m, $p)")

    return conde(
        {
            //println("(n `===` numberZero) and (p `===` numberZero)")
            and((n `===` `()`), (p `===` `()`))
        }(),
        {
            //println("posᴼ(n) and (m `===` numberZero) and (p `===` numberZero)")
            and(posᴼ(n), (m `===` `()`), (p `===` `()`))
        }(),
        {
            //println("(n `===` numberOne) and posᴼ(m) and (m `===` p)")
            and((n `===` `(1)`), posᴼ(m), (m `===` p))
        }(),
        {
            //println("greaterThan1ᴼ(n) and (m `===` numberOne) and (n `===` p)")
            and(greaterThan1ᴼ(n), (m `===` `(1)`), (n `===` p))
        }(),
        freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, z ->

            and(
                (n `===` (`0` + x)), posᴼ(x),
                (p `===` (`0` + z)), posᴼ(z),
                greaterThan1ᴼ(m),
                mulᴼ(x, m, z)
            )
        },
        freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, y ->

            and(
                (n `===` (`1` + x)), posᴼ(x),
                (m `===` (`0` + y)), posᴼ(y),
                mulᴼ(m, n, p)
            )
        },
        freshTypedVars<LogicList<Digit>, LogicList<Digit>> { x, y ->

            and(
                (n `===` (`1` + x)), posᴼ(x),
                (m `===` (`1` + y)), posᴼ(y),
                oddMulᴼ(x, n, m, p)
            )
        }
    )
}

// Verified
context(RelationalContext)
fun oddMulᴼ(x: OlegTermAsList, n: OlegTermAsList, m: OlegTermAsList, p: OlegTermAsList): Goal = freshTypedVars<LogicList<Digit>> { q ->

    and(
        boundMulᴼ(q, p, n, m),
        mulᴼ(x, m, q),
        plusᴼ((`0` + q), m, p)
    )
}

// Verified
context(RelationalContext)
fun boundMulᴼ(q: OlegTermAsList, p: OlegTermAsList, n: OlegTermAsList, m: OlegTermAsList): Goal = conde(
    and((q `===` `()`), posᴼ(p)),
    freshTypedVars<Digit, Digit, Digit, Digit, LogicList<Digit>, LogicList<Digit>, LogicList<Digit>> { a0, a1, a2, a3, x, y, z ->

        and(
            q `===` (a0 + x),
            p `===` (a1 + y),
            conde(
                and(
                    n `===` `()`,
                    m `===` (a2 + z),
                    boundMulᴼ(x, y, z, `()`)
                ),
                and(
                    n `===` (a3 + z),
                    boundMulᴼ(x, y, z, m)
                )
            )
        )
    }
)

context(RelationalContext)
fun repeatedMulᴼ(n: OlegTermAsList, q: OlegTermAsList, nq: OlegTermAsList): Goal = conde(
    and(posᴼ(n), (q `===` `()`), (nq `===` `(1)`)),
    and((q `===` `(1)`), (n `===` nq)),
    and(
        greaterThan1ᴼ(q),
        freshTypedVars<OlegLogicNumberAsList, OlegLogicNumberAsList> { q1, nq1 ->
            and(
                plusᴼ(q1, `(1)`, q),
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
fun divᴼ(n: OlegTermAsList, m: OlegTermAsList, q: OlegTermAsList, r: OlegTermAsList): Goal = conde(
    and((r `===` n), (q `===` `()`), lessThanᴼ(n, m)),
    and(
        (q `===` `(1)`), hasTheSameLengthᴼ(n, m), plusᴼ(r, m, n),
        lessThanᴼ(r, m)
    ),
    and(
        hasTheSmallerLengthᴼ(m, n),
        lessThanᴼ(r, m),
        posᴼ(q),
        freshTypedVars<LogicList<Digit>, OlegLogicNumberAsList, LogicList<Digit>, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, LogicList<Digit>> { nh, nl, qh, ql, qlm, qlmr, rr, rh ->

            and(
                splitᴼ(n, r, nl, nh),
                splitᴼ(q, r, ql, qh),
                conde(
                    and(
                        nh `===` `()`,
                        qh `===` `()`,
                        minusᴼ(nl, r, qlm),
                        mulᴼ(ql, m, qlm)
                    ),
                    and(
                        posᴼ(nh),
                        mulᴼ(ql, m, qlm),
                        plusᴼ(qlm, r, qlmr),
                        minusᴼ(qlmr, nl, rr),
                        splitᴼ(rr, r, `()`, rh),
                        divᴼ(nh, m, qh, rh)
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
fun splitᴼ(n: OlegTermAsList, r: OlegTermAsList, l: OlegTermAsList, h: Term<LogicList<Digit>>): Goal = conde(
    (n `===` `()`) and (h `===` nilLogicList()) and (l `===` `()`),
    freshTypedVars<Digit, LogicList<Digit>> { b, n1 ->
        val concatenation = b + n1

        and(
            n `===` (`0` + concatenation),
            r `===` `()`,
            h `===` concatenation,
            l `===` `()`
        )
    },
    freshTypedVars<LogicList<Digit>> { n1 ->
        and(
            n `===` (`1` + n1),
            (r `===` `()`),
            (n1 `===` h),
            (l `===` `(1)`)
        )
    },
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>> { b, n1, a, r1 ->
        val concatenation = b + n1

        and(
            n `===` (`0` + concatenation),
            r `===` (a + r1),
            l `===` `()`,
            splitᴼ(concatenation, r1, `()`, h)
        )
    },
    freshTypedVars<LogicList<Digit>, Digit, LogicList<Digit>> { n1, a, r1 ->
        and(
            n `===` (`1` + n1),
            r `===` (a + r1),
            l `===` `(1)`,
            splitᴼ(n1, r1, `()`, h)
        )
    },
    freshTypedVars<Digit, LogicList<Digit>, Digit, LogicList<Digit>, LogicList<Digit>> { b, n1, a, r1, l1 ->

        and(
            n `===` (b + n1),
            r `===` (a + r1),
            l `===` (b + l1),
            posᴼ(l1),
            splitᴼ(n1, r1, l1, h)
        )
    },
)

/**
 * Satisfies n = b ^ q + r, where 0 <= r <= n and q is the largest.
 */
context(RelationalContext)
@Suppress("NAME_SHADOWING")
fun logᴼ(n: OlegTermAsList, b: OlegTermAsList, q: OlegTermAsList, r: OlegTermAsList): Goal = conde(
    (n `===` `(1)`) and posᴼ(b) and (q `===` `()`) and (r `===` `()`),
    (q `===` `()`) and lessThanᴼ(n, b) and plusᴼ(r, `(1)`, n),
    (q `===` `(1)`) and greaterThan1ᴼ(b) and hasTheSameLengthᴼ(n, b) and plusᴼ(r, b, n),
    (b `===` `(1)`) and posᴼ(q) and plusᴼ(r, `(1)`, n),
    (b `===` `()`) and posᴼ(q) and (r `===` n),
    (b `===` numberTwo) and freshTypedVars<Digit, Digit, LogicList<Digit>> { a, ad, dd ->

        and(
            posᴼ(dd),
            n `===` (a + (ad + dd)),
            exp2ᴼ(n, nilLogicList(), q),
            freshTypedVars<LogicList<Digit>> { s ->
                splitᴼ(n, dd, r, s)
            }
        )
    },
    and(
        freshTypedVars<Digit, Digit, Digit, LogicList<Digit>> { a, ad, add, ddd ->
            conde(
                b `===` numberThree,
                b `===` (a + (ad + (add + ddd)))
            )
        },
        hasTheSmallerLengthᴼ(b, n),
        freshTypedVars<OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList> { bw1, bw, nw, nw1, ql1, ql, s ->
            and(
                exp2ᴼ(b, nilLogicList(), bw1),
                plusᴼ(bw1, `(1)`, bw),
                hasTheSmallerLengthᴼ(q, n),
                freshTypedVars<OlegLogicNumberAsList, OlegLogicNumberAsList> { q1, bwq1 ->
                    and(
                        plusᴼ(q, `(1)`, q1),
                        mulᴼ(bw, q1, bwq1),
                        lessThanᴼ(nw1, bwq1)
                    )
                },
                exp2ᴼ(n, nilLogicList(), nw1),
                plusᴼ(nw1, `(1)`, nw),
                divᴼ(nw, bw, ql1, s),
                plusᴼ(ql, `(1)`, ql1),
                hasTheSmallerOrSameLengthᴼ(ql, q),
                freshTypedVars<OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList> { bql, qh, s, qdh, qd ->
                    and(
                        repeatedMulᴼ(b, ql, bql),
                        divᴼ(nw, bw1, qh, s),
                        plusᴼ(ql, qdh, qh),
                        plusᴼ(ql, qd, q),
                        lessThanOrEqualᴼ(qd, qdh),
                        freshTypedVars<OlegLogicNumberAsList, OlegLogicNumberAsList, OlegLogicNumberAsList> { bqd, bq1, bq ->
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
fun exp2ᴼ(n: OlegTermAsList, b: Term<LogicList<Digit>>, q: OlegTermAsList): Goal = conde(
    (n `===` `(1)`) and (q `===` `()`),
    and(
        greaterThan1ᴼ(n) and (q `===` `(1)`),
        freshTypedVars<OlegLogicNumberAsList> { s ->
            splitᴼ(n, b, s, logicListOf(`1`))
        }
    ),
    freshTypedVars<LogicList<Digit>, LogicList<Digit>> { q1, b2 ->

        and(
            q `===` (`0` + q1),
            posᴼ(q1),
            hasTheSmallerLengthᴼ(b, n),
            appendᴼ(b, `1` + b, b2),
            exp2ᴼ(n, b2, q1)
        )
    },
    freshTypedVars<LogicList<Digit>, LogicList<Digit>, LogicList<Digit>, OlegLogicNumberAsList> { q1, nh, b2, s ->

        and(
            q `===` (`1` + q1),
            posᴼ(q1),
            posᴼ(nh),
            splitᴼ(n, b, s, nh),
            appendᴼ(b, `1` + b, b2),
            exp2ᴼ(nh, b2, q1)
        )
    }
)

context(RelationalContext)
fun expᴼ(b: OlegTermAsList, q: OlegTermAsList, n: OlegTermAsList): Goal = logᴼ(n, b, q, `()`)
