package spaceInvaders.util
import scalikejdbc._
import spaceInvaders.model.Highscore

trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"

  val dbURL = "jdbc:derby:myDB;create=true;";
  // initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)

  ConnectionPool.singleton(dbURL, "me", "mine")

  // ad-hoc session provider on the REPL
  implicit val session: DBSession = AutoSession


}
object Database extends Database{
  def setupDB() = {
      if (!hasDBInitialize) //if highscore dont exist
          Highscore.initializeTable()   //create table
}

  def hasDBInitialize : Boolean = {

    DB getTable "highscore" match {
      case Some(x) => true
      case None => false
    }

  }
}