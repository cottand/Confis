

title = "Simple Contract Example"
val alice by party(
        named = "Alice",
        description = "Alice Liddell of Imperial College"
)

val bob by party(
    named = "Bob",
    description = "Bob from Sheffield"
)

val pay by action

val data by thing(
        named = "the Data",
        description = "Confidential non-anonymised customer data"
)

val send by action(description = "Email file to company inbox")

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall 
    be governed and construed in accordance with the law
    of England and Wales.
"""


alice must pay(bob) underCircumstances {
    within { (1 of June)..(7 of June) year 2022 }
}

alice mayNot pay(bob) asLongAs {
    after { alice did pay(bob) }
}

bob must send(data) underCircumstances {
    after { alice did pay(bob) }
    within { (7 of June)..(14 of June) year 2022 }
}
