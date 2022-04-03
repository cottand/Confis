val alice by party("alice")

val bob by party("bob")

val hug by action

alice may { hug(bob) } asLongAs {
    alice may { hug(bob) }
    with purpose (Research)
}

alice mayNot { hug(bob) } asLongAs {
    with purpose Commercial
}
