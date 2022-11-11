import java.io.IOException
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util
import java.util.Locale
import java.util.stream.Collectors
import scala.io.Source

object PageHTML {
  private val format = DateTimeFormatter.ofPattern("EEEE dd LLLL yyyy").withLocale(Locale.FRANCE)

  def makeArrondissementPage(cinemas: Seq[Cinema],
                             arrondissement: String,
                             date: LocalDateTime): String = {
    val template = Source.fromResource("template-timetable.html").mkString
    val cinemasHtml =
      if (cinemas.nonEmpty) cinemas.map(_.html).mkString("")
      else "<p>Pas de cinemas dans cet arrondissement.</p>"

    template
      .replace("{seance-group}", arrondissement)
      .replace("{date}", date.format(format))
      .replace("{body}", cinemasHtml)
  }

  def makeMoviePage(movieScreenings: MovieScreenings, date: LocalDateTime): String = {
    val template = Source.fromResource("template-timetable.html").mkString
    val movieHtml =
      if (movieScreenings.screenings.nonEmpty) movieScreenings.html
      else "<p>Pas de séances pour ce film.</p>"

    template
      .replace("{seance-group}", movieScreenings.title)
      .replace("{date}", date.format(format))
      .replace("{body}", movieHtml)
  }

  def makeSommairePage(top5PressRecent: Seq[MovieRating],
                       top5SpectatorsRecent: Seq[MovieRating],
                       top5PressAll: Seq[MovieRating],
                       top5SpectatorsAll: Seq[MovieRating],
                       date: LocalDateTime): String = {
    val template = Source.fromResource("template-sommaire.html").mkString
    // format: off
    template
      .replace("{top 1 press recent}", s"${top5PressRecent(0).title} - (${top5PressRecent(0).pressRating.getOrElse(0.0)})")
      .replace("{top 2 press recent}", s"${top5PressRecent(1).title} - (${top5PressRecent(1).pressRating.getOrElse(0.0)})")
      .replace("{top 3 press recent}", s"${top5PressRecent(2).title} - (${top5PressRecent(2).pressRating.getOrElse(0.0)})")
      .replace("{top 4 press recent}", s"${top5PressRecent(3).title} - (${top5PressRecent(3).pressRating.getOrElse(0.0)})")
      .replace("{top 5 press recent}", s"${top5PressRecent(4).title} - (${top5PressRecent(4).pressRating.getOrElse(0.0)})")
      .replace("{top 1 spectators recent}", s"${top5SpectatorsRecent(0).title} - (${top5SpectatorsRecent(0).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 2 spectators recent}", s"${top5SpectatorsRecent(1).title} - (${top5SpectatorsRecent(1).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 3 spectators recent}", s"${top5SpectatorsRecent(2).title} - (${top5SpectatorsRecent(2).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 4 spectators recent}", s"${top5SpectatorsRecent(3).title} - (${top5SpectatorsRecent(3).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 5 spectators recent}", s"${top5SpectatorsRecent(4).title} - (${top5SpectatorsRecent(4).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 1 press all}", s"${top5PressAll(0).title} - (${top5PressAll(0).pressRating.getOrElse(0.0)})")
      .replace("{top 2 press all}", s"${top5PressAll(1).title} - (${top5PressAll(1).pressRating.getOrElse(0.0)})")
      .replace("{top 3 press all}", s"${top5PressAll(2).title} - (${top5PressAll(2).pressRating.getOrElse(0.0)})")
      .replace("{top 4 press all}", s"${top5PressAll(3).title} - (${top5PressAll(3).pressRating.getOrElse(0.0)})")
      .replace("{top 5 press all}", s"${top5PressAll(4).title} - (${top5PressAll(4).pressRating.getOrElse(0.0)})")
      .replace("{top 1 spectators all}", s"${top5SpectatorsAll(0).title} - (${top5SpectatorsAll(0).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 2 spectators all}", s"${top5SpectatorsAll(1).title} - (${top5SpectatorsAll(1).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 3 spectators all}", s"${top5SpectatorsAll(2).title} - (${top5SpectatorsAll(2).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 4 spectators all}", s"${top5SpectatorsAll(3).title} - (${top5SpectatorsAll(3).spectatorsRating.getOrElse(0.0)})")
      .replace("{top 5 spectators all}", s"${top5SpectatorsAll(4).title} - (${top5SpectatorsAll(4).spectatorsRating.getOrElse(0.0)})")
      .replace("{date}", date.format(format))
    // format: on
  }

  def makeRechercherFilmPage(movies: Seq[MovieScreenings], date: LocalDateTime): String = {
    val template = Source.fromResource("template-rechercher.html").mkString
    template
      .replace("{title}", "Rechercher un film")
      .replace("{date}", date.format(format))
      .replace("{recherche}", "film")
      .replace("{body}", movies.map(_.html).mkString("\n\n"))
  }

  def makeRechercherCinemaPage(cinemas: Seq[Cinema], date: LocalDateTime): String = {
    val template = Source.fromResource("template-rechercher.html").mkString
    template
      .replace("{title}", "Rechercher un cinéma")
      .replace("{date}", date.format(format))
      .replace("{recherche}", "cinéma")
      .replace("{body}", cinemas.map(_.html).mkString("\n\n"))
  }
}
