package org.klogic.functions

import org.klogic.core.`&&&`
import org.klogic.core.Goal
import org.klogic.core.Nil.nil
import org.klogic.core.Term
import org.klogic.core.fresh
import org.klogic.core.`|||`

fun appendo(x: Term, y: Term, xy: Term): Goal {
    return ((x `===` nil) `&&&` (y `===` xy)) `|||`
            fresh { head ->
                fresh { tail ->
                    fresh { rest ->
                        (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendo(tail, y, rest)
                    }
                }
            }
}

fun reverso(x: Term, reversed: Term): Goal {
    return ((x `===` nil) `&&&` (reversed `===` nil)) `|||`
            fresh { head ->
                fresh { tail ->
                    fresh { rest ->
                        (x `===` head + tail) `&&&` reverso(tail, rest) `&&&` appendo(
                            rest,
                            head + nil,
                            reversed
                        )
                    }
                }
            }
}
