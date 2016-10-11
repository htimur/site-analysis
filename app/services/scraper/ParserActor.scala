package services.scraper

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.ScraperData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import services.scraper.extractor.Extractor

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object ParserActorProtocol {

  case class Parse(content: String, data: ScraperData)

  case class Result(data: ScraperData)
}

object ParserActor {
  def apply(extractors: List[Extractor[ScraperData]]): Props = Props(
    classOf[ParserActor],
    extractors
  )
}

class ParserActor(extractors: List[Extractor[ScraperData]]) extends Actor with ActorLogging {
  private case class Extract(receiver: ActorRef, document: Document, data: ScraperData)

  import ParserActorProtocol._

  override def receive = {
    case Parse(content, data) =>
      val document = Jsoup.parse(content)
      self ! Extract(sender(), document, data)
    case Extract(receiver, document, data) =>
      val result = extractors.foldRight(data) { case (f, intermediate) =>
        f(document, intermediate)
      }
      receiver ! Result(result)
  }
}
