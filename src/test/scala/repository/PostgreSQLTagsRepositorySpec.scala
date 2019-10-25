package repository

import cats.effect.IO
import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}
import io.github.dpratt747.technical_notes.domain.adt.Tag
import io.github.dpratt747.technical_notes.infrastructure.repository.PostgreSQLTagsRepository
import org.scalatest._
import cats.implicits._
import org.scalatest.concurrent.ScalaFutures
import test_utils.FunSpecWithFixtures

final class PostgreSQLTagsRepositorySpec extends FunSpecWithFixtures with Matchers with ScalaFutures with OptionValues {

  describe("TagsRepository") {

    val kubernetesTagName = "Kubernetes"
    val dockerTagName = "Docker"
    val repo = new PostgreSQLTagsRepository[IO](connection)

    it("should add a tag and return an int representing the generated primary key"){ flyway =>
      flyway.migrate
      repo.insertTagOrGetExisting(kubernetesTagName).unsafeRunSync shouldEqual 1
      repo.insertTagOrGetExisting(dockerTagName).unsafeRunSync shouldEqual 2
    }

    it("should return the same id when a tag is inserted that already exists"){ flyway =>
      flyway.migrate
      repo.insertTagOrGetExisting(kubernetesTagName).unsafeRunSync shouldEqual 1
      repo.insertTagOrGetExisting(kubernetesTagName).unsafeRunSync shouldEqual 1
    }

    it("should retrieve a tag successfully by its name when the tag name exists"){ flyway =>
      flyway.migrate
      repo.insertTagOrGetExisting(dockerTagName).unsafeRunSync shouldEqual 1
      repo.getTagByName(dockerTagName).unsafeRunSync.value shouldEqual Tag(TagId(1).some, TagName(dockerTagName.toUpperCase))
    }

    it("should not retrieve a tag successfully by its name when the tag name does not exists"){ flyway =>
      flyway.migrate
      repo.getTagByName(dockerTagName).unsafeRunSync.isDefined shouldBe false
    }

    it("should retrieve a tag successfully by its id when the tag exists"){ flyway =>
      flyway.migrate
      repo.insertTagOrGetExisting(dockerTagName).unsafeRunSync shouldEqual 1
      repo.getTagById(1).unsafeRunSync.value shouldEqual Tag(TagId(1).some, TagName(dockerTagName.toUpperCase))
    }

    it("should not retrieve a tag successfully by its id when the id does not exists"){ flyway =>
      flyway.migrate
      repo.getTagById(1).unsafeRunSync.isDefined shouldBe false
    }

  }

}