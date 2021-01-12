package cl.clillo.ilumination.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "show")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowEntity {

    @Id
    @GeneratedValue
    private int showId;

    @Column
    private String name;

    @OneToMany
    @JoinColumn(name = "showId")
    private Set<SceneEntity> scenes;
}
