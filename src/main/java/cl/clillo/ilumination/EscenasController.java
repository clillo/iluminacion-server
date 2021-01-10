package cl.clillo.ilumination;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EscenasController {

    @GetMapping("/ejecuta")
    public String ejecuta() {


        return "ok";
    }
}
