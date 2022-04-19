package org.glx.graphdatabase.utils.nebula.ldbc

import org.glx.graphdatabase.utils.CommonUtils

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
 * @program: LDBC-Trans
 * @description:
 * @author: LiamGao
 * @create: 2022-04-18 09:35
 */
object NebulaLDBCTrans {
  private var srcDir: File = _
  var srcDynamicDir: File = _
  var srcStaticDir: File = _

  private var targetDir: File = _
  var targetNodesDir: File = _
  var targetRelationshipDir: File = _

  def main(args: Array[String]): Unit = {
    val headLinesMap = Source.fromResource("headLines_nebula.txt").getLines()
      .map(f => f.split("="))
      .map(f => (f.head, f.last))
      .toMap

    srcDir = new File(args(0))
    targetDir = new File(args(1))

    srcDynamicDir = new File(s"$srcDir/dynamic")
    srcStaticDir = new File(s"$srcDir/static")

    targetNodesDir = new File(s"$targetDir/nodes")
    if (!targetNodesDir.exists()) targetNodesDir.mkdirs()

    targetRelationshipDir = new File(s"$targetDir/relations")
    if (!targetRelationshipDir.exists()) targetRelationshipDir.mkdirs()

    val nodeFilesSet: Set[File] = {
      val set = srcDynamicDir.listFiles().filter(file => isNodeFile(file.getName)).toSet ++ srcStaticDir.listFiles().filter(file => isNodeFile(file.getName)).toSet
      set
    }
    val relFilesSet: Set[File] = {
      val set = srcDynamicDir.listFiles().filter(file => isRelationFile(file.getName)).toSet ++ srcStaticDir.listFiles().filter(file => isRelationFile(file.getName)).toSet
      set
    }
    val nodesFuture = nodeFilesSet.map(f => Future{
      new NodeFileHandler(f, headLinesMap).trans()
    })
    val relationFuture = relFilesSet.map(f => Future{
      new RelationshipHandler(f, headLinesMap).trans()
    })
    nodesFuture.foreach(f => Await.result(f, Duration.Inf))
    relationFuture.foreach(f => Await.result(f, Duration.Inf))
  }

  def isNodeFile(name: String): Boolean ={
    name.split("_").length == 3
  }
  def isRelationFile(name: String): Boolean = {
    name.split("_").length == 5
  }
}
