package services.scraper.extractor

import models.ScraperData
import org.jsoup.nodes.Document

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object TitleExtractor {
  def apply(): TitleExtractor = new TitleExtractor()
}

class TitleExtractor extends Extractor[ScraperData] {
  override def apply(doc: Document, in: ScraperData): ScraperData = Option {
    doc.select("title").text()
  } match {
    case title@Some(_) => in.copy(title = title)
    case None => in
  }
}
