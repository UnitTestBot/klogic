@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.computing

import org.klogic.core.*
import org.klogic.utils.terms.Nil.nilLogicList

context(RelationalContext)
fun quinesᴼ(q: Term<Gterm>): Goal = evalᴼ(q, nilLogicList(), Val(q))

/**
 * Finds [n] quines - such programs q that (eval q) ⇒ q.
 */
context(RelationalContext)
fun findQuines(n: Int): List<ReifiedTerm<Gterm>> = run(n, { quinesᴼ(it) })
