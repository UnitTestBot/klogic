@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.computing

import org.klogic.core.*
import org.klogic.utils.terms.LogicPair
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.logicTo

context(RelationalContext)
fun twinesᴼ(x: Term<LogicPair<Gterm, Gterm>>): Goal = freshTypedVars<Gterm, Gterm> { q, p ->
    and(
        (q `!==` p),
        evalᴼ(q, nilLogicList(), Val(p)),
        evalᴼ(p, nilLogicList(), Val(q)),
        x `===` (q logicTo p)
    )
}

/**
 * Finds [n] twines - distinct programs p and q, such that
 * (eval p) ⇒ q and (eval q) ⇒ p.
 */
context(RelationalContext)
fun findTwines(n: Int): List<ReifiedTerm<LogicPair<Gterm, Gterm>>> = run(n, { twinesᴼ(it) })
