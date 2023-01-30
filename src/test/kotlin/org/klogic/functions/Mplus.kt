package org.klogic.functions

import org.klogic.state.Goal
import org.klogic.state.State
import org.klogic.state.conjuction
import org.klogic.state.disjunction
import org.klogic.state.fresh
import org.klogic.state.unify
import org.klogic.streams.ConsStream
import org.klogic.streams.Stream
import org.klogic.streams.ThunksStream
import org.klogic.streams.bind
import org.klogic.streams.empty
import org.klogic.streams.mplus
import org.klogic.streams.take
import org.klogic.types.Nil
import org.klogic.types.Symbol
import org.klogic.types.Term
import org.klogic.types.Var
import org.klogic.types.toSymbol
import org.klogic.unify.Substitution
import org.klogic.unify.walk

private fun <X> repeat(e: X): ThunksStream<X> {
 //   repeat()
    return ThunksStream { ConsStream(e, repeat(e)) }
}

private fun ones() = repeat(1)

fun testMplus(): Stream<Int> {
    val twos = repeat(2)
    val threes = repeat(3)

    return (ones() mplus twos) mplus threes
}

fun testBind(): Stream<Int> {
    return ones().bind { ConsStream(2, ConsStream(3, empty())) }
}

fun testUnify1(): Substitution? {
    val one = Var(1)
    val secondSym = Symbol("2")

    return org.klogic.unify.unify(emptyMap(), one + secondSym, secondSym + one)
}

fun testUnify2(): Substitution? {
    val one = Var(1)
    val second = Var(2)

    return org.klogic.unify.unify(emptyMap(), one, second)
}

fun testUnify3(): Substitution? {
    return org.klogic.unify.unify(emptyMap(), Var(1) + Var(2), Var(2) + Nil)
}


fun testAppendo() {
    fun appendo(x: Term, y: Term, xy: Term): Goal {
        return (unify(x, Nil) conjuction unify(y, xy)) disjunction
            fresh { head ->
                fresh { tail ->
                    fresh { tail2 ->
                        unify(x, head + tail) conjuction unify(xy, head + tail2) conjuction appendo(tail, y, tail2)
                    }
                }
            }
    }

//    val goal = appendo(Symbol("a") + Nil, Symbol("b") + Nil, Var(-42))
    val goal = appendo(Var(-1), Var(-2), Symbol("a") + (Symbol("b") + Nil))
    // (Nil + ("a" + "b" + Nil),
    // ("a" + Nil) + ("b" + Nil)),
    // ("a" + "b" + Nil) + Nil,
//    val goal = appendo(Symbol("a") + Nil, Nil, Var(-42))
//    val goal = appendo("a".toSymbol() + Nil, "b".toSymbol() + Nil, Symbol("a") + Symbol("b") + Nil)
    org.klogic.state.run(4, Var(-1) + Var(-2), goal).joinToString("\n").let {
        println(it)
    }
}

fun testWalk(): Term {
    val substitution = mapOf(
        0 to Nil
    )

    return walk(Var(0), substitution)
}

fun main() {
//    val mPlusResult = testMplus().take(10)
//    println(mPlusResult)

//    val bindResult = testBind().take(10)
//    println(bindResult)

//    val unify1 = testUnify1()
//    println(unify1)
//
//    val unify2 = testUnify2()
//    println(unify2)
//
//    val unify3 = testUnify3()
//    println(unify3)

    testAppendo()

//    println(testWalk())
}
