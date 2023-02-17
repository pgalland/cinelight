import java.util
import java.util.Arrays
import Constants.parisPostalCodes

import java.time.{LocalDate, ZoneId}

object Runner {
  def main(args: Array[String]): Unit = {
    doCinelightLogic(dryRun = args.nonEmpty && args.head == "--dry-run")
  }

  def doCinelightLogic(dryRun: Boolean = false): String = {
    val date = java.time.LocalDateTime.now(ZoneId.of("Europe/Paris"))

    val infoByPostalCode = parisPostalCodes.map { postalCode =>
      val scrapingResult = Scraper
        .findCinemasByPostalCode(postalCode)
        .map(Scraper.scrapeCinemaPage)
      val cinemas = scrapingResult.map(_._1)
      val movieRatings = scrapingResult.flatMap(_._2)
      postalCode -> (cinemas, movieRatings)
    }.toMap

    val cinemas = infoByPostalCode.view.mapValues(_._1.filter(_.hasMovies)).toSeq
    val ratings =
      infoByPostalCode.values
        .flatMap(_._2)
        .groupBy(_.title)
        .view
        .mapValues(_.minBy(rating =>
          rating.pressRating.getOrElse(0.0) ->
            rating.spectatorsRating.getOrElse(0.0))) // In case the ratings have changed midway through the scraping, we select the harshest one.
        .toMap

    val movies = Movies
      .fromAllCinemas(cinemas.flatMap {
        case (postalCode, cinemasInZone) => cinemasInZone.map(_ -> postalCode)
      })
      .filter(_.hasScreenings)

    val top5PressRecent = movies
      .filter(movie =>
        ratings(movie.title).releaseDate.exists(releaseDate =>
          releaseDate.isAfter(LocalDate.now().minusYears(1))))
      .sortBy { movie =>
        val rating = ratings(movie.title)
        rating.pressRating.getOrElse(0.0) -> rating.spectatorsRating.getOrElse(0.0)
      }
      .reverse
      .take(5)
    val top5SpectatorsRecent =
      movies
        .filter(movie =>
          ratings(movie.title).releaseDate.exists(releaseDate =>
            releaseDate.isAfter(LocalDate.now().minusYears(1))))
        .sortBy { movie =>
          val rating = ratings(movie.title)
          rating.spectatorsRating.getOrElse(0.0) -> rating.pressRating.getOrElse(0.0)
        }
        .reverse
        .take(5)

    val top5PressAll = movies
      .sortBy { movie =>
        val rating = ratings(movie.title)
        rating.pressRating.getOrElse(0.0) -> rating.spectatorsRating.getOrElse(0.0)
      }
      .reverse
      .take(5)
    val top5SpectatorsAll =
      movies
        .sortBy { movie =>
          val rating = ratings(movie.title)
          rating.spectatorsRating.getOrElse(0.0) -> rating.pressRating.getOrElse(0.0)
        }
        .reverse
        .take(5)

    val pages = Map(
      "index.html" -> PageHTML.makeSommairePage(
        top5PressRecent = top5PressRecent.map(movie => ratings(movie.title)),
        top5SpectatorsRecent = top5SpectatorsRecent.map(movie => ratings(movie.title)),
        top5PressAll = top5PressAll.map(movie => ratings(movie.title)),
        top5SpectatorsAll = top5SpectatorsAll.map(movie => ratings(movie.title)),
        date = date
      ),
      "rechercherFilm.html" -> PageHTML.makeRechercherFilmPage(movies, date),
      "rechercherCinema.html" -> PageHTML.makeRechercherCinemaPage(cinemas.flatMap(_._2), date)
    ) ++
      cinemas.map {
        case (postalCode, cinemasInZone) =>
          s"$postalCode.html" -> PageHTML.makeArrondissementPage(cinemasInZone, postalCode, date)
      }.toMap
    // format: off
      ++ (0 to 4)
      .map(topIdx =>
        s"top_${topIdx + 1}_press_recent.html" -> PageHTML.makeMoviePage(top5PressRecent(topIdx), date))
      .toMap
      ++ (0 to 4)
      .map(topIdx =>
        s"top_${topIdx + 1}_spectators_recent.html" -> PageHTML.makeMoviePage(top5SpectatorsRecent(topIdx), date))
      .toMap
      ++ (0 to 4)
      .map(topIdx =>
        s"top_${topIdx + 1}_press_all.html" -> PageHTML.makeMoviePage(top5PressAll(topIdx), date))
      .toMap
      ++ (0 to 4)
      .map(topIdx =>
        s"top_${topIdx + 1}_spectators_all.html" -> PageHTML.makeMoviePage(top5SpectatorsAll(topIdx), date))
      .toMap
    // format: on

    val ftpUploader = if (dryRun) new LocalUploader else new FTPUploader
    try {
      pages.foreach { case (pageName, pageHTML) => ftpUploader.uploadFile(pageName, pageHTML) }
    } finally { if (ftpUploader != null) ftpUploader.close() }
    "OK"
  }
}
