package org.klogic.functions

import org.klogic.core.`&&&`
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.freshTypedVar
import org.klogic.core.`|||`
import org.klogic.terms.Nil.nilLogicList
import org.klogic.terms.LogicList
import org.klogic.terms.plus

fun <T : Term<T>> appendo(x: Term<LogicList<T>>, y: Term<LogicList<T>>, xy: Term<LogicList<T>>): Goal =
    ((x `===` nilLogicList())) `&&&` (y `===` xy) `|||`
            freshTypedVar<T> { head ->
                freshTypedVar<LogicList<T>> { tail ->
                    freshTypedVar<LogicList<T>> { rest ->
                        (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendo(tail, y, rest)
                    }
                }
            }

fun <T : Term<T>> reverso(x: Term<LogicList<T>>, reversed: Term<LogicList<T>>): Goal =
    ((x `===` nilLogicList()) `&&&` (reversed `===` nilLogicList())) `|||`
            freshTypedVar<T> { head ->
                freshTypedVar<LogicList<T>> { tail ->
                    freshTypedVar<LogicList<T>> { rest ->
                        (x `===` head + tail) `&&&` reverso(tail, rest) `&&&` appendo(
                            rest,
                            head + nilLogicList(),
                            reversed
                        )
                    }
                }
            }
