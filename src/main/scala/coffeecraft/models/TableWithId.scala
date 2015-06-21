package coffeecraft.models

import slick.driver.H2Driver.api._


trait EntityWithId {
  type Id

  def id: Option[Long]
}


abstract class TableWithId[E <: EntityWithId](tag: Tag, name: String) extends Table[E](tag, name) {
  def id: Rep[Long]
}
