# scala-coffeecraft
This project is an experiment and exercise on using akka-http and slick. We first try to show:
  - How to perform basic CRUD operations in a DB with Slick.
  - How to write a generic DAO class to handle CRUD from different tables.
  - How to write a generic REST routes for those DAOs with GET, POST, DELETE and PUT operations.

And then some more complex stuff:
  - How to separate persistence, application and interface layers.
  - How to write an actor-based application with Akka.

## How does it work anyway?

The application has a bunch of coffee ingredients and products, and each user has an inventory containing a bunch of these. Ingredients can be "crafted" together to produce a new coffee product that is added to the inventory, consuming the ingredients. We can also "mine" for new random ingredients, what costs money, and "sell" the items receiving money in exchange.

### HTTP API
Coffeecraft is just a back-end service, that is supposed to be accessed by a front-end application providing a nice user interface. The HTTP API was created using [Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/current/scala/http/). The service routes are documented on its (Apiary page)[http://docs.coffeecraft.apiary.io/]. The user data is transmitted in JSON format, for instance:

```
> curl -v http://hpavc.net/coffeecraft/api/user/102
> GET /coffeecraft/api/user/102 HTTP/1.1
> User-Agent: curl/7.38.0
> Host: hpavc.net
> Accept: */*
> 
< HTTP/1.1 200 OK
< Date: Sun, 05 Jul 2015 02:34:52 GMT
< Server: akka-http/2.4-M1
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Methods: GET, POST, PUT, OPTIONS, DELETE
< Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization
< Content-Type: application/json
< Content-Length: 441
< 
{
  "money": 10.0,
  "inventory": [
  {"index": 0, "item": {"name": "Coffee", "price": 2.5, "id": 1}},
  {"index": 1, "item": {"name": "Coffee", "price": 2.5, "id": 1}},
  {"index": 2, "item": {"name": "Milk", "price": 2.0, "id": 2}},
  {"index": 3, "item": {"name": "Chocolate", "price": 2.2, "id": 4}}
  ]
}
```

An interface to coffecraft implemented with Dart and Polymer is available at the [coffeecraft-ui](https://github.com/nlw0/coffeecraft-ui) project.
![coffeecraft-ui](http://i.imgur.com/QNsCLvx.png)

### Alternative custom TCP interface
Apart from the HTTP interface, there is a more hackish way to interact with the coffeecraft system. The camel-based TCP server receives the MINE, CRAFT and SELL commands to change the user state, and the LIST command exhibits it. Having this alternative interface is interesting to force us decouple this layer from the rest of the system.

```
> netcat localhost 60001
LIST
CoolUserState(10.0,List(InventoryItem(0,Coffee(Coffee,2.5,Some(1))), InventoryItem(1,Coffee(Coffee,2.5,Some(1))), InventoryItem(2,Coffee(Milk,2.0,Some(2))), InventoryItem(3,Coffee(Chocolate,2.2,Some(4)))))
CRAFT 0 2
ActionACK
LIST
CoolUserState(10.0,List(InventoryItem(1,Coffee(Coffee,2.5,Some(1))), InventoryItem(3,Coffee(Chocolate,2.2,Some(4))), InventoryItem(4,Coffee(Pingado,4.5,Some(7)))))
CRAFT 1 3
ActionACK
LIST
CoolUserState(10.0,List(InventoryItem(0,Coffee(Mocha,6.0,Some(8))), InventoryItem(4,Coffee(Pingado,4.5,Some(7)))))
SELL 0 4
ActionACK
LIST
CoolUserState(20.5,List())
MINE
ActionACK
MINE
ActionACK
MINE
ActionACK
MINE
ActionACK
LIST
CoolUserState(12.5,List(InventoryItem(0,Coffee(Coffee,2.5,Some(1))), InventoryItem(1,Coffee(Lime,1.2,Some(5))), InventoryItem(2,Coffee(Jameson,12.5,Some(6))), InventoryItem(3,Coffee(Lime,1.2,Some(5)))))
CRAFT 0 3
ActionACK
LIST
CoolUserState(12.5,List(InventoryItem(1,Coffee(Lime,1.2,Some(5))), InventoryItem(2,Coffee(Jameson,12.5,Some(6))), InventoryItem(4,Coffee(Roman Coffee,4.5,Some(10)))))
```
