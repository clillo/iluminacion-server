package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Step {

    private boolean nextExecution;
    private long nextExecutionTime;
    private String description;

    private List<Point> pointList;
}
