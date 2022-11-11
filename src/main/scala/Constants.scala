import TimeGrid.PostalCode

object Constants {
  val parisPostalCodes: Seq[PostalCode] = Seq(
    "75001",
    "75002",
    "75003",
    "75004",
    "75005",
    "75006",
    "75007",
    "75008",
    "75009",
    "75010",
    "75011",
    "75012",
    "75013",
    "75014",
    "75015",
    "75016",
    "75017",
    "75018",
    "75019",
    "75020"
  )

  val postalCodeOrder: Map[String, Int] = parisPostalCodes.zipWithIndex.toMap
}
