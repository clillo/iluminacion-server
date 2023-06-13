package cl.clillo.lighting.repository;

import cl.clillo.lighting.model.SceneEntity;
import org.springframework.data.repository.CrudRepository;

public interface  ScenesRepository extends CrudRepository<SceneEntity, Integer> {
}
