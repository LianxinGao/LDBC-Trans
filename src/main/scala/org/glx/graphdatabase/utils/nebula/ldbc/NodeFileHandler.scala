package org.glx.graphdatabase.utils.nebula.ldbc

import com.typesafe.scalalogging.LazyLogging

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

/**
 * @program: LDBC-Trans
 * @description:
 * @author: LiamGao
 * @create: 2022-04-18 14:40
 */
class NodeFileHandler(nodeFile: File, headLinesMap: Map[String, String]) extends LazyLogging{
  val nodeFileName = nodeFile.getName.split("_").head
  val header = headLinesMap.get(nodeFileName)
  if (header.isEmpty) throw new Exception(s"not find header for node file: $nodeFileName.csv")

  val targetFile = new File(s"${NebulaLDBCTrans.targetNodesDir}/$nodeFileName.csv")

  def trans(): Unit ={
    println(s"--- START TRANS $nodeFileName.csv ---")
    val reader = Source.fromFile(nodeFile, 1024 * 1024 * 200)
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
    println(s"--- FINISH TRANS $nodeFileName.csv ---")
    println()
  }
}
