package cl.clillo.lighting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "scene")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneEntity {

    @Id
    @GeneratedValue
    private int sceneId;

    private String name;
    private long duration;

    @ManyToOne
    @JoinColumn(name = "showId", insertable = false, updatable = false)
    private ShowEntity show;

    @OneToMany
    @JoinColumn(name = "sceneId")
    private Set<SceneNodeEntity> sceneNodes;
}
