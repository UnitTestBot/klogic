package org.klogic.functions

import org.klogic.core.`&&&`
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.freshTypedVar
import org.klogic.core.`|||`
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.terms.RecursiveList
import org.klogic.terms.plus

@Suppress("RemoveExplicitTypeArguments")
fun <T : Term<T>> appendo(x: Term<RecursiveList<T>>, y: Term<RecursiveList<T>>, xy: Term<RecursiveList<T>>): Goal =
    ((x `===` nilRecursiveList())) `&&&` (y `===` xy) `|||`
            freshTypedVar<T> { head ->
                freshTypedVar<RecursiveList<T>> { tail ->
                    freshTypedVar<RecursiveList<T>> { rest ->
                        (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendo(tail, y, rest)
                    }
                }
            }

fun <T : Term<T>> reverso(x: Term<RecursiveList<T>>, reversed: Term<RecursiveList<T>>): Goal =
    ((x `===` nilRecursiveList()) `&&&` (reversed `===` nilRecursiveList())) `|||`
            freshTypedVar<T> { head ->
                freshTypedVar<RecursiveList<T>> { tail ->
                    freshTypedVar<RecursiveList<T>> { rest ->
                        (x `===` head + tail) `&&&` reverso(tail, rest) `&&&` appendo(
                            rest,
                            head + nilRecursiveList(),
                            reversed
                        )
                    }
                }
            }
