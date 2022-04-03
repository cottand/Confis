val alice by party("alice")

val bob by party("bob")

val hug by action

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

alice may { hug(bob) } asLongAs {
    with purpose (Research)
}

alice mayNot { hug(bob) } asLongAs {
    with purpose Commercial
}
