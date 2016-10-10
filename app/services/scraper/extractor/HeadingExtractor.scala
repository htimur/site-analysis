package services.scraper.extractor

import models.ScraperData
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object HeadingExtractor {
  def apply(): HeadingExtractor = new HeadingExtractor()
}

class HeadingExtractor extends Extractor[ScraperData] {
  override def apply(doc: Document, in: ScraperData): ScraperData = Option {
    doc.select("h1,h2,h3,h4,h5,h6").asScala
      .map(_.tagName())
      .foldLeft(Map.empty[String, Int]) { (m, x) => m + ((x, m.getOrElse(x, 0) + 1)) }
  } match {
    case headings@Some(_) => in.copy(headings = headings)
    case None => in
  }
}
