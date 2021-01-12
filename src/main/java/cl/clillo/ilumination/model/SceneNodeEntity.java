package cl.clillo.ilumination.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "sceneNode")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneNodeEntity {

    @Id
    @GeneratedValue
    private int stepId;

    @ManyToOne
    @JoinColumn(name = "sceneId", insertable = false, updatable = false)
    private SceneEntity scene;

    private String fixture;
    private int dimmer;
    private int speed;
    private String lights;
    private String positions;

}
