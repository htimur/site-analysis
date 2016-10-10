package services.scraper.extractor

import models.ScraperData
import org.jsoup.nodes.{Document, DocumentType}

import scala.collection.JavaConverters._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object HtmlVersionExtractor {
  def apply(): HtmlVersionExtractor = new HtmlVersionExtractor()
}
class HtmlVersionExtractor extends Extractor[ScraperData] {
  override def apply(doc: Document, in: ScraperData): ScraperData = {
    doc
      .childNodes()
      .asScala.find(_.isInstanceOf[DocumentType]) match {
      case Some(node) =>
        val version = if (node.attr("publicid").isEmpty) {
          "HTML5"
        } else {
          "HTML4"
        }
        in.copy(htmlVersion = Some(version))
      case None => in
    }
  }
}
