package com.hotmart.accesscontrolneptunepoc.controller

import com.hotmart.accesscontrolneptunepoc.model.User
import com.hotmart.neptunning.extension.ofType
import com.hotmart.neptunning.extension.toListMapping
import com.hotmart.neptunning.service.GraphService
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

}