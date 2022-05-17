package com.creditas.poccache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
@EnableCaching
class PocCacheApplication

fun main(args: Array<String>) {
	runApplication<PocCacheApplication>(*args)
}

// Cache manager Caffeine gerenciador a parte
@Configuration
class AppConfig {
	@Bean
	fun cacheManager(): CacheManager = CaffeineCacheManager("Card")
		.apply {
			isAllowNullValues = false
			setCaffeine(
				Caffeine.newBuilder()
					.maximumSize(100)
					.expireAfterAccess(1, TimeUnit.HOURS)
			)
		}
}
// Fim Cache manager Caffeine

@Entity
data class Card (
	@Id
	val id: Long,
	val name: String
)

interface CardRepository : CrudRepository<Card, Long>

@Service
@CacheConfig(cacheNames = ["Card"])
class CardService(val cardRepository: CardRepository) {

	@Cacheable(key = "#id")
	fun getCardById(id: Long) = cardRepository.findById(id)

	@CacheEvict(key = "#id")
	fun update(id: Long, card: Card) {
		val existingCard = cardRepository.findById(id)
		val updatedCard = existingCard.get().copy(name = card.name)
		cardRepository.save(updatedCard)
	}
}

@RestController
class CardController(val cardService: CardService) {

	@GetMapping("/{id}")
	fun getCardById(@PathVariable id: Long) = cardService.getCardById(id)

	@GetMapping("/{id}/update")
	fun updateUser(@PathVariable id: Long) {
		//cardService.update(id, Card(id, UUID.randomUUID().toString()))
		cardService.update(id, Card(id, "Creditas"))
	}
}


