package services.scraper

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import services.scraper.extractor._

import scala.io.Source
import scala.util.Success
import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class ParserActorSpec extends TestKit(ActorSystem("MySpec")) with FlatSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll {
  import ParserActorProtocol._
  import akka.pattern.ask

  implicit val timeout = akka.util.Timeout(1.seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val html = Source.fromURL(getClass.getResource("/login.html")).mkString
  val data = ScraperData("http://github.com")

  val extractors = List(HtmlVersionExtractor(), HeadingExtractor(), LinkExtractor(), TitleExtractor(), LoginFormExtractor())

  "ParserActor" should "parse and return result" in {
    val actor = TestActorRef(new ParserActor(extractors))
    val future = actor ? Parse(html, data)
    val Success(result: Result) = future.value.get

    val resultData = result.data
    resultData.url shouldBe "http://github.com"
    resultData.htmlVersion shouldBe Some("HTML5")
    resultData.haveLoginForm shouldBe true
    resultData.headings shouldBe Some(Map("h1" -> 1))

    resultData.links shouldBe a[Some[Links]]
    val links = resultData.links.get
    links.externalLinkCount shouldBe 0
    links.internalLinkCount shouldBe 10
  }
}
