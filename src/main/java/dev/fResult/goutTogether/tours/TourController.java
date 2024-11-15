package dev.fResult.goutTogether.tours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/tours")
public class TourController {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(3);
    private final Logger logger = LoggerFactory.getLogger(TourController.class);

    private final Map<Long, Tour> toursInMemDb;

    public TourController() {
        toursInMemDb = new HashMap<>(
            Map.of(
                    1L, new Tour(1L, "Bali 5 days", 50),
                    2L, new Tour(2L, "Kunlun 7 days", 50)
            )
        );
    }

    @GetMapping
    public List<Tour> all() {
        logger.info("All Tours");
        return toursInMemDb.values().stream().toList();
    }

    @GetMapping("/{id}")
    public Tour byId(@PathVariable long id) {
        var tour = Optional.ofNullable(toursInMemDb.get(id)).orElseThrow(() -> {
            logger.error("tourId: {} not found", id);
            return new RuntimeException("Not found");
        });
        logger.info("Get tourId: {}", id);

        return tour;
    }

    @PostMapping
    public Tour create(@RequestBody Tour body) {
        var idToCreate = ID_GENERATOR.getAndIncrement();
        var tourToCreate = new Tour(idToCreate, body.title(), body.maxPeople());
        toursInMemDb.put(idToCreate, tourToCreate);
        logger.info("Create tour: {}", tourToCreate);
        return toursInMemDb.get(tourToCreate.id());
    }

    @PutMapping("/{id}")
    public Tour update(@PathVariable long id, @RequestBody Tour body) {
        Optional.ofNullable(toursInMemDb.get(id)).orElseThrow(() -> {
            logger.error("tourId: {} not found", id);
            return new RuntimeException("Not found");
        });
        var tourToUpdate = new Tour(id, body.title(), body.maxPeople());
        logger.info("Update rour: {}", tourToUpdate);

        return tourToUpdate;
    }
}

