package org.klogic.utils

import org.klogic.core.Term
import org.klogic.core.UnboundedValue
import org.klogic.core.Wildcard

internal fun Map<UnboundedValue<*>, Term<*>>.withoutWildcards(): Map<UnboundedValue<*>, Term<*>> =
    filterNot { it.key is Wildcard || it.value is Wildcard }
