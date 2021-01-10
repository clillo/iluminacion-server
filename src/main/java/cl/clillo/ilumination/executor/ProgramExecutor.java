package cl.clillo.ilumination.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ProgramExecutor {

	@Autowired
	private List<Program> programList;

	public void tic() {
		if (Objects.isNull(programList))
			return;

		long actual = System.currentTimeMillis();

		for (Program program : programList) {
			if (program.isExecuting() && program.getNextExecutionTime() < actual) {
				program.getStepExecutor().execute(program);
			}

		}
	}

}
