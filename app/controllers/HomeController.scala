package controllers

import java.net.URL
import javax.inject._

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import services.scraper.Scraper

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(scraper: Scraper, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  import models._

  val logger = play.Logger.of(classOf[HomeController])

  val form = Form(
    mapping(
      "url" -> nonEmptyText(5)
    )(ScrapeInput.apply)(ScrapeInput.unapply).verifying(
      "Valid url should be provided", { input: ScrapeInput => Try {
        new URL(input.url)
      } match {
        case Failure(error) =>
          logger.error("URL error", error)
          false
        case Success(_) => true
      }
      }
    ))

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok(views.html.index(form))
  }

  def scrapeUrl = Action.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        logger.error("Form error")
        Future.successful(Ok(views.html.index(formWithErrors)))
      },
      userInput => {
        scraper.scrape(userInput.url).map {
          case r@ScraperResult(message, None) => Ok(views.html.index(form, Some(r)))
          case r@ScraperResult(message, Some(_)) => Ok(views.html.result(r))
        }
      }
    )
  }

}
