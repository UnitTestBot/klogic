package org.klogic.terms

import org.klogic.core.Term
import org.klogic.core.Var.Companion.createTypedVar

fun Any.toTerm(): Term<out Any> = when (this) {
    is Int -> this.createTypedVar()
    is String -> Symbol(this)
    is Term<*> -> this
    else -> error("Could not transform $this to term")
}
