package coffeecraft

import coffeecraft.models.{Coffee, Coffees, InventoryItem, InventoryItems}
import slick.dbio.DBIO
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InitDB {
  def apply() = {
    val coffees = TableQuery[Coffees]

    val invent = TableQuery[InventoryItems]

    val db = Database.forConfig("h2mem1")

    val createTable = DBIO.seq(
      coffees.schema.create,
      invent.schema.create,
      coffees += Coffee("Milk", 2.0),
      coffees += Coffee("Coffee", 3.25),
      coffees += Coffee("Sparkling water", 1.20),
      coffees += Coffee("Jameson", 12.50),
      invent += InventoryItem(101, 2),
      invent += InventoryItem(101, 3),
      invent += InventoryItem(102, 1),
      invent += InventoryItem(102, 4),
      invent += InventoryItem(102, 4)
    )

    Await.result(db.run(createTable), Duration.Inf)

  }
}
