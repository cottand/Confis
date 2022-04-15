val alice by party("alice")

val bob by party

val pay by action

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

alice may pay(bob) asLongAs {
    with purpose Research
    within { (1 of June)..(7 of June) year 2022 }
}

alice mayNot pay(bob) asLongAs {
    after { alice did pay(bob) }
}
