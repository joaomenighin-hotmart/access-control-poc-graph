package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.accesscontrolneptunepoc.model.Permission
import com.hotmart.accesscontrolneptunepoc.model.User
import com.hotmart.accesscontrolneptunepoc.vo.AddPermissionVO
import com.hotmart.accesscontrolneptunepoc.vo.AddSecurityGroupVO
import com.hotmart.neptunning.extension.ofType
import com.hotmart.neptunning.extension.toListMapping
import com.hotmart.neptunning.service.GraphService
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.hasLabel
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.out
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("user")
class UserController (val graphService: GraphService) {

    @GetMapping
    fun getUsers(u: User): Any {
        return graphService.findAll(u)
    }

    @PostMapping
    fun createUsers(@RequestBody u: User): String {
        graphService.addVertex(u)
        return "Added User $u"
    }

    @PutMapping
    fun updateUser(@RequestBody u: User): String {
        graphService.updateVertex(u)
        return "User updated to $u"

    }

    @DeleteMapping
    fun deleteUser(@RequestBody u: User): String {
        graphService.deleteVertex(u)
        return "User vertex with id ${u.id} deleted"
    }

    @PostMapping("/add-permission")
    fun addPermission(@RequestBody vo: AddPermissionVO): String {
        val g = graphService.g
        val w = g.V(vo.toGivePermissionId)
                .addE("has-permission")
                .to(g.V(vo.permissionId))
                .next()

        return "Permission added. Edge-result: ${w.id()}"
    }

    @PostMapping("/add-security-group")
    fun addSecurityGroup(@RequestBody vo: AddSecurityGroupVO): String {
        val g = graphService.g
        val w = g.V(vo.toGiveSecurityGroupId)
                .addE("is-in")
                .to(g.V(vo.securityGroupId))
                .next()

        return "Permission added. Edge-result: ${w.id()}"
    }

    @GetMapping("{userId}/permissions")
    fun getPermissions(@PathVariable userId: String): Any {
        val g = graphService.g
        return g.V(userId)
                .until(hasLabel<String>(Permission::class.simpleName))
                .repeat(out()).simplePath()
                .toListMapping(Permission::class)
    }

}