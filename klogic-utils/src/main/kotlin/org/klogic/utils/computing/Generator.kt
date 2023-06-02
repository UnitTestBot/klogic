@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.computing

import org.klogic.core.*
import org.klogic.utils.terms.*
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.Symbol.Companion.toSymbol

// TODO docs

sealed interface Gterm : CustomTerm<Gterm> {
    abstract fun toList(): List<Term<Gterm>>
}

@JvmInline
value class Symb(val s: Term<Symbol>) : Gterm {
    override val subtreesToUnify: Array<Term<Symbol>>
        get() = arrayOf(s)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): Symb = Symb(subtrees.single() as Term<Symbol>)

    override fun toString(): String = s.toString()

    override fun toList(): List<Term<Gterm>> = listOf(this)
}

@JvmInline
value class Seq(private val xs: Term<LogicList<Gterm>>) : Gterm {
    constructor(vararg terms: Term<Gterm>) : this(logicListOf(*terms))

    override val subtreesToUnify: Array<*>
        get() = arrayOf(xs)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): Seq = Seq(subtrees.single() as Term<LogicList<Gterm>>)

    override fun toString(): String = xs.toString()

    override fun toList(): List<Term<Gterm>> = xs.asReified().toList().flatMap {
        if (it.isVar()) listOf(it) else it.asReified().toList()
    }
}


sealed interface Gresult : CustomTerm<Gresult>

typealias Fenv = LogicList<LogicPair<Symbol, Gresult>>

data class Closure(private val s: Term<Symbol>, private val t: Term<Gterm>, private val xs: Term<Fenv>) : Gresult {
    override val subtreesToUnify: Array<*>
        get() = arrayOf(s, t, xs)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): Closure {
        // We use by-hand iteration here to avoid losing performance.
        val iterator = subtrees.iterator()
        val s = iterator.next()
        val t = iterator.next()
        val xs = iterator.next()

        require(!iterator.hasNext()) {
            "Expected only three elements for constructing Closure but got more elements"
        }

        return Closure(s as Term<Symbol>, t as Term<Gterm>, xs as Term<Fenv>)
    }
}

@JvmInline
value class Val(private val t: Term<Gterm>) : Gresult {
    override val subtreesToUnify: Array<Term<Gterm>>
        get() = arrayOf(t)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): Val = Val(subtrees.single() as Term<Gterm>)
}

internal val quoteSymbol: Symbol = "quote".toSymbol()
internal val listSymbol: Symbol = "list".toSymbol()
internal val lambdaSymbol: Symbol = "lambda".toSymbol()

fun lookupᴼ(x: Term<Symbol>, env: Term<Fenv>, t: Term<Gresult>): Goal =
    freshTypedVars<Symbol, Gresult, LogicList<LogicPair<Symbol, Gresult>>> { y, v, rest ->
        and(
            env `===` ((y logicTo v) + rest),
            conde(
                (y `===` x) and (v `===` t),
                (y `!==` x) and lookupᴼ(x, rest, t)
            )
        )
    }

fun notInEnvᴼ(x: Term<Symbol>, env: Term<Fenv>): Goal = conde(
    freshTypedVars<Symbol, Gresult, LogicList<LogicPair<Symbol, Gresult>>> { y, v, rest ->
        and(
            env `===` ((y logicTo v) + rest),
            y `!==` x,
            notInEnvᴼ(x, rest)
        )
    },
    env `===` nilLogicList()
)

fun properListᴼ(es: Term<LogicList<Gterm>>, env: Term<Fenv>, rs: Term<LogicList<Gterm>>): Goal = conde(
    (es `===` nilLogicList()) and (rs `===` nilLogicList()),
    freshTypedVars<Gterm, LogicList<Gterm>, Gterm, LogicList<Gterm>> { e, d, te, td ->
        and(
            es `===` e + d,
            rs `===` te + td,
            evalᴼ(e, env, Val(te)),
            properListᴼ(d, env, td)
        )
    }
)

fun evalᴼ(term: Term<Gterm>, env: Term<Fenv>, r: Term<Gresult>): Goal = conde(
    freshTypedVars<Gterm> { t ->
        and(
            term `===` Seq(logicListOf(Symb(quoteSymbol), t)),
            r `===` Val(t),
            notInEnvᴼ(quoteSymbol, env)
        )
    },
    freshTypedVars<LogicList<Gterm>, LogicList<Gterm>> { es, rs ->
        and(
            term `===` Seq(Symb(listSymbol) + es),
            r `===` Val(Seq(rs)),
            notInEnvᴼ(listSymbol, env),
            properListᴼ(es, env, rs)
        )
    },
    freshTypedVars<Symbol> { s ->
        and(
            term `===` Symb(s),
            lookupᴼ(s, env, r)
        )
    },
    freshTypedVars<Gterm, Gterm, Gresult, Symbol, Gterm, Fenv> { func, arge, arg, x, body, env1 ->
        and(
            term `===` Seq(logicListOf(func, arge)),
            evalᴼ(arge, env, arg),
            evalᴼ(func, env, Closure(x, body, env1)),
            evalᴼ(body, (x logicTo arg) + env1, r)
        )
    },
    freshTypedVars<Symbol, Gterm> { x, body ->
        and(
            term `===` Seq(Symb(lambdaSymbol) + logicListOf(Seq(Symb(x).toLogicList()), body)),
            notInEnvᴼ(lambdaSymbol, env),
            r `===` Closure(x, body, env)
        )
    }
)
