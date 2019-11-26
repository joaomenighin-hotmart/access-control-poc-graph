package com.hotmart.neptunning.extension

import com.hotmart.neptunning.annotation.Id
import com.hotmart.neptunning.annotation.Property
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__` as GremlinFunctions
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.io.InvalidClassException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import org.apache.tinkerpop.gremlin.structure.T as GremlinT
import com.hotmart.neptunning.annotation.Vertex as NeptunningVertex

/**
 * Queries for vertex with the label of type
 */
inline fun <T: Any> GraphTraversal<Vertex, Vertex>.ofType(clazz: KClass<T>): GraphTraversal<Vertex, Vertex> {
    val vertexAnnotation = clazz.annotations.firstOrNull { it is com.hotmart.neptunning.annotation.Vertex } as com.hotmart.neptunning.annotation.Vertex?
            ?: throw InvalidClassException("Class ${clazz.simpleName} does not have Vertex annotation")

    val label = if (vertexAnnotation.label.isEmpty()) clazz.simpleName!! else vertexAnnotation.label

    return this.hasLabel(label)
}

/**
 * Executes the query and maps to a list of T
 * @return list of T
 */
inline fun <T: Any> GraphTraversal<Vertex, *>.toListMapping(clazz: KClass<T>): List<T> {
    val data = this
            .valueMap<Any>()
            .with(WithOptions.tokens)
            .by(GremlinFunctions.unfold<Any>())
            .toList()

    val res = mutableListOf<T>()

    if (!clazz.annotations.any { it is NeptunningVertex })
        throw InvalidClassException("Class ${clazz.simpleName} does not have Vertex annotation")

    // Get the right constructor to use
    val neptunningProps = clazz.declaredMemberProperties.filter { it.annotations.any { a -> a is Property || a is Id } }
    val ctor = clazz.constructors.firstOrNull {
        it.parameters.size == neptunningProps.size &&
                neptunningProps.map { p -> p?.name }.containsAll(it.parameters.map { p -> p?.name })
    } ?: throw Exception("Class ${clazz.simpleName} must have a constructor with all ${neptunningProps.size} Neptunning parameters")

    // Iterate through data converting it to object
    for (el in data) {
        // Hold the constructor parameter value in order
        val ctorParametersValues = mutableMapOf<KParameter, Any>()

        for (param in ctor.parameters) {

            val prop = neptunningProps.first{ it?.name == param.name }!!

            if (prop.annotations.any { it is Property }) {
                val propertyAnnotation = prop.annotations.first { it is Property } as Property
                val propertyKey = if (propertyAnnotation.key.isEmpty()) prop.name else propertyAnnotation.key

                if (el.containsKey(propertyKey) && el[propertyKey] != null)
                    ctorParametersValues[param] = el[propertyKey]!!
            } else if (prop.annotations.any { it is Id }) {
                ctorParametersValues[param] = el[GremlinT.id]!!
            }
        }

        // Call the constructor and add it to list
        res.add(ctor.callBy(ctorParametersValues))
    }

    return res
}