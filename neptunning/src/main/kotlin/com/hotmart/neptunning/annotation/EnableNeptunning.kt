package com.hotmart.neptunning.annotation

import com.hotmart.neptunning.NeptunningConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(NeptunningConfiguration::class)
annotation class EnableNeptunning