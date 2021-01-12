package cl.clillo.ilumination.repository;

import cl.clillo.ilumination.model.SceneEntity;
import cl.clillo.ilumination.model.ShowEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShowsRepository extends CrudRepository<ShowEntity, Integer> {
}
