val alice by party
val bob by party()
val pay by action
val notify by action(description = "Notify by email")

alice may pay(alice)

alice may notify(alice) asLongAs {
    with purpose Commercial
    after { alice did pay(alice) }
}



-"""
 This license is governed by england law
"""
