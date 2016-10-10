import javax.inject._

import play.api.http.HttpFilters

@Singleton
class Filters @Inject() extends HttpFilters {
  def filters = Seq()
}
