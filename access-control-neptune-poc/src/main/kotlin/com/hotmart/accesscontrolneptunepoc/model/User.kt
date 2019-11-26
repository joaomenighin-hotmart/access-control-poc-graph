package com.hotmart.accesscontrolneptunepoc.model

import com.hotmart.neptunning.annotation.Id
import com.hotmart.neptunning.annotation.Property
import com.hotmart.neptunning.annotation.Vertex

@Vertex
data class User (
       @Id
       val id: String? = null,

       @Property
       val marketplaceId: Int? = null,

       @Property
       val name: String? = null
)