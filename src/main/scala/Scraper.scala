import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.select.Elements

import java.io.IOException
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import java.util
import java.util.Locale
import java.util.regex.Pattern
import java.util.stream.Collectors
import scala.jdk.CollectionConverters.*
import scala.util.Try

object Scraper {

  /** Scrapes a cinema page and return the scrapped data. */
  def scrapeCinemaPage(cinemaURL: String): (Cinema, Seq[MovieRating]) =
    try {
      val doc = Jsoup.connect(cinemaURL).get
      val cinemaName = doc.getElementsByClass("theater-cover-title").first.text
      val movies =
        doc.getElementsByClass("card entity-card entity-card-list movie-card-theater cf hred")
      val movieInfo = movies.asScala.map { movie =>
        val title = movie.getElementsByClass("meta-title-link").first.text
        val screeningTimes = movie
          .getElementsByClass("showtimes-hour-item-value")
          .eachText
          .asScala
          .map(time => LocalTime.parse(time, TimeGrid.timeFormatter))
          .toSeq

        val format = DateTimeFormatter.ofPattern("d LLLL yyyy").withLocale(Locale.FRANCE)
        val releaseDate = Try(
          LocalDate.parse(movie
                            .getElementsByClass("meta-body-item meta-body-info")
                            .first
                            .text
                            .split("/")
                            .head
                            .strip(),
                          format)).toOption

        val pressRating = Try(
          movie
            .getElementsByClass("rating-item")
            .asScala
            .filter(rating =>
              rating.getElementsByClass("rating-title").first.text.toLowerCase == "presse")
            .head
            .getElementsByClass("stareval-note")
            .first
            .text
            .replace(",", ".")
            .toDouble).toOption
        val spectatorsRating = Try(
          movie
            .getElementsByClass("rating-item")
            .asScala
            .filter(rating =>
              rating.getElementsByClass("rating-title").first.text.toLowerCase == "spectateurs")
            .head
            .getElementsByClass("stareval-note")
            .first
            .text
            .replace(",", ".")
            .toDouble).toOption
        (title,
         screeningTimes,
         MovieRating(title,
                     releaseDate = releaseDate,
                     pressRating = pressRating,
                     spectatorsRating = spectatorsRating))
      }

      Cinema(cinemaName, movieInfo.map(info => info._1 -> info._2).toMap) ->
        movieInfo.toSeq.map(_._3)
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }

  /** @return the URLs of all the cinemas for a postal code. */
  def findCinemasByPostalCode(postalCode: String): Seq[String] = {
    val regex = "[A-Z0-9]+"
    val pattern = Pattern.compile(regex)
    Jsoup
      .connect("https://www.allocine.fr/rechercher/theater/?q=" + postalCode)
      .get
      .getElementsByClass("add-theater-anchor")
      .asScala
      .map(e => {
        // Ressemble à data-theater="{"id":"C0073","name":"Le Champo - Espace Jacques Tati"}"
        // Il y a des quote etc dedans d'où le unescapeEntities
        val data = Parser.unescapeEntities(e.attr("data-theater"), true)
        // hack pour récupérer le code.
        data
          .split("\"")
          .find((s: String) => pattern.matcher(s).matches)
          .get
      })
      .filterNot(code => code.equalsIgnoreCase("Z8888")) // Z8888 est un ciné test.
      .map(code => s"https://www.allocine.fr/seance/salle_gen_csalle=$code.html")
      .toSeq
  }
}
