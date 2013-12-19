package com.betbuddy.util

import java.sql._
import grizzled.slf4j.Logging
import javax.sql.DataSource
import org.apache.tomcat.dbcp.dbcp.BasicDataSource
import collection.mutable.ListBuffer
import scala.util.control.Exception.ignoring
import org.joda.time.{ReadableDateTime, DateTime}
import com.betbuddy.config.DatabaseConfig
import resource._

object JDBCHelpers extends Logging {

  def createDataSource(config: DatabaseConfig): DataSource = {
    // TODO: Proper pooled connection factory
    val ds = new BasicDataSource()
    ds.setDriverClassName(config.driverClass)
    ds.setUrl(config.urlWithProperties)
    ds.setUsername(config.user)
    ds.setPassword(config.password)
    ds.setValidationQuery(config.validationQuery)
    ds.setMaxActive(config.maxActive)
    ds
  }

  def withTransactionalConnection[T](dataSource: DataSource)(f: Connection => T): T = {
    implicit def connectionResourceWithRollback[A <: java.sql.Connection] = new Resource[A] {
      override def open(r: A) = r.setAutoCommit(false)
      override def close(r: A) = {
        r.setAutoCommit(true)
        r.close()
      }
      override def toString = "Resource[java.sql.Connection]"
      override def closeAfterException(r: A, t: Throwable) = {
        ignoring(classOf[SQLException]) apply {
          r.rollback()
        }
        close(r)
      }
    }

    managed(dataSource.getConnection) acquireAndGet { conn =>
      val result = f(conn)
      // Allow exceptions caused by commit to bubble up by committing outside of scala-arm,
      // which is configured by default to ignore exceptions that occur during cleanup
      conn.commit()
      result
    }
  }

  def withConnection[T](dataSource: DataSource)(f: Connection => T): T = {
    managed(dataSource.getConnection) acquireAndGet { conn =>
      f(conn)
    }
  }

  def execute(conn: Connection, sql: String) {
    logger.debug(sql)
    managed(conn.prepareStatement(sql)) acquireAndGet { stmt =>
      stmt.execute
    }
  }

  def update(conn: Connection, sql: String, params: Seq[_] = Nil): Int = {
    logger.debug(sql + "\n params:" + params)
    managed(conn.prepareStatement(sql)) acquireAndGet { stmt =>
      setParams(stmt, params)
      stmt.executeUpdate()
    }
  }

  def query[T](conn: Connection, sql: String, params: Seq[_] = Nil)(f: ResultSet => T): List[T] = {
    logger.debug(sql + "\n params:" + params)
    managed(conn.prepareStatement(sql)) acquireAndGet { stmt =>
      setParams(stmt, params)
      val result = ListBuffer[T]()
      for (rs <- managed(stmt.executeQuery)) {
        while (rs.next) {
          result += f(rs)
        }
      }
      result.toList
    }
  }

  def find[T](conn: Connection, sql: String, params: Seq[_] = Nil)(f: ResultSet => T): Option[T] = {
    query(conn, sql, params)(f).headOption
  }

  // Option-to-SQL bridge; extract from Some, convert None to NULL
  def setParams(stmt: PreparedStatement, params: Seq[_]) {
    for ((param, index) <- params.zipWithIndex) {
      val sqlParam = param match {
        case Some(s) => s
        case None => null
        case _ => param
      }
      stmt.setObject(index + 1, sqlParam)
    }
  }
}

abstract class JDBCRepository(val dataSource: DataSource) {

  def find[T](queryString: String, params: Seq[_] = Nil)(implicit mapper: ResultSet => T): Option[T] = {
    // TODO: warn if cardinality is not what it should be?
    try {
      JDBCHelpers.withConnection(dataSource) { conn =>
        JDBCHelpers.find(conn, queryString, params) { rs =>
          mapper(rs)
        }
      }
    }
    catch {
      case ex: SQLException => {
        throw new Exception("Unable to query user with queryString: " + queryString, ex)
      }
    }
  }

  def query[T](queryString: String, params: Seq[_] = Nil)(implicit mapper: ResultSet => T): List[T] = {
    try {
      JDBCHelpers.withConnection(dataSource) { conn =>
        JDBCHelpers.query(conn, queryString, params) { rs =>
          mapper apply rs
        }
      }
    }
    catch {
      case ex: SQLException => {
        throw new Exception("Unable to query user with queryString: " + queryString, ex)
      }
    }
  }
}

object DateTimeConversion {
  def timestampAsDate(seconds: Int): Option[DateTime] = {
    if (seconds == 0) None // null is treated as zero by ResultSet
    else Some(new DateTime(seconds * 1000L))
  }

  def dateAsTimestamp(d: DateTime) = (d.getMillis / 1000L).asInstanceOf[Int]
}