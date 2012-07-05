package chc

import java.util.UUID
import scala.collection.mutable.HashMap

object Installer {

  def main(args: Array[String]) {

    val defaults = Map(
      "dbhost"  -> "localhost",
      "dbport"  -> "3306",
      "dbname"  -> "emperor",
      "dbuser"  -> "root",
      "dbpass"  -> ""
    )

    val config = getConfig(defaults)

    writeConfig(config)
  }

  def getConfig(defaults: Map[String,String]): Map[String,String] = {

    defaults.map { v =>
      val input = Console.readLine(v._1 + " [" + defaults(v._1) + "]: ")
      val realValue = input match {
        case "" => defaults(v._1)
        case iv => iv
      }
      (v._1 -> realValue)
    }
  }

  def writeConfig(config: Map[String,String]) {

    val secret = UUID.randomUUID.toString.replace("-","")
    println("application.secret=\"" + secret.toString + "\"")
    println("db.default.driver=com.mysql.jdbc.Driver")
    println("db.default.url=\"jdbc:mysql://" + config("dbhost") + ":" + config("dbport") + "/" + config("dbpass") + "?useUnicode=yes&characterEncoding=UTF-8&useOldAliasMetadataBehavior=true\"")
    println("db.default.user=" + config("dbuser"))
    println("db.default.password=\"" + config("dbpass") + "\"")
  }
}