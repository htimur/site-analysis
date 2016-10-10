package services.scraper

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.routing.SmallestMailboxPool
import akka.util.Timeout
import models.{ScraperData, ScraperResult}
import play.api.cache.CacheApi
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import services.scraper.extractor._

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
trait Scraper {
  type Url = String

  /**
    * Returns a future Json of scraping
    *
    * @param url Url to scrape
    * @return Future Json
    */
  def scrape(url: Url): Future[ScraperResult]
}

@Singleton
final class UrlScraper @Inject()(system: ActorSystem, config: Configuration, cache: CacheApi, ws: WSClient) extends Scraper {

  import ParserActorProtocol._
  import system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val logger = Logger(classOf[UrlScraper])
  val extractors = List(HtmlVersionExtractor(), HeadingExtractor(), LinkExtractor(), TitleExtractor(), LoginFormExtractor())

  lazy val poolSize = config.getInt("scraper.pool-size").getOrElse(1)
  lazy val cacheTtl = config.getInt("scraper.cache-ttl-seconds").getOrElse(1).seconds
  lazy val metadataScraperActorsRoundRobin = system.actorOf(ParserActor(extractors).withRouter(SmallestMailboxPool(poolSize)), "scraper-router")

  /**
    * Returns cache value
    *
    * @param url parse url
    * @return Optional cached value
    */
  private def getCached(url: Url): Future[Option[ScraperResult]] = Future {
    cache.get[ScraperResult](cacheKey(url))
  }

  /**
    * Throws a given value into the cache
    *
    * @param value value to be cached
    */
  private def setCache(url: Url, value: ScraperResult) = {
    cache.set(cacheKey(url), value, cacheTtl)
  }

  /**
    * Returns a Json object for a passed in ScrapedData message
    *
    * @param data ScrapedData message object
    * @return JsValue
    */
  private def scraperResult(response: WSResponse, data: Option[ScraperData]): ScraperResult =
  if (response.status == 200) ScraperResult("Successfully parsed", data)
  else ScraperResult(s"Status ${response.status} ${response.statusText}", data)

  /**
    * Returns a cache key for the current url
    *
    * @return String Cache key
    */
  private def cacheKey(url: Url): String = s"urlCacheKey.$url"

  /**
    * Parse response
    *
    * @param promise  communication promise
    * @param url      original url
    * @param response response from url
    */
  private def parseResponse(promise: Promise[ScraperResult], url: Url, response: WSResponse): Unit = {
    response.status match {
      case 200 =>
        val scraperData = ScraperData(url)
        val futureResult = ask(
          metadataScraperActorsRoundRobin,
          Parse(response.body, scraperData)
        )

        futureResult onComplete {
          case Failure(fail) => promise.failure(fail)
          case Success(Result(data)) =>
            val scrapedData = scraperResult(response, Some(data))
            Future {
              setCache(url, scrapedData)
            }
            promise.complete(Success(scrapedData))
          case any =>
            val error = s"Unsupported value received $any"
            logger.warn(error)
            promise.failure(new IllegalArgumentException(error))
        }
      case other =>
        logger.warn(s"Unsuccessful response code ${response.status} ${response.statusText}")
        promise.complete(Success(scraperResult(response, None)))
    }
  }

  /**
    * Returns a future result of scraping
    *
    * @param url Url to scrape
    * @return Future result
    */
  override def scrape(url: Url): Future[ScraperResult] = {
    getCached(url) flatMap {
      case Some(jsValue) => Future.successful(jsValue)
      case None =>
        val promise = Promise[ScraperResult]()

        ws.url(url).get() onComplete {
          case Success(response) => parseResponse(promise, url, response)
          case Failure(error) =>
            promise.failure(error)
        }

        promise.future
    }
  }
}