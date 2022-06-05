import io.kotest.matchers.date.within

title = "Meat Purchase contract"

val effDate = 1 of June year 2022

val payDueDate = 10 of June year 2022
val deliveryDueDate = 15 of June year 2022

val seller by party("the Seller",
    description = "Alice Liddell"
)
val buyer by party("the Buyer",
    description = "The Meat Supermarket, Inc"
)

val meat by thing("the Goods",
    description = "30kg of beef"
)

val giveMeatTo by action("give $meat to",
    description = "as in pass on the title in $meat to"
)

val deliverMeatTo by action("deliver $meat to",
    description = "as in deliver $meat in one delivery at the warehouse of"
)

val contract by thing("the Contract",
    description = "this legal agreement"
)

val amt = 20
val curr = "EUR"
val interest = "3%"

val payMeatPriceTo by action("pay for $meat to",
    description = "as in pay $amt in $curr to"
)
val payInterestMeatPriceTo by action("pay for $meat to",
    description = "as in pay $amt in $curr with $interest interest to"
)

buyer may payMeatPriceTo(seller) asLongAs {
    at { payDueDate }
}
buyer mayNot payMeatPriceTo(seller) unless {
    at { payDueDate }
}

seller must giveMeatTo(buyer) underCircumstances {
    after { buyer did payMeatPriceTo(seller) }
}

seller must deliverMeatTo(buyer) underCircumstances {
    after { buyer did payMeatPriceTo(buyer) }
    within { deliveryDueDate..(deliveryDueDate + 10.days) }
}

buyer may payInterestMeatPriceTo(buyer) asLongAs {
    after(payDueDate)
}

// termination

val terminate by action
buyer mayNot terminate(contract) asLongAs {
    // we do not need to specify 10 day delivery because a
    // longer delivery is outside the capabilities of the Seller
    after { seller did giveMeatTo(buyer) }
}

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

