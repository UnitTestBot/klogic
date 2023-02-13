package org.klogic.functions

import org.klogic.core.`&&&`
import org.klogic.core.Goal
import org.klogic.core.Nil.nil
import org.klogic.core.Term
import org.klogic.core.freshTypedVar
import org.klogic.core.`|||`

fun appendo(x: Term, y: Term, xy: Term): Goal =
    ((x `===` nil) `&&&` (y `===` xy)) `|||`
            freshTypedVar { head ->
                freshTypedVar { tail ->
                    freshTypedVar { rest ->
                        (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendo(tail, y, rest)
                    }
                }
            }

fun reverso(x: Term, reversed: Term): Goal =
    ((x `===` nil) `&&&` (reversed `===` nil)) `|||`
            freshTypedVar { head ->
                freshTypedVar { tail ->
                    freshTypedVar { rest ->
                        (x `===` head + tail) `&&&` reverso(tail, rest) `&&&` appendo(
                            rest,
                            head + nil,
                            reversed
                        )
                    }
                }
            }
