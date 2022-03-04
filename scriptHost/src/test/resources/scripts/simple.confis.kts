val Alice by declareParty

val Bob by declareParty

val hug by declareAction

val text by declareAction

// the `additionally` modifier allows appending "purpose policies"
Alice may { hug(Bob) } additionally {
    purposes allowed include(Research)
    purposes forbidden include(Commercial)
}

// a plaintext clause that we do not wish to try to encode
-"""
    The Licence and the terms and conditions thereof shall be governed and construed in
    accordance with the law of England and Wales.
"""

// exceptions can be added at the end of clauses with `unless`
Bob may { hug(Alice) } additionally { purposes forbidden include(Commercial) } unless { forceMajeure }

Bob mayNot { text(Alice) } unless { forceMajeure }
