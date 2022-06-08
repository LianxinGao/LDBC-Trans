package org.glx.graphdatabase.utils.pandadb.ldbc

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

/** @author:John117
  * @createDate:2022/6/5
  * @description: 10 node files
  */
class NodeHandler(nodesFile: Array[File], outPath: String, metaData: MetaData) {
  val path = s"$outPath/nodes/"
  val nodeFile = new File(path)
  if (!nodeFile.exists()) nodeFile.mkdirs()

  val messageDir: Array[File] =
    nodesFile.filter(f =>
      f.getName == "Comment" || f.getName == "Post"
    ) // 2 files
  val orgAndPlaceDir: Array[File] = nodesFile.filter(f =>
    f.getName == "Organisation" || f.getName == "Place"
  ) //2 files
  val personDir: Array[File] = nodesFile.filter(f =>
    f.getName.contains("Person")
  ) // 3 file: person, email, speaks
  val normalDir: Array[File] = nodesFile.filter(f =>
    f.getName == "Forum" || f.getName == "Tag" || f.getName == "TagClass"
  )

  def processNodes(): Unit = {
    val f1 = Future { processPersonFiles() }
    val f2 = Future { processMessageFiles() }
    val f3 = Future { processNormalFiles() }
    val f4 = Future { processOrgAndPlaceFiles() }
    val futures = Seq(f1, f2, f3, f4)
    futures.foreach(f => Await.result(f, Duration.Inf))
  }

