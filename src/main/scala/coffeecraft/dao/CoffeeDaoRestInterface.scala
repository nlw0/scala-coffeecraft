package coffeecraft.dao

import akka.http.scaladsl.model.HttpResponse
import coffeecraft.models.Coffee

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CoffeeDaoRestInterface {

  def get(id: Int) =
    CoffeeDao.fetch(id) map (_.headOption)

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

  def listAll(): Future[Vector[Coffee]] = CoffeeDao.fetchAll()

}
