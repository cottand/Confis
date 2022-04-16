val alice by party("alice")

val bob by party

val pay by action

val shareDataWith by action


// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

alice may shareDataWith(bob) asLongAs {
    with purpose Research
    after { bob did pay(alice) }
}

bob must pay(alice) underCircumstances {
    within { (1 of June)..(7 of June) year 2022 }
}

