val ana by party
val bob by party(description = "the builder")

val cake by thing
val cookie by thing(named = "the cookie")
val eat by action

// ana may eat cake only if bob has eaten the cookie!

bob may { eat(cookie) }

ana may { eat(cake) } asLongAs {
    after { bob did eat(cookie) }
}

