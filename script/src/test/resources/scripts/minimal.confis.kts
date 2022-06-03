

title = "Minimal example"

val alice by party
val eat by action
val cookie by thing
val cake by thing

alice may eat(cookie) unless  {
    with purpose Commercial
}