  private def processNormalFiles(): Unit = {
    normalDir.foreach(dir => {
      val files =
        dir
          .listFiles()
          .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))
//          .head // data all in one csv
      val label = dir.getName
      val header = s"${metaData.nodeHeaderMap(label)}"
      val idIndex = header.split("\\|").indexOf("id:long")

      val writer = new BufferedWriter(
        new FileWriter(new File(s"$path/$label.csv")),
        1024 * 1024 * 100
      )
      val newHeader = s":ID|:LABEL|$header"
      writer.write(newHeader)
      writer.newLine()

      var nodeCount: Int = 0
      files.foreach(file => {
        val sf = Source.fromFile(file)
        val iter = sf.getLines()
        iter.next()

        while (iter.hasNext) {
          nodeCount += 1
          if (nodeCount % 20000 == 0) {
            nodeCount = 0
            metaData.addGlobalNodes(20000)
          }

          val lineArr = iter.next().split("\\|")
          val id = lineArr(idIndex)

          val transId = metaData.getTransferId(id.toLong, label)
          lineArr(idIndex) = transId.toString

          val toWrite = s"$transId|$label|${lineArr.mkString("|")}"
          writer.write(toWrite)
          writer.newLine()
        }
      })

      writer.flush()
      writer.close()
      metaData.addGlobalNodes(nodeCount)
    })
  }
  private def processPersonFiles(): Unit = {
    val emailFile = personDir
      .filter(f => f.getName.contains("email"))
      .head
      .listFiles()
      .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))

    val speaksFile = personDir
      .filter(f => f.getName.contains("speaks"))
      .head
      .listFiles()
      .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))

    val personFile = personDir
      .filter(f => f.getName == "Person")
      .head
      .listFiles()
      .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))

    val emailMap = getEmailOrSpeakIDMap(emailFile)
    val speaksMap = getEmailOrSpeakIDMap(speaksFile)

    val writer = new BufferedWriter(
      new FileWriter(new File(s"$path/Person.csv")),
      1024 * 1024 * 100
    )
    processPersonFile(personFile, emailMap, speaksMap, writer)
  }
  private def processOrgAndPlaceFiles(): Unit = {
    orgAndPlaceDir.foreach(dir => {
      val files =
        dir
          .listFiles()
          .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))
//          .head // data all in one csv
      val label = dir.getName
      val header = s"${metaData.nodeHeaderMap(label)}"
      val idIndex = header.split("\\|").indexOf("id:long")
      val typeIndex = header.split("\\|").indexOf("type")
      val newHeader = s":ID|:LABEL|$header"

      val writer = new BufferedWriter(
        new FileWriter(new File(s"$path/$label.csv")),
        1024 * 1024 * 100
      )
      writer.write(newHeader)
      writer.newLine()

      var nodeCount: Int = 0
      files.foreach(file => {
        val sf = Source.fromFile(file)
        val iter = sf.getLines()
        iter.next()
        while (iter.hasNext) {
          nodeCount += 1
          if (nodeCount % 20000 == 0) {
            nodeCount = 0
            metaData.addGlobalNodes(20000)
          }
          val lineArr = iter.next().split("\\|")
          val id = lineArr(idIndex)
          val innerLabel = lineArr(typeIndex)
          val transId = metaData.getTransferId(id.toLong, label)
          lineArr(idIndex) = transId.toString

          val toWrite = s"$transId|$innerLabel|${lineArr.mkString("|")}"
          writer.write(toWrite)
          writer.newLine()
        }
      })

      writer.flush()
      writer.close()
      metaData.addGlobalNodes(nodeCount)
    })
  }
  private def processMessageFiles(): Unit = {
    messageDir.foreach(dir => {
      val files =
        dir
          .listFiles()
          .filter(f => f.getName != "_SUCCESS" && !f.getName.contains("crc"))
//          .head // data all in one csv
      val label = dir.getName
      val header = s"${metaData.nodeHeaderMap(label)}"
      val idIndex = header.split("\\|").indexOf("id:long")

      val writer = new BufferedWriter(
        new FileWriter(new File(s"$path/$label.csv")),
        1024 * 1024 * 100
      )
      val newHeader = s":ID|:LABEL|$header"
      writer.write(newHeader)
      writer.newLine()

      var nodeCount: Int = 0
      files.foreach(file => {
        val sf = Source.fromFile(file)
        val iter = sf.getLines()
        iter.next()
        while (iter.hasNext) {
          nodeCount += 1
          if (nodeCount % 20000 == 0) {
            nodeCount = 0
            metaData.addGlobalNodes(20000)
          }

          val lineArr = iter.next().split("\\|")
          val id = lineArr(idIndex)

          val transId = metaData.getTransferId(id.toLong, label)
          lineArr(idIndex) = transId.toString

          val toWrite = s"$transId|$label,Message|${lineArr.mkString("|")}"
          writer.write(toWrite)
          writer.newLine()
        }
      })
      writer.flush()
      writer.close()
      metaData.addGlobalNodes(nodeCount)
    })
  }

  private def processPersonFile(
      personFile: Array[File],
      emailIdMap: Map[Long, ArrayBuffer[String]],
      speaksIdMap: Map[Long, ArrayBuffer[String]],
      writer: BufferedWriter
  ): Unit = {
    val label = "Person"
    val header = metaData.nodeHeaderMap(label)
    val idIndex = header.split("\\|").indexOf("id:long")

    val newHeader = s":ID|:LABEL|$header|email|speaks"
    writer.write(newHeader)
    writer.newLine()

    var nodeCount: Int = 0
    personFile.foreach(ff => {
      val file = Source.fromFile(ff)
      val iter = file.getLines()
      iter.next()
      while (iter.hasNext) {
        nodeCount += 1
        if (nodeCount % 20000 == 0) {
          nodeCount = 0
          metaData.addGlobalNodes(20000)
        }

        val lineArr = iter.next().split("\\|")
        val pId = lineArr(idIndex).toLong

        val transferId = metaData.getTransferId(pId, label)
        lineArr(idIndex) = transferId.toString
        val email = emailIdMap.get(transferId).map(f => f.mkString(","))
        val speaks = speaksIdMap.get(transferId).map(f => f.mkString(","))
        val toWrite = s"$transferId|$label|${lineArr.mkString("|")}|${email
          .getOrElse("")}|${speaks.getOrElse("")}"

        writer.write(toWrite)
        writer.newLine()
      }
    })
    writer.flush()
    writer.close()
    metaData.addGlobalNodes(nodeCount)
  }

  private def getEmailOrSpeakIDMap(
      infoFiles: Array[File]
  ): Map[Long, ArrayBuffer[String]] = {
    var map: Map[Long, ArrayBuffer[String]] = Map.empty

    infoFiles.foreach(ff => {
      val file = Source.fromFile(ff)
      val iter = file.getLines()
      iter.next()
      while (iter.hasNext) {
        val data = iter.next().split("\\|")
        val id = data(1).toLong
        val email = data(2)

        val transferId = metaData.getTransferId(id, "Person")

        if (map.contains(transferId)) {
          map(transferId).append(email)
        } else {
          map += transferId -> ArrayBuffer(email)
        }
      }
      file.close()
    })
    map
  }
}
