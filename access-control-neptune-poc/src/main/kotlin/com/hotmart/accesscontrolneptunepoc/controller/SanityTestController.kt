package com.hotmart.accesscontrolneptunepoc.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sanity-test")
class SanityTestController {

    @GetMapping
    fun sanityTest(): String {
        return "Not crazy yet";
    }
}