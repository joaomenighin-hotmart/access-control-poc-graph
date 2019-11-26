package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.neptunning.service.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("graph")
class GraphController {

    @Autowired
    lateinit var graphService: GraphService

    @PostMapping("/clear-graph")
    fun clearGraph(): String {
        graphService.g.V().drop().iterate()
        return "R.I.P. Graph"
    }
}