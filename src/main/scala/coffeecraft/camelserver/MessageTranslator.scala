package coffeecraft.camelserver

import coffeecraft.domain.UserInventory.{CraftCmd, ListCmd, MineCmd, SellCmd}

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "LIST" :: Nil =>
      ListCmd
    case "MINE" :: Nil =>
      MineCmd
    case "CRAFT" :: inventoryIds =>
      CraftCmd(inventoryIds map (_.toLong) toSet)
    case "SELL" :: inventoryIds =>
      SellCmd(inventoryIds map (_.toLong) toSet)
    case _ =>
  }
}
