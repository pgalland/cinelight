import org.scalatest.*
import funsuite.*

import java.time.LocalTime

class CinemaAndMoviesSpec extends AnyFunSuite {
  extension(s: String) {
    def toTime: LocalTime = LocalTime.parse(s, TimeGrid.timeFormatter)
  }

  private val JamesBond = "James Bond III"
  private val InTheMood = "In the mood for love"

  private val h10_55 = "10:55".toTime
  private val h12_00 = "12:00".toTime
  private val h12_30 = "12:30".toTime
  private val h20_10 = "20:10".toTime
  private val h20_20 = "20:20".toTime
  private val h20_25 = "20:25".toTime
  private val h20_30 = "20:30".toTime
  private val h20_55 = "20:55".toTime

  test("Simple movie grid") {
    val cinema = Cinema(name = "Whatever",
                        screenings =
                          Map(JamesBond -> Seq(h12_00, h20_30), InTheMood -> Seq(h10_55, h12_30)))

    // format: off
    val expected =
      Map(JamesBond -> Seq(None,         Some(h12_00), Some(h20_30)),
          InTheMood -> Seq(Some(h10_55), Some(h12_30), None))
    // format: on

    assertResult(expected)(TimeGrid.computeTimeGrid(cinema.screenings))
  }

  test("Grid with several same movie in same hour") {
    val cinema =
      Cinema(
        name = "Whatever",
        screenings = Map(JamesBond -> Seq(h12_00, h20_10, h20_20, h20_30),
                     InTheMood -> Seq(h10_55, h20_25, h20_55))
      )

    // format: off
    val expected =
      Map(JamesBond -> Seq(None,         Some(h12_00), Some(h20_10), Some(h20_20), Some(h20_30)),
          InTheMood -> Seq(Some(h10_55), None,         None,         Some(h20_25), Some(h20_55)))
    // format: on

    assertResult(expected)(TimeGrid.computeTimeGrid(cinema.screenings))
  }

  test("Movies.fromAllCinemas") {
    val ugcGobelins = Cinema("UGC Gobelins",
                             screenings =
                               Map(JamesBond -> Seq(h12_00, h20_30), InTheMood -> Seq(h10_55)))
    val escurial =
      Cinema("Escurial", screenings = Map(JamesBond -> Seq(h12_00), InTheMood -> Seq(h12_00, h20_10)))

    val epeeDeBois = Cinema("l'Epée de bois", screenings = Map(InTheMood -> Seq()))

    val expected =
      Seq(
        MovieScreenings(InTheMood,
              Map(("UGC Gobelins", "75013") -> Seq(h10_55),
                  ("Escurial", "75013") -> Seq(h12_00, h20_10))),
        MovieScreenings(JamesBond,
              Map(("UGC Gobelins", "75013") -> Seq(h12_00, h20_30),
                  ("Escurial", "75013") -> Seq(h12_00)))
      )

    assertResult(expected)(
      Movies
        .fromAllCinemas(Seq(ugcGobelins -> "75013", escurial -> "75013", epeeDeBois -> "75005"))
        .sortBy(_.title))
  }

  test("movieTimeGridToHtml") {
    val movieGrid =
      Seq(JamesBond -> Seq(None, Some(h12_00), Some(h20_30)),
          InTheMood -> Seq(Some(h10_55), Some(h12_30), None))

    val expected =
      s"""|<tr>
          |<td>$JamesBond</td> <td></td> <td>12:00</td> <td>20:30</td>
          |</tr>
          |<tr>
          |<td>$InTheMood</td> <td>10:55</td> <td>12:30</td> <td></td>
          |</tr>""".stripMargin

    assertResult(expected)(TimeGrid.timeGridToHtml(movieGrid))
  }
}
