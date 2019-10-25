package endpoint

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}
import org.scalatest.{Assertion, Matchers}

protected object Http4sServiceCheck extends Matchers{

  /**
   * @param actual provide the IO response here
   * @param expectedStatus provide the expected status to assert against
   * @return an assertion against the status code
   */
  def apply(actual: IO[Response[IO]], expectedStatus: Status): Assertion = {
    val actualResp = actual.unsafeRunSync
    actualResp.status shouldEqual expectedStatus
  }

  /**
   * @param actual provide the IO response here
   * @param expectedStatus provide the expected status to assert against
   * @param expectedBody provide the expected body to assert against wrapped in a some
   * @param ev implicit entity decoder
   * @tparam A the adt for json decoding and encoding
   * @return an assertion against the json body and will also validate status prior to returning
   */
  def apply[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
    implicit ev: EntityDecoder[IO, A]
  ): Assertion = {
    val actualResp = actual.unsafeRunSync
    actualResp.status shouldEqual expectedStatus
    expectedBody.fold[Assertion](actualResp.body.compile.toVector.unsafeRunSync.isEmpty shouldEqual true)( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync shouldEqual expected
    )
  }


}
