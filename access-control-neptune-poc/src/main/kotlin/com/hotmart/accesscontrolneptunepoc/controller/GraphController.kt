package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.accesscontrolneptunepoc.model.Permission
import com.hotmart.accesscontrolneptunepoc.model.SecurityGroup
import com.hotmart.accesscontrolneptunepoc.model.User
import com.hotmart.accesscontrolneptunepoc.vo.AddPermissionVO
import com.hotmart.accesscontrolneptunepoc.vo.AddSecurityGroupVO
import com.hotmart.neptunning.service.GraphService
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.*
import org.apache.tinkerpop.gremlin.structure.Element
import org.omg.CORBA.NO_PERMISSION
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.apache.tinkerpop.gremlin.structure.T as GremlinT

@RestController
@RequestMapping("graph")
class GraphController {

    @Autowired
    lateinit var graphService: GraphService

    @Autowired
    lateinit var securityGroupsController: SecurityGroupsController

    @Autowired
    lateinit var permissionController: PermissionController

    @Autowired
    lateinit var userController: UserController

    var logger: Logger = LoggerFactory.getLogger(GraphController::class.java)

    private val N_USERS: Int = 50;
    private val N_PERMISSIONS: Int = 400;
    private val N_SECURITY_GROUPS: Int = 100;


    @PostMapping("/seed-graph")
    fun seed(): String {

        logger.warn("Inserting users...")
        for (i in 0 until N_USERS) {
            userController.createUsers(User("u$i", i, "User $i"))
        }

        logger.warn("Done users. Inserting Permissions...")
        for (i in 0 until N_PERMISSIONS) {
            permissionController.createPermissions(Permission("p$i", "Permission $i"))
        }

        logger.warn("Done permissions. Inserting Security Groups...")
        for (i in 0 until N_SECURITY_GROUPS) {
            securityGroupsController.createSecurityGroups(SecurityGroup("sg$i", "Security Group $i"))
        }

        logger.warn("Done Security groups. Inserting Edges: permission-security...")
        for (i in 0 until N_SECURITY_GROUPS) {
            for (j in 0 until 3) {
                val rd = (0 until N_PERMISSIONS).shuffled().first()
                securityGroupsController.addPermission(AddPermissionVO("sg$i", "p$rd"))
            }
        }

        logger.warn("Inserting Edges: security-security...")
        for (i in 0 until N_SECURITY_GROUPS step 2) {
            val rd = (0 until N_SECURITY_GROUPS).shuffled().first()
            securityGroupsController.addSecurityGroup(AddSecurityGroupVO("sg$i", "sg$rd"))
        }

        logger.warn("Inserting Edges: permission-user...")
        for (i in 0 until N_USERS) {
            for (j in 0 until 2) {
                val rd = (0 until N_PERMISSIONS).shuffled().first()
                userController.addPermission(AddPermissionVO("u$i", "p$rd"))
            }
        }

        logger.warn("Inserting Edges: user-sg...")
        for (i in 0 until N_USERS) {
            for (j in 0 until 3) {
                val rd = (0 until N_SECURITY_GROUPS).shuffled().first()
                userController.addSecurityGroup(AddSecurityGroupVO("u$i", "sg$rd"))
            }
        }

        return "Graph created"
    }

    @GetMapping("/get-graph")
    fun getGraph(): Any {
        return mapOf(
                "nodes" to graphService.g.V()
                        .project<Any>("id", "label", "group")
                        .by(GremlinT.id)
                        .by(
                                union<Any, Any>(
                                    id<Element>(),
                                    coalesce<Any, Any>(
                                            values<Element, Any>("code"),
                                            values<Element, Any>("name")
                                    )
                                ).fold()
                        )
                        .by(GremlinT.label)
                        .toList(),
                "edges" to graphService.g.E()
                        .project<Any>("id","from","to")
                        .by(GremlinT.id)
                        .by(outV().id())
                        .by(inV().id())
                        .toList()
        )
    }

    @PostMapping("/clear-graph")
    fun clearGraph(): String {
        graphService.g.V().drop().iterate()
        return "R.I.P. Graph"
    }
}