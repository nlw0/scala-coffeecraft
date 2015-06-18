package coffeecraft.dao

import akka.http.scaladsl.model.HttpResponse
import coffeecraft.models.{Coffee, Inventory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CoffeeDaoRestInterface {

  def get(id: Int) =
    CoffeeDao.fetchById(id)

  def delete(id: Int) = {
    CoffeeDao.remove(id) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def post(newCoffee: Coffee) = {
    CoffeeDao.insert(newCoffee)
    HttpResponse(204)
  }

  def put(id: Int, newCoffee: Coffee) =
    CoffeeDao.update(id, newCoffee) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }

  def listAll(): Future[Coffee] = CoffeeDao.fetchById(1) // CoffeeDao.fetchAll()

}


object InventoryDaoRestInterface {

  def get(id: Int) =
    InventoryDao.fetchById(id)

  def delete(id: Int) = {
    InventoryDao.remove(id) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def post(newItem: Inventory) = {
    InventoryDao.insert(newItem)
    HttpResponse(204)
  }

  def put(id: Int, newItem: Inventory) =
    InventoryDao.update(id, newItem) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }

  def listAll(): Future[Inventory] = InventoryDao.fetchById(1) //All()

}
