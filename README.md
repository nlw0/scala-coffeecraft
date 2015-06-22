# scala-coffeecraft
Testing akka-http and slick

Right now we only have basic CRUD of coffee ingredients. Will implement some kind of "crafting" method later, to illustrate how to do something else beyond CRUD.

### Example session from the TCP interface
```
> netcat localhost 60001
LIST
Map(1 -> Coffee(Coffee,2.5,Some(1)), 2 -> Coffee(Coffee,2.5,Some(1)), 3 -> Coffee(Milk,2.0,Some(2)), 4 -> Coffee(Lime,1.2,Some(4)))
MINE
None
MINE
Some(Coffee(Lime,1.2,Some(4)))
MINE
Some(Coffee(Jameson,12.5,Some(5)))
MINE
Some(Coffee(Coffee,2.5,Some(1)))
LIST
Map(5 -> Coffee(Lime,1.2,Some(4)), 1 -> Coffee(Coffee,2.5,Some(1)), 6 -> Coffee(Jameson,12.5,Some(5)), 2 -> Coffee(Coffee,2.5,Some(1)), 7 -> Coffee(Coffee,2.5,Some(1)), 3 -> Coffee(Milk,2.0,Some(2)), 4 -> Coffee(Lime,1.2,Some(4)))
CRAFT 2 6 3
Some(Coffee(Irish Coffee,19.0,Some(10)))
CRAFT 5 1
Some(Coffee(Roman Coffee,4.5,Some(9)))
LIST  
Map(9 -> Coffee(Roman Coffee,4.5,Some(9)), 7 -> Coffee(Coffee,2.5,Some(1)), 8 -> Coffee(Irish Coffee,19.0,Some(10)), 4 -> Coffee(Lime,1.2,Some(4)))
MINE
Some(Coffee(Coffee,2.5,Some(1)))
MINE
Some(Coffee(Milk,2.0,Some(2)))
MINE
Some(Coffee(Jameson,12.5,Some(5)))
MINE
None
MINE
Some(Coffee(Coffee,2.5,Some(1)))
LIST
Map(10 -> Coffee(Coffee,2.5,Some(1)), 9 -> Coffee(Roman Coffee,4.5,Some(9)), 13 -> Coffee(Coffee,2.5,Some(1)), 12 -> Coffee(Jameson,12.5,Some(5)), 7 -> Coffee(Coffee,2.5,Some(1)), 11 -> Coffee(Milk,2.0,Some(2)), 8 -> Coffee(Irish Coffee,19.0,Some(10)), 4 -> Coffee(Lime,1.2,Some(4)))
CRAFT 10 11
Some(Coffee(Pingado,4.5,Some(6)))
CRAFT 13 4
Some(Coffee(Roman Coffee,4.5,Some(9)))
LIST
Map(14 -> Coffee(Pingado,4.5,Some(6)), 9 -> Coffee(Roman Coffee,4.5,Some(9)), 12 -> Coffee(Jameson,12.5,Some(5)), 7 -> Coffee(Coffee,2.5,Some(1)), 8 -> Coffee(Irish Coffee,19.0,Some(10)), 15 -> Coffee(Roman Coffee,4.5,Some(9)))
```
