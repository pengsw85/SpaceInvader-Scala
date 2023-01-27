package spaceInvaders.model

import java.time.LocalDate;
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.FloatProperty
import scalafx.beans.property.IntegerProperty
import spaceInvaders.util.Database
import scalikejdbc._
import scalafx.beans.property.ObjectProperty
import scala.util.{ Try, Success, Failure }

class Highscore (val highscoreS : Float) extends Database {
	def this()     = this(0.0f)
    var highscore     = ObjectProperty[Float](highscoreS)

	def save() : Try[Long] = {
		// save new entry in database
		Try(DB autoCommit { implicit session => 
			sql"""
				INSERT INTO highscore (highscore) VALUES (${highscore.value})
			""".update.apply()
		})
				
	}
    
	def isExist : Boolean =  {
		// check if entry exist based on highscore value
		DB readOnly { implicit session =>
			sql"""
				select * from highscore where 
				highscore = ${highscore.value}
			""".map(rs => rs.string("highscore")).single.apply()
		} match {
			case Some(_) => true
			case None => false
		}
	}
}

object Highscore extends Database{
  	val highscoreData = new ObservableBuffer[Highscore]()

	def apply (
		_id: Integer,
		highscoreS : Float, 
		) : Highscore = {
		new Highscore(highscoreS) {
			highscore.value     = highscoreS
		}
	} 

	def initializeTable() = {
		// initialise SQL table
		DB autoCommit { implicit session => 
			sql"""
			create table Highscore (
			  id int, 
			  highscore float
			)
			""".execute.apply()
		}
	}

    def allHighscores : List[Highscore] = {
		// get all highscore in list
        val table = SQLSyntax.createUnsafely("highscore")
		DB readOnly { implicit session =>
			sql"select * from ${table}".map( {rs => 
        new Highscore( rs.float("highscore")) } ).list.apply()
		}
	}

	def delete() : Try[Int] = {
		// delete all entry in table highscore
		Try(DB autoCommit { implicit session => 
		sql"""
			DELETE FROM highscore 
			""".update.apply()
		})
	
	}
}
