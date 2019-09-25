package persistence

import adt.tables.TagRow
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import persistence.TagsPersistence._
import test_utils.FunSpecWithFixtures

class TagsPersistenceSpec extends FunSpecWithFixtures with Matchers with ScalaFutures with OptionValues {

  describe("TagsPersistence") {

    it("should add one tag and return an int id"){ flyway =>
      flyway.migrate
      insertTag("Kubernetes").unsafeRunSync shouldEqual 1
      flyway.clean()
    }

    it("should throw an exception when the same tag is added twice"){ flyway =>
      flyway.migrate
      insertTag( "Kubernetes").unsafeRunSync shouldEqual 1
      val exception = the [Exception] thrownBy insertTag("Kubernetes").unsafeRunSync
      exception.getMessage should include("unique constraint")
      flyway.clean()
    }

    it("should retrieve a tag successfully by its name when the tag name exists"){ flyway =>
      flyway.migrate
      val tagName = "Docker"
      insertTag(tagName).unsafeRunSync shouldEqual 1
      getTagByName(tagName).unsafeRunSync.value shouldEqual TagRow(Some(1), tagName.toUpperCase)
      flyway.clean()
    }

    it("should not retrieve a tag successfully by its name when the tag name does not exists"){ flyway =>
      flyway.migrate
      val tagName = "Docker"
      getTagByName(tagName).unsafeRunSync.isDefined shouldBe false
      flyway.clean()
    }

    it("should retrieve a tag successfully by its id when the tag exists"){ flyway =>
      flyway.migrate
      val tagName = "Docker"
      insertTag(tagName).unsafeRunSync shouldEqual 1
      getTagById(1).unsafeRunSync.value shouldEqual TagRow(Some(1), tagName.toUpperCase)
      flyway.clean()
    }

    it("should not retrieve a tag successfully by its id when the id does not exists"){ flyway =>
      flyway.migrate
      getTagById(1).unsafeRunSync.isDefined shouldBe false
      flyway.clean()
    }

  }

}