package uz.nodir.billing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "uz.nodir.*"
    ]
)
@ConfigurationPropertiesScan
class BillingApplication

fun main(args: Array<String>) {
    runApplication<BillingApplication>(*args)
}
