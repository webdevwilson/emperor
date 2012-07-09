package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class TicketLinkTypeModelSpec extends Specification {

  import models.TicketLinkTypeModel

  "Ticket Link Type model" should {

    "create, retrieve and delete" in {
      running(FakeApplication()) {

        val s = models.TicketLinkType(
          name = "Ticket Link Type 1",
          dateCreated = new Date
        )
        val newLinkType = TicketLinkTypeModel.create(s)
        newLinkType must beAnInstanceOf[models.TicketLinkType]

        // Get it by id
        val linkType = TicketLinkTypeModel.getById(newLinkType.id.get)
        linkType must beSome
        linkType.get must beAnInstanceOf[models.TicketLinkType]

        // // Change it
        val cLinkType = linkType.get.copy(name = "Test Link Type 1!")
        val updatedLinkType = TicketLinkTypeModel.update(cLinkType.id.get, cLinkType)
        updatedLinkType must beSome
        updatedLinkType.get.name mustEqual "Test Link Type 1!"

        // Nix it
        TicketLinkTypeModel.delete(newLinkType.id.get)
        val gone =  TicketLinkTypeModel.getById(newLinkType.id.get)
        gone must beNone
      }
    }
  }
}
