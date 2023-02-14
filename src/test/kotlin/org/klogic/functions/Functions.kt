/*
package org.klogic.functions

import org.klogic.core.`&&&`
import org.klogic.core.`===`
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.freshTypedVar
import org.klogic.core.`|||`
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.terms.RecursiveList
import org.klogic.terms.plus

inline fun <reified T : Term> appendo(x: T, y: T, xy: T): Goal =
    ((x `===` nilRecursiveList<T>()) `&&&` (y `===` xy)) `|||`
            freshTypedVar<T> { head ->
                freshTypedVar<T> { tail ->
                    freshTypedVar<T> { rest ->
                        (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendo(tail, y, rest)
                    }
                }
            }

fun <T : Any> reverso(x: Term<RecursiveList<T>>, reversed: Term<RecursiveList<T>>): Goal =
    ((x `===` nilRecursiveList()) `&&&` (reversed `===` nilRecursiveList())) `|||`
            freshTypedVar<RecursiveList<T>> { head ->
                freshTypedVar<RecursiveList<T>> { tail ->
                    freshTypedVar { rest ->
                        (x `===` head + tail) `&&&` reverso(tail, rest) `&&&` appendo(
                            rest,
                            head + nilRecursiveList(),
                            reversed
                        )
                    }
                }
            }
*/
