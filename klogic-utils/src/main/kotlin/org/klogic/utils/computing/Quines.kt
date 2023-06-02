@file:Suppress("NonAsciiCharacters", "FunctionName")

package org.klogic.utils.computing

import org.klogic.core.Goal
import org.klogic.core.ReifiedTerm
import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.utils.terms.Nil.nilLogicList

// TODO docs

fun quinesᴼ(q: Term<Gterm>): Goal = evalᴼ(q, nilLogicList(), Val(q))

fun findQuines(n: Int): List<ReifiedTerm<Gterm>> = run(n, { quinesᴼ(it) })
