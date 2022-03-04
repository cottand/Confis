val Alice by declareParty

val Bob by declareParty

val text by declareAction

// should not compile: cannot use `purposes` for a mayNot clause
Bob mayNot { text(Alice) } additionally { purposes forbidden include(Commercial) }
