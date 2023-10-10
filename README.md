# What is klogic?

klogic is a strongly typed implementation of relational programming language [miniKanren](http://minikanren.org/) into Kotlin. 
It is largely inspired by [OCanren](https://github.com/PLTools/OCanren) &ndash; a widely known embedding of miniKanren in 
functional programming language OCaml.

# How can I use it?

Relational programming languages are designed based on the logical programming paradigm and perfectly suite
for solving problems that can be expressed as logical queries &ndash; for example, theorem-proving tasks,
puzzle-solving, etc.

Also, relational languages in general and klogic in particular are well-suited for solving constraint 
satisfaction problems. Constraint satisfaction problems involve finding a solution that satisfies a set 
of constraints or conditions (can be logical, or any other type of restrictions). The logical nature and backtracking capabilities of relational languages 
make them particularly effective for expressing and solving constraint problems. 
They can automatically search for solutions by exploring possible combinations 
of values that satisfy the constraints, making them useful in areas like scheduling, planning, and optimization.

# Key features

Like OCanren, klogic has some important features some of which were firstly implemented in 
[faster-minikanren](https://github.com/michaelballantyne/faster-miniKanren). Among them:

- **Disequality constraints** &ndash; <code>(&alpha; &ne; &beta;)</code> asserts that two logic terms &alpha; and &beta; are not equal and can 
never be made equal through unification.
- **Typed logic terms** &ndash; strong static typing disallows unification of logic terms with incompatible types
in compile time.
- **[Wildcard logic variables](https://drive.google.com/file/d/1RdtwC2kmzHK7Sz3fO_Hq9AgMOxwr5m-2/view)** &ndash; 
extending the expressive power of miniKanren with a limited form of universal quantification.

# Examples

Let's see how generating a list with all permutations of some Peano numbers could be implemented.

The following snippet of code introduces a logic type for Peano numbers:

```kotlin
sealed class PeanoLogicNumber : CustomTerm<PeanoLogicNumber> {
    abstract fun toInt(): Int
    
    companion object {
        fun succ(number: Term<PeanoLogicNumber>): NextNaturalNumber = NextNaturalNumber(number)
    }
}

object ZeroNaturalNumber : PeanoLogicNumber() {
    val Z: ZeroNaturalNumber = ZeroNaturalNumber

    override val subtreesToUnify: Array<*> = emptyArray<Any?>()
    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<PeanoLogicNumber> = this

    override fun toInt(): Int = 0
    override fun toString(): String = "0"
}

data class NextNaturalNumber(val previous: Term<PeanoLogicNumber>) : PeanoLogicNumber() {
    override val subtreesToUnify: Array<*>
        get() = arrayOf(previous)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<PeanoLogicNumber> =
        NextNaturalNumber(subtrees.single() as Term<PeanoLogicNumber>)

    override fun toInt(): Int {
        require(previous !is Var) {
            "$this number is not reified"
        }

        return 1 + previous.asReified().toInt()
    }
    override fun toString(): String = "S($previous)"
}
```

The code below shows a definition of sorting relation:
```kotlin
context(RelationalContext)
fun sortᴼ(unsortedList: Term<LogicList<PeanoLogicNumber>>, sortedList: Term<LogicList<PeanoLogicNumber>>): Goal = conde(
    (unsortedList `===` nilLogicList()) and (sortedList `===` nilLogicList()),
    freshTypedVars<PeanoLogicNumber, LogicList<PeanoLogicNumber>, LogicList<PeanoLogicNumber>> { smallest, unsortedOthers, sortedTail ->
        (sortedList `===` smallest + sortedTail) and sortᴼ(unsortedOthers, sortedTail) and smallestᴼ(unsortedList, smallest, unsortedOthers)
    }
)
```

Using this relation, the implementation of relation for generating all permutations is pretty straightforward:
```kotlin
val sortedList = logicListOf(one, two, three)
val goal = { unsortedList: Term<LogicList<PeanoLogicNumber>> -> 
    sortᴼ(unsortedList, sortedList)
}
val permutations = run(6, goal)
```

# Future plans

- [set-var-val! optimization](https://github.com/michaelballantyne/faster-minikanren/#set-var-val) &ndash; speeding
up unification by reducing number of expensive lookups.
- **Filtering out subsumed and irrelevant constraints** &ndash; for now, all disequality constraints are represented
in answers, even if they are connected with logic variables that are unused or are subsumed with another constraints.
