package org.glx.graphdatabase.utils.nebula.ldbc

import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

/**
 * @program: LDBC-Trans
 * @description:
 * @author: LiamGao
 * @create: 2022-04-18 14:56
 */
class RelationshipHandler (relationFile: File, headLinesMap: Map[String, String]) extends LazyLogging{
  val relationFileName = relationFile.getName.split("_0_0.csv").head
  val header = headLinesMap.get(relationFileName)
  if (header.isEmpty) throw new Exception(s"not find header for relationship file: $relationFileName.csv")

  val targetFile = new File(s"${NebulaLDBCTrans.targetRelationshipDir}/$relationFileName.csv")

  def trans(): Unit ={
    println(s"--- START TRANS $relationFileName.csv ---")
    val reader = Source.fromFile(relationFile, 1024 * 1024 * 200)
    val iter = reader.getLines()
    iter.next()

    val writer = new BufferedWriter(new FileWriter(targetFile), 1024 * 1024 * 100)
    writer.write(header.get)
    writer.newLine()

    while (iter.hasNext){
      val line = iter.next()
      writer.write(line)
      writer.newLine()
    }

    writer.flush()
    writer.close()
    reader.close()
    println(s"--- FINISH TRANS $relationFileName.csv ---")
    println()
  }
}
