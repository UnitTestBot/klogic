@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.computing

import org.klogic.core.*
import org.klogic.utils.terms.LogicTriple
import org.klogic.utils.terms.Nil.nilLogicList

context(RelationalContext)
fun thrinesᴼ(x: Term<LogicTriple<Gterm, Gterm, Gterm>>): Goal = freshTypedVars<Gterm, Gterm, Gterm> { p, q, r ->
    and(
        p `!==` q,
        p `!==` r,
        q `!==` r,
        evalᴼ(p, nilLogicList(), Val(q)),
        evalᴼ(q, nilLogicList(), Val(r)),
        evalᴼ(r, nilLogicList(), Val(p)),
        LogicTriple(p, q, r) `===` x
    )
}

/**
 * Finds [n] thrines - distinct programs p, q, and r, such that
 * (eval p) ⇒ q, (eval q) ⇒ r , and (eval r ) ⇒ p.
 */
context(RelationalContext)
fun findThrines(n: Int): List<ReifiedTerm<LogicTriple<Gterm, Gterm, Gterm>>> = run(n, { thrinesᴼ(it) })
