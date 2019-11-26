package com.hotmart.accesscontrolneptunepoc

import com.hotmart.neptunning.annotation.EnableNeptunning
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableNeptunning
class AccessControlNeptunePocApplication

fun main(args: Array<String>) {
    runApplication<AccessControlNeptunePocApplication>(*args)
}
