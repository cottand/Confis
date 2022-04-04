val ana by party
val bob by party


val cake by thing
val eat by action

val startOfContract = 1 of May year 2022
val endOfContract = 13 of April year 2023



bob may { eat(cake) } unless {
    within { startOfContract..endOfContract }
}
