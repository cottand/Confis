
val effDate = 1 of June year 2022


val seller by party("the Seller", description = "Alice Liddell")
val buyer by party("the Buyer", description = "The Meat Supermarket, Inc")
val contract by thing("the Contract", description = "this Agreement")


// confidentiality
val reveal by action(
    description = "as in not keeping the contents confidential"
)
seller mayNot reveal(contract) asLongAs {
    within { effDate..(effDate + 6.months) }
}

buyer mayNot reveal(contract) asLongAs {
    within { effDate..(effDate + 6.months) }
}