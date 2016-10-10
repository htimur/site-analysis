package services.scraper.extractor

import models.ScraperData
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object LoginFormExtractor {
  def apply(): LoginFormExtractor = new LoginFormExtractor()
}

class LoginFormExtractor extends Extractor[ScraperData]{
  override def apply(doc: Document, in: ScraperData): ScraperData = Option {
    doc.select("form").forms().asScala.map { form =>
      form.select("input").asScala.filter { input:Element =>
        input.attr("name") == "login" ||
          input.attr("name") == "user" ||
          input.attr("name") == "username" ||
          input.attr("name") == "pwd" ||
          input.attr("name") == "password"
      }
    }.filter(_.size > 1)
  } match {
    case Some(forms) =>
      in.copy(haveLoginForm = if (forms.nonEmpty) true else false)
    case None => in
  }
}
