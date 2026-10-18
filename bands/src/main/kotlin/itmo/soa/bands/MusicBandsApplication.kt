package itmo.soa.bands

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MusicBandsApplication

fun main(args: Array<String>) {
    SpringApplication.run(MusicBandsApplication::class.java, *args)
}
