package org.glx.graphdatabase.utils.pandadb.ldbc

import org.glx.graphdatabase.utils.CommonUtils

import java.io.File
import java.util.concurrent.atomic.AtomicLong
import scala.io.Source

/** @author:John117
  * @createDate:2022/6/5
  * @description:
  */
object MetaData {
  val nodeHeaderMap: Map[String, String] = getHeaderMap("nodeHeadLines.txt")
  val relationHeaderMap: Map[String, String] = getHeaderMap(
    "relationshipHeadLines.txt"
  )

  val relationId = new AtomicLong(0)
  val nodeIdPrefix: Long = 100000000000000L

  var labelSerialMap: Map[String, Int] = Map[String, Int]()
  var labelSerialIndex: Int = 1

  def getLabelSerialNum(label: String): Int = {
    this.synchronized {
      if (labelSerialMap.contains(label)) labelSerialMap(label)
      else {
        labelSerialMap += (label -> labelSerialIndex)
        val serialNum = labelSerialIndex
        labelSerialIndex += 1
        serialNum
      }
    }
  }
  def getTransferId(srcId: Long, label: String): Long = {
    srcId + getLabelSerialNum(label) * nodeIdPrefix
  }

  def getTransferId(srcId: Long, serialNum: Int): Long = {
    srcId + nodeIdPrefix * serialNum
  }

  def getHeaderMap(txtName: String): Map[String, String] = {
    val f =
      s"${CommonUtils.getModuleRootPath}/src/main/resources/$txtName"
    val headerFile = Source.fromFile(new File(f))
    val mp = headerFile
      .getLines()
      .toSeq
      .map(f => f.split("="))
      .map(f => (f.head, f.last))
      .toMap
    headerFile.close()
    mp
  }
}