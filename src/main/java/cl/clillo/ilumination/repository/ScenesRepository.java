package cl.clillo.ilumination.repository;

import cl.clillo.ilumination.model.SceneEntity;
import org.springframework.data.repository.CrudRepository;

public interface  ScenesRepository extends CrudRepository<SceneEntity, Integer> {
}
