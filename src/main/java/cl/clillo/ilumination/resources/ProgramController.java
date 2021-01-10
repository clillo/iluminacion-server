package cl.clillo.ilumination.resources;

import cl.clillo.ilumination.executor.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class ProgramController {

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private List<Program> programList;

    @GetMapping("/programs")
    @ResponseBody
    public List<Program> getProgramList() {
        return programList;
    }

    @GetMapping("/program")
    @ResponseBody
    public Program getProgram(int index) {
        return programList.get(index);
    }

    @PostMapping("/program")
    @ResponseBody
    public boolean updateProgram(int index, boolean executing) {
        final Program program = programList.get(index);
        program.setExecuting(executing);
        return true;
    }
}
