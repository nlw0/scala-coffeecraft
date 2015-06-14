package coffeecraft.dao

import akka.http.scaladsl.model.HttpResponse
import coffeecraft.models.Coffee

import scala.concurrent.ExecutionContext.Implicits.global

object CoffeeDaoRestInterface {

  def get(id: Int) =
    CoffeeDao.run(Fetch(id)).mapTo[Vector[Coffee]] map (_.headOption)

  def delete(id: Int) = {
    CoffeeDao.run(Remove(id)).mapTo[Int] map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def post(newCoffee: Coffee) = {
    CoffeeDao.run(Insert(newCoffee)).mapTo[Unit]
    HttpResponse(204)
  }

  def put(id: Int, newCoffee: Coffee) =
    CoffeeDao.run(Update(id, newCoffee)).mapTo[Int] map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }

  def getAll() = CoffeeDao.run(FetchAll).mapTo[Vector[Coffee]]

}
