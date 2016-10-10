package services.scraper.extractor

import org.jsoup.nodes.Document

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
trait Extractor[T] {
    def apply(doc: Document, in: T): T
}
