package org.apache.spark.orientdb.graphs

import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory

class DefaultSource( orientDBGraphVertexWrapper: OrientDBGraphVertexWrapper,
                     orientDBGraphEdgeWrapper: OrientDBGraphEdgeWrapper,
                     orientDBClientFactory: OrientDBCredentials => OrientDBClientFactory)
      extends RelationProvider
      with SchemaRelationProvider
      with CreatableRelationProvider {
  private val log = LoggerFactory.getLogger(getClass)

  def this() = this(DefaultOrientDBGraphVertexWrapper, DefaultOrientDBGraphEdgeWrapper,
                orientDBCredentials => new OrientDBClientFactory(orientDBCredentials))

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    val params = Parameters.mergeParameters(parameters)

    if (params.query.isDefined && (params.vertexType.isEmpty && params.edgeType.isEmpty)) {
      throw new IllegalArgumentException("Along with the 'query' parameter you must specify either 'vertexType' parameter or" +
        " 'edgeType' parameter or user-defined Schema")
    }

    if (params.vertexType.isDefined) {
      OrientDBVertexRelation(orientDBGraphVertexWrapper, orientDBClientFactory, params, None)(sqlContext)
    } else {
      OrientDBEdgeRelation(orientDBGraphEdgeWrapper, orientDBClientFactory, params, None)(sqlContext)
    }
  }

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String],
                              schema: StructType): BaseRelation = {
    val params = Parameters.mergeParameters(parameters)

    if (params.vertexType.isDefined) {
      OrientDBVertexRelation(orientDBGraphVertexWrapper, orientDBClientFactory, params, None)(sqlContext)
    } else {
      OrientDBEdgeRelation(orientDBGraphEdgeWrapper, orientDBClientFactory, params, None)(sqlContext)
    }
  }

  override def createRelation(sqlContext: SQLContext, mode: SaveMode,
                              parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val params = Parameters.mergeParameters(parameters)

    val vertexType = params.vertexType
    val edgeType = params.edgeType

    if (vertexType.isEmpty && edgeType.isEmpty) {
      throw new IllegalArgumentException("For save operations you must specify a OrientDB Graph Vertex" +
        " or Edge type with the 'vertexType' & 'edgeType' parameter respectively")
    }


    def tableExists: Boolean = {
      if (vertexType.isDefined) {
        try {
          orientDBGraphVertexWrapper.doesVertexTypeExists(vertexType.get)
        } finally {
          orientDBGraphEdgeWrapper.close()
        }
      }
      else {
        try {
          orientDBGraphEdgeWrapper.doesEdgeTypeExists(edgeType.get)
        } finally {
          orientDBGraphEdgeWrapper.close()
        }
      }
    }

    if (vertexType.isDefined) {
      val (doSave, dropExisting) = mode match {
        case SaveMode.Append => (true, false)
        case SaveMode.Overwrite => (true, true)
        case SaveMode.ErrorIfExists =>
          if (tableExists) {
            sys.error(s"Vertex type $vertexType already exists! (SaveMode is set to ErrorIfExists)")
          } else {
            (true, false)
          }
        case SaveMode.Ignore =>
          if (tableExists) {
            log.info(s"Vertex Type $vertexType already exists. Ignoring save requests.")
            (false, false)
          } else {
            (true, false)
          }
      }

      if (doSave) {
        val updatedParams = parameters.updated("overwrite", dropExisting.toString)
        new OrientDBVertexWriter(orientDBGraphVertexWrapper, orientDBClientFactory)
          .saveToOrientDB(data, mode, Parameters.mergeParameters(updatedParams))
      }
      createRelation(sqlContext, parameters)
    } else {
      try {
        orientDBGraphEdgeWrapper.doesEdgeTypeExists(edgeType.get)
      } finally {
        orientDBGraphEdgeWrapper.close()
      }

      val (doSave, dropExisting) = mode match {
        case SaveMode.Append => (true, false)
        case SaveMode.Overwrite => (true, true)
        case SaveMode.ErrorIfExists =>
          if (tableExists) {
            sys.error(s"Edge Type $edgeType already exists! (SaveMode is set to ErrorIfExists)")
          } else {
            (true, false)
          }
      }

      if (doSave) {
        val updatedParams = parameters.updated("overwrite", dropExisting.toString)
        new OrientDBEdgeWriter(orientDBGraphEdgeWrapper, orientDBClientFactory)
          .saveToOrientDB(data, mode, Parameters.mergeParameters(updatedParams))
      }
      createRelation(sqlContext, parameters)
    }
  }
}