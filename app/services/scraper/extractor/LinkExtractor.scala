package services.scraper.extractor

import models.{Links, ScraperData}
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object LinkExtractor {
  def apply(): LinkExtractor = new LinkExtractor()
}
class LinkExtractor extends Extractor[ScraperData] {
  override def apply(doc: Document, in: ScraperData): ScraperData = Option {
    doc.select("a[href]").asScala.map(_.attr("href")).toList
  } match {
    case Some(links) => in.copy(links = Some(Links(in.url, links)))
    case None => in
  }
}
