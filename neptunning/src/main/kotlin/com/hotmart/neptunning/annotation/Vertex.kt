package com.hotmart.neptunning.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Vertex (val label: String = "")