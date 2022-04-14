val alice by party("alice")

val bob by party

val pay by action

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

alice may { pay(bob) } asLongAs {
    with purpose Research
}

alice mayNot { pay(bob) } asLongAs {
    with purpose Commercial
    alice did { pay(bob) }
}
