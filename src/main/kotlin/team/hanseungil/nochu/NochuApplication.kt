package team.hanseungil.nochu

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class NochuApplication

fun main(args: Array<String>) {
	runApplication<NochuApplication>(*args)
}
