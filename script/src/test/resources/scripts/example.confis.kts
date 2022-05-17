val alice by party(named = "Alice", description = "Alice Liddell")
val bob by party(named = "Bobby", description = "Bob The Builder")
val pay by action
val notify by action(description = "Notify by email")

alice must pay(bob) underCircumstances  {
    with purpose Internal
}

alice may notify(bob) asLongAs {
    with purpose Commercial
    after { alice did pay(bob) }
}


alice mayNot notify(bob) unless {
    with purpose Commercial
}
-"""
 This license is governed by england law
"""
