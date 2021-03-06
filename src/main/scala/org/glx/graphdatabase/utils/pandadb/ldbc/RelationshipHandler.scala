package org.glx.graphdatabase.utils.pandadb.ldbc

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

/** @author:John117
  * @createDate:2022/6/5
  * @description:
  */
class RelationshipHandler(
    relFiles: Array[File],
    outPath: String,
    metaData: MetaData
) {
  val path = s"$outPath/rels/"
  val relFile = new File(path)
  if (!relFile.exists()) relFile.mkdirs()

  val relMap = Map(
    "hasMember" -> "HAS_MEMBER",
    "hasTag" -> "HAS_TAG",
    "replyOf" -> "REPLY_OF",
    "isLocatedIn" -> "IS_LOCATED_IN",
    "hasCreator" -> "HAS_CREATOR",
    "hasInterest" -> "HAS_INTEREST",
    "workAt" -> "WORK_AT",
    "hasModerator" -> "HAS_MODERATOR",
    "containerOf" -> "CONTAINER_OF",
    "likes" -> "LIKES",
    "knows" -> "KNOWS",
    "studyAt" -> "STUDY_AT",
    "hasType" -> "HAS_TYPE",
    "isPartOf" -> "IS_PART_OF",
    "isSubclassOf" -> "IS_SUBCLASS_OF"
  )

  def processRels(): Unit = {
    var globalRelId: Long = 0
    var globalCounter: Int = 0

    relFiles.foreach(file => {
      val writer =
        new BufferedWriter(
          new FileWriter(s"$path/${file.getName}.csv"),
          1024 * 1024 * 100
        )

      val header = metaData.relationHeaderMap(file.getName)
      val nameArr = file.getName.split("_")
      val leftLabel = nameArr.head
      val relationType = relMap(nameArr(1))
      val rightLabel = nameArr.last

      val leftIndex = header.split("\\|").indexOf(":START_ID")
      val rightIndex = header.split("\\|").indexOf(":END_ID")

      val newHeader = s"REL_ID|:TYPE|$header"
      writer.write(newHeader)
      writer.newLine()

      val dataFiles = file
        .listFiles()
        .filter(p => p.getName != "_SUCCESS" && !p.getName.contains("crc"))
      dataFiles.foreach(dataFile => {
        val dataSource = Source.fromFile(dataFile)
        val iter = dataSource.getLines()
        iter.next()
        while (iter.hasNext) {
          globalCounter += 1
          if (globalCounter % 50000 == 0) {
            globalCounter = 0
            metaData.addGlobalRels(50000)
          }
          val lineArr = iter.next().split("\\|")

          val leftId = lineArr(leftIndex).toLong
          val rightId = lineArr(rightIndex).toLong
          lineArr(leftIndex) = metaData
            .getTransferId(leftId, transferSubType2ParentType(leftLabel))
            .toString
          lineArr(rightIndex) = metaData
            .getTransferId(rightId, transferSubType2ParentType(rightLabel))
            .toString

          globalRelId += 1
          writer.write(s"$globalRelId|$relationType|${lineArr.mkString("|")}")
          writer.newLine()
        }
      })
      writer.flush()
      writer.close()
    })
    metaData.addGlobalRels(globalCounter)
  }

  def transferSubType2ParentType(typeName: String): String = {
    typeName match {
      case "City"       => "Place"
      case "Country"    => "Place"
      case "Continent"  => "Place"
      case "University" => "Organisation"
      case "Company"    => "Organisation"
      case _            => typeName
    }
  }
}
