package org.glx.graphdatabase.utils.pandadb.ldbc

import java.io.File
import java.util.concurrent.{Executors, TimeUnit}
import scala.io.Source

/** @program: LDBC-Trans
  * @description:
  * @author: LiamGao
  * @create: 2022-06-04 20:35
  */

object PandaDBLDBCTrans {
  def main(args: Array[String]): Unit = {
    val srcPath = args(0)
    val outPath = args(1)

    val dir = new File(srcPath)
    val check = dir
      .listFiles()
      .filter(f => f.getName == "dynamic" || f.getName == "static")
    if (check.length != 2) {
      throw new Exception("need dynamic dir and static dir")
    }

    val watcher = Executors.newSingleThreadScheduledExecutor()
    watcher.scheduleAtFixedRate(
      new Thread(new Runnable {
        override def run(): Unit = {
          println(s"Transfer Nodes: ${MetaData.getGlobalNodes}")
          println(s"Transfer Relationships: ${MetaData.getGlobalRelationships}")
        }
      }),
      0,
      30,
      TimeUnit.SECONDS
    )

    val files = getNodeAndRelFiles(srcPath)
    val np = new NodeHandler(files._1, outPath)
    np.processNodes()
    val rp = new RelationshipHandler(files._2, outPath)
    rp.processRels()
    watcher.shutdown()
    println()
    println(
      s"Transfer Total Nodes: ${MetaData.totalNodesCount}, Transfer Total Relationships: ${MetaData.totalRelationshipsCount}"
    )
  }

  def getNodeAndRelFiles(dirPath: String): (Array[File], Array[File]) = {
    val dir = new File(dirPath)
    val files: Array[File] =
      dir.listFiles().flatMap(f => f.listFiles())
    val nodes: Array[File] = files.filterNot(f => f.getName.contains("_")) ++
      files.filter(f =>
        f.getName == "Person_email_EmailAddress" || f.getName == "Person_speaks_Language"
      )
    val relationships: Array[File] = {
      files.filter(f =>
        f.getName.contains(
          "_"
        ) && f.getName != "Person_email_EmailAddress" && f.getName != "Person_speaks_Language"
      )
    }
    (nodes, relationships)
  }
}
