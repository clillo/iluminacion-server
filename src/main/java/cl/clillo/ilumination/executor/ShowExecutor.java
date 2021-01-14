package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.model.Show;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ShowExecutor {

	@Autowired
	private List<Show> showList;

	public void tic() {
		if (Objects.isNull(showList))
			return;

		long now = System.currentTimeMillis();

		for (Show show : showList) {
			if (show.isExecuting() && show.getNextExecutionTime() < now) {
				show.getStepExecutor().execute(show);
			}

		}
	}

}
