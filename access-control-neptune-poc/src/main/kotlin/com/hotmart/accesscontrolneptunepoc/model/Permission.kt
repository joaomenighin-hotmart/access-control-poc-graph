package com.hotmart.accesscontrolneptunepoc.model

import com.hotmart.neptunning.annotation.Id
import com.hotmart.neptunning.annotation.Property
import com.hotmart.neptunning.annotation.Vertex

@Vertex
data class Permission (
       @Id
       val id: String? = null,

       @Property
       val code: String? = null
)