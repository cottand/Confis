val alice by party("alice")

val bob by party("bob")

val pay by action

alice may { pay(bob) } asLongAs {
    with purpose (Research)
}

james mayNot { pay(bob) }
