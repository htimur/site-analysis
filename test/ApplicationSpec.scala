import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Site Analysis")
    }

    "accept the post requsts" in {
      val body = "url" -> "http://github.com"
      val home = route(app, FakeRequest(POST, "/").withFormUrlEncodedBody(body)).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Successfully parsed")
    }

  }

}
