val alice by party(named = "Alice", description = "Alice Liddell")
val bob by party(named = "Bobby", description = "Bob The Builder")
val pay by action
val notify by action(description = "Notify by email")

alice may pay(bob)

alice may notify(alice) asLongAs {
    with purpose Commercial
    after { alice did pay(bob) }
}



-"""
 This license is governed by england law
"""
