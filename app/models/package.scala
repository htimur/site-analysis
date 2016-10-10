import java.net.URI

import play.api.libs.json.{JsValue, Json, Writes}

import scala.util.Try

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
package object models {
  case class ScrapeInput(url: String)


  case class Links(pageUrl: String, links: List[String]) {
    private val host:String = Try {
      val url = new URI(pageUrl)
      url
    }.map(_.getHost).get

    private val parsedLinks = links.flatMap { link =>
      Try {
        new URI(link)
      }.toOption
    }

    val internalLinks = parsedLinks.filter { link => link.getHost == host || link.getHost == null }
    val internalLinkCount = internalLinks.size
    val externalLinks = parsedLinks.filter { link => link.getHost != host && link.getHost != null }
    val externalLinkCount = externalLinks.size
  }

  case class ScraperData(url: String,
                         htmlVersion: Option[String] = None,
                         title: Option[String] = None,
                         headings: Option[Map[String, Int]] = None,
                         links: Option[Links] = None,
                         haveLoginForm: Boolean = false)

  case class ScraperResult(message: String, data: Option[ScraperData] = None)

  implicit val links = new Writes[Links] {
    override def writes(o: Links): JsValue = Json.obj(
      "internal_links" -> o.internalLinks.map(_.toString),
      "internal_lings_count" -> o.internalLinkCount,
      "external_links" -> o.externalLinks.map(_.toString),
      "external_links_count" -> o.externalLinkCount
    )
  }

  implicit val scraperData = new Writes[ScraperData] {
    override def writes(o: ScraperData): JsValue = Json.obj(
      "url" -> o.url,
      "html_version" -> o.htmlVersion,
      "title" -> o.title,
      "headings" -> o.headings,
      "links" -> o.links,
      "hav_login_form" -> o.haveLoginForm
    )
  }

  implicit val scraperResult = new Writes[ScraperResult] {
    override def writes(o: ScraperResult): JsValue = Json.obj(
      "message" -> o.message,
      "data" -> o.data
    )
  }
}
