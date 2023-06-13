package cl.clillo.lighting.repository;

import cl.clillo.lighting.model.ShowEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShowsRepository extends CrudRepository<ShowEntity, Integer> {
}
