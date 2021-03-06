package de.springbootbuch.messaging_amqp.film_rental;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Part of springbootbuch.de.
 *
 * @author Michael J. Simons
 * @author @rotnroll666
 */
@RestController
public class RentalController {
	
	public static class ReturnedFilm {
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
	
	private final InventoryRepository inventoryRepository;
	
	private final AmqpTemplate amqpTemplate;

	public RentalController(InventoryRepository inventoryRepository, AmqpTemplate amqpTemplate) {
		this.inventoryRepository = inventoryRepository;
		this.amqpTemplate = amqpTemplate;
	}

	@PostMapping("/returnedFilms")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void returnFilm(
		@RequestBody ReturnedFilm returnedFilm) {
		final FilmInStore filmInStore = 
			this.inventoryRepository
				.save(new FilmInStore(returnedFilm.getTitle()));

		amqpTemplate.convertAndSend(
			"returned-film-events", 
			new FilmReturnedEvent(
				filmInStore.getId(), 
				filmInStore.getTitle()
			)
		);
	}
}
