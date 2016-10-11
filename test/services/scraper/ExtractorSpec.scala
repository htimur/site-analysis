package services.scraper

import models.{Links, ScraperData}
import org.jsoup.Jsoup
import org.scalatest._
import services.scraper.extractor._

import scala.io.Source

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class ExtractorSpec extends FlatSpec with Matchers {
  val html = Source.fromURL(getClass.getResource("/login.html")).mkString
  val data = ScraperData("http://github.com")
  val document = Jsoup.parse(html)

  "HeadingExtractor" should "extract headings" in {
    val extractor = HeadingExtractor()
    extractor.apply(document, data) shouldBe data.copy(headings = Some(Map("h1" -> 1)))
  }

  "TitleExtractor" should "extract title" in {
    val extractor = TitleExtractor()
    extractor.apply(document, data) shouldBe data.copy(title = Some("Sign in to GitHub · GitHub"))
  }

  "HtmlVersionExtractor" should "extract version" in {
    val html4 = Jsoup.parse("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">")
    val extractor = HtmlVersionExtractor()
    extractor.apply(html4, data) shouldBe data.copy(htmlVersion = Some("HTML4"))
    extractor.apply(document, data) shouldBe data.copy(htmlVersion = Some("HTML5"))
  }

  "LinkExtractor" should "extract links" in {
    val extractor = LinkExtractor()
    val extractedData = extractor.apply(document, data)
    val links = extractedData.links
    links shouldBe a[Some[_]]
    links.get.externalLinkCount shouldBe 0
    links.get.internalLinkCount shouldBe 10
  }

  "LoginFormExtractor" should "extract login form existence" in {
    val noform = Jsoup.parse("<html><head><title>First parse</title></head>\"\n  + \"<body><p>Parsed HTML into a doc.</p></body></html>")
    val extractor = LoginFormExtractor()
    extractor.apply(document, data) shouldBe data.copy(haveLoginForm = true)
    extractor.apply(noform, data) shouldBe data.copy(haveLoginForm = false)
  }
}
