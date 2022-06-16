

title = "Minimal example"

val alice by party
val eat by action
val soup by thing

val cake by thing

alice mayNot  eat(soup) unless  {
    with purpose Commercial
    after { alice did eat(cake) }
}

alice may eat(soup) asLongAs {
    with purpose Commercial
    after { alice did eat(cake) }
}