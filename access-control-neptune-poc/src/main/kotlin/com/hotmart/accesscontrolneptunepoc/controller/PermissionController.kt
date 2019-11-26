package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.accesscontrolneptunepoc.model.Permission
import com.hotmart.neptunning.extension.ofType
import com.hotmart.neptunning.extension.toListMapping
import com.hotmart.neptunning.service.GraphService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("permissions")
class PermissionController (val graphService: GraphService) {

    @GetMapping
    fun getPermissions(p: Permission): Any {
        return graphService.findAll(p)
    }

    @PostMapping
    fun createPermissions(@RequestBody p: Permission): String {
        graphService.addVertex(p)
        return "Added Permission $p"
    }

    @PutMapping
    fun updatePermission(@RequestBody p: Permission): String {
        graphService.updateVertex(p)
        return "Permission updated to $p"

    }

    @DeleteMapping
    fun deletePermission(@RequestBody p: Permission): String {
        graphService.deleteVertex(p)
        return "Permission vertex with id ${p.id} deleted"
    }

}