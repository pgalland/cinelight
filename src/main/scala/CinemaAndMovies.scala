import Constants.postalCodeOrder
import TimeGrid.{CinemaName, MovieTitle, PostalCode, computeTimeGrid, timeFormatter}

import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import scala.collection.mutable

case class Cinema(name: CinemaName, screenings: Map[MovieTitle, Seq[LocalTime]]) {

  def hasMovies: Boolean = screenings.values.exists(_.nonEmpty)

  def html: String = {
    val timeGrid = computeTimeGrid(screenings).toSeq.sortBy { case (movieTitle, _) => movieTitle }
    s"""
       |<div class="timegrid" id="$name">
       |<p>$name</p>
       |<table>
       |<tbody id="liste-horaires">
       |${TimeGrid.timeGridToHtml(timeGrid)}
       |</tbody>
       |</table>
       |</div>""".stripMargin
  }
}

case class MovieRating(title: MovieTitle,
                       releaseDate: Option[LocalDate],
                       pressRating: Option[Double],
                       spectatorsRating: Option[Double])

case class MovieScreenings(title: String,
                           screenings: Map[(CinemaName, PostalCode), Seq[LocalTime]]) {
  def hasScreenings: Boolean = screenings.values.exists(_.nonEmpty)

  def html: String = {
    val timeGrid = computeTimeGrid(screenings).toSeq.sortBy {
      case ((cinemaName, postalCode), _) => postalCodeOrder(postalCode) -> cinemaName
    }
    s"""
       |<div class="timegrid" id="$title">
       |<p>$title</p>
       |<table>
       |<tbody id="liste-horaires">
       |${TimeGrid.timeGridToHtml(timeGrid)}
       |</tbody>
       |</table>
       |</div>""".stripMargin
  }
}

object Movies {
  def fromAllCinemas(cinemas: Seq[(Cinema, PostalCode)]): Seq[MovieScreenings] = {
    cinemas
      .flatMap {
        case (Cinema(cinemaName, movies), postalCode) =>
          movies.map { case (title, times) => title -> (cinemaName -> postalCode, times) }
      }
      .filter { case (_, (_, times)) => times.nonEmpty }
      .groupBy(_._1)
      .map { case (title, times) => MovieScreenings(title, times.map(_._2).toMap) }
      .toSeq
  }
}

object TimeGrid {
  type PostalCode = String
  type CinemaName = String
  type MovieTitle = String
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  /** Gives for each movie the values to put in each column of the time grid. */
  def computeTimeGrid[T](
      timesPerIdentifier: Map[T, Seq[LocalTime]]): Map[T, Seq[Option[LocalTime]]] = {

    // The starting time of each column of the time grid.
    val columnStartingTimes: Seq[LocalTime] = {
      timesPerIdentifier.toSeq
        .flatMap { case (id, times) => times.map(time => time -> id) }
        .sortBy(_._1)
        .foldLeft(List.empty[(LocalTime, Set[T])]) {
          case (Nil, (time, id)) => List(time -> Set(id))
          case ((lastTime, idsAtThisHour) :: tail, (time, movie)) =>
            if (idsAtThisHour.contains(movie) || time.getHour != lastTime.getHour)
              (time, Set(movie)) :: (lastTime, idsAtThisHour) :: tail
            else
              (lastTime, idsAtThisHour + movie) :: tail
        }
        .map { case (startingTimes, _) => startingTimes }
        .reverse // Necessary because of the pattern matching.
    }

    timesPerIdentifier.view.mapValues { times =>
      val columnsWithMovie = times
        .map(
          movieTime =>
            columnStartingTimes.view
              .filter(columnStartingTime =>
                columnStartingTime.isBefore(movieTime) || columnStartingTime.equals(movieTime))
              .max -> movieTime)
        .toMap
      columnStartingTimes.map(columnStartingTime => columnsWithMovie.get(columnStartingTime))
    }.toMap
  }

  def timeGridToHtml[T](timeGrid: Seq[(T, Seq[Option[LocalTime]])]): String = {
    timeGrid
      .map {
        case ((cinema, postalCode), times) => s"$cinema <i>($postalCode)</i>" -> times
        case _ @tuple                      => tuple
      }
      .map {
        case (id, times) =>
          s"""|<tr>
              |<td>$id</td> ${times
               .map {
                 case Some(time) => s"<td>${time.format(timeFormatter)}</td>"
                 case None       => "<td></td>"
               }
               .mkString(" ")}
              |</tr>""".stripMargin
      }
      .mkString("\n")
  }
}
