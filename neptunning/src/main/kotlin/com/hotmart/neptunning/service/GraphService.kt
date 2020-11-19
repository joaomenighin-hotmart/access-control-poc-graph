package com.hotmart.neptunning.service

import com.hotmart.neptunning.annotation.Id
import com.hotmart.neptunning.annotation.Property
import com.hotmart.neptunning.annotation.UniqueKey
import com.hotmart.neptunning.extension.ofType
import com.hotmart.neptunning.extension.toListMapping
import org.apache.tinkerpop.gremlin.driver.Client
import org.apache.tinkerpop.gremlin.driver.SigV4WebSocketChannelizer
import org.apache.tinkerpop.gremlin.driver.Cluster
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection
import org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerV3d0
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InvalidClassException
import java.security.InvalidKeyException
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberFunctions
import com.hotmart.neptunning.annotation.Vertex as NeptunningVertex
import org.apache.tinkerpop.gremlin.structure.T as GremlinT

@Service
class GraphService {

    @Value("\${neptunning.url}")
    lateinit var url: String

    @Value("\${neptunning.port}")
    var port: Int = 8182

    @Value("\${neptunning.enableSsl}")
    var ssl: Boolean = false

    private var _g: GraphTraversalSource? = null
    private var _cluster: Cluster? = null
    val g: GraphTraversalSource
        get() {
            if (_g == null || _cluster?.isClosed == true || _cluster?.isClosing == true) {
                var cluster: Cluster
                val clusterBuilder = Cluster.build()
                clusterBuilder.addContactPoint(url)
                clusterBuilder.port(port)
                clusterBuilder.channelizer(SigV4WebSocketChannelizer::class.java)
                clusterBuilder.enableSsl(ssl)
                clusterBuilder.serializer(GraphSONMessageSerializerV3d0())
                cluster = clusterBuilder.create()
                val client = cluster.connect<Client>()
                _g = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(client))
                //_g = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(_cluster));
            }

            return _g!!
        }


    /**
     * Apply "has" function with the given properties. Used to filter
     */
    private fun<T: Any> filterGraphProperties(op: GraphTraversal<*, *>, el: T, properties: List<KProperty1<T, *>>) {
        for (p in properties) {
            val propertyAnnotation = p.annotations.first { it is Property } as Property
            val propertyKey = if (propertyAnnotation.key.isEmpty()) p.name else propertyAnnotation.key

            val value = p.get(el)
            if (value != null)
                op.has(propertyKey, value)
        }
    }

    /**
     * Add the object properties to gremlin pipeline
     */
    private fun <T: Any> addGraphProperties(op: GraphTraversal<*, *>, el: T, setId: Boolean = true) {
        val t = el.javaClass.kotlin

        // The ID should not be set in some cases, like updating a Vertex
        if (setId) {
            val idProperty = t.declaredMemberProperties.firstOrNull { it.annotations.any { a -> a is Id } }
            if (idProperty != null) {
                val value = idProperty.get(el) as String?
                if (value != null && value.isNotEmpty())
                    op.property(GremlinT.id, value.toString())
            }
        }

        // Get all the properties and add them with Gremlin
        val properties = t.declaredMemberProperties.filter { it.annotations.any { a -> a is Property } }

        for (p in properties) {
            val propertyAnnotation = p.annotations.first { it is Property } as Property
            val propertyKey = if (propertyAnnotation.key.isEmpty()) p.name else propertyAnnotation.key

            if (propertyKey.toLowerCase() == "id")
                throw InvalidKeyException("A property with key \"Id\" cannot be set. To map it, use the @Id annotation")
            val value = p.get(el)
            if (value != null)
                op.property(VertexProperty.Cardinality.single, propertyKey, value)
        }
    }

    /**
     * Get the Vertex Id
     */
    private fun <T: Any> getVertexId(vertex: T): String {
        val t = vertex.javaClass.kotlin

        // Checks for Vertex annotation
        if (!t.annotations.any { it is NeptunningVertex })
            throw InvalidClassException("Class ${t.simpleName} does not have Vertex annotation")

        // Checks for Id annotation
        val idProperty = t.declaredMemberProperties.firstOrNull { it.annotations.any { a -> a is Id } }
                ?: throw InvalidClassException("Class ${t.simpleName} does not have an Id property")

        return idProperty.get(vertex) as String
    }

    fun <T: Any> findByUnique(vertex: T): List<T> {
        val t = vertex.javaClass.kotlin
        val uniqueProperties = t.declaredMemberProperties.filter { it.annotations.any { a -> a is UniqueKey } }
        val pipe = this.g.V()
        filterGraphProperties(pipe, vertex, uniqueProperties)
        return pipe.toListMapping(t)
    }

    fun <T: Any> findAll(vertex: T): List<T> {
        val t = vertex.javaClass.kotlin
        val props = t.declaredMemberProperties.filter { it.annotations.any { a -> a is Property } }
        val pipe = this.g.V().ofType(t)
        filterGraphProperties(pipe, vertex, props)
        return pipe.toListMapping(t)
    }

    fun <T: Any> addVertex(vertex: T) {
        // Get the type class and check if it has the Vertex annotation
        val t = vertex.javaClass.kotlin
        val vertexAnnotation = t.annotations.firstOrNull { it is NeptunningVertex } as NeptunningVertex?
                ?: throw InvalidClassException("Class ${t.simpleName} does not have Vertex annotation")

        val label = if (vertexAnnotation.label.isEmpty()) t.simpleName!! else vertexAnnotation.label

        // Start the Gremlin saving chain
        val addingV = this.g.addV(label)

        // Add the node properties on Gremlin pipeline
        addGraphProperties(addingV, vertex)

        // Execute query
        addingV.next()
    }

    fun <T: Any> updateVertex(vertex: T) {
        // Get the Id and update it on the graph
        val id = getVertexId(vertex)
        val updatingV = this.g.V(id)
        addGraphProperties(updatingV, vertex, false)
        updatingV.next()
    }

    fun <T: Any> deleteVertex(vertex: T) {
        // Get the vertex and delete it
        val id = getVertexId(vertex)
        g.V(id).drop().iterate()
    }
}