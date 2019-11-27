package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.accesscontrolneptunepoc.model.SecurityGroup
import com.hotmart.accesscontrolneptunepoc.vo.AddPermissionVO
import com.hotmart.accesscontrolneptunepoc.vo.AddSecurityGroupVO
import com.hotmart.neptunning.extension.ofType
import com.hotmart.neptunning.extension.toListMapping
import com.hotmart.neptunning.service.GraphService
import org.apache.tinkerpop.gremlin.structure.T
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/security-groups")
class SecurityGroupsController(val graphService: GraphService) {

    @GetMapping
    fun getSecurityGroups(sg: SecurityGroup): Any {
        return graphService.findAll(sg)
    }

    @PostMapping
    fun createSecurityGroups(@RequestBody sg: SecurityGroup): String {
        val a = graphService.g.addV(sg.javaClass.simpleName)
                graphService.addVertex(sg)
        return "Added Security Group $sg"
    }

    @PutMapping
    fun updateSecurityGroup(@RequestBody sg: SecurityGroup): String {
        graphService.updateVertex(sg)
        return "Security Group updated to $sg"

    }

    @DeleteMapping
    fun deleteSecurityGroup(@RequestBody sg: SecurityGroup): String {
        graphService.deleteVertex(sg)
        return "Security Group vertex with id ${sg.id} deleted"
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
                .addE("has-parent")
                .to(g.V(vo.securityGroupId))
                .next()

        return "Security group added. Edge-result: ${w.id()}"
    }

}