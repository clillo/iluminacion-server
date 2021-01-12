package cl.clillo.ilumination.resources;

import cl.clillo.ilumination.config.ShowsConfig;
import cl.clillo.ilumination.model.Show;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ui.Model;

@Controller
@Log4j2
public class ShowController {

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private List<Show> showList;

    @Autowired
    private ShowsConfig showsConfig;

    @GetMapping("/programs")
    @ResponseBody
    public List<Show> getProgramList() {
        return showList;
    }

    @GetMapping("/program")
    @ResponseBody
    public Show getProgram(int index) {
        return showList.get(index);
    }


    //creating put mapping that updates the book detail
    @PutMapping("/programupdate")
    private void update(@RequestBody Show show) {
        log.info("Actualizando: "+ show.getName());

    }

    @GetMapping("/index")
    public String showUserList(Model model) {
        model.addAttribute("shows", showList);
        return "index";
    }

    @GetMapping("/signup")
    public String showSignUpForm(Show show) {
        return "add-user";
    }

    @PostMapping("/adduser")
    public String addUser(Show show, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-user";
        }

      //  userRepository.save(user);
        return "redirect:/index";
    }

    @PostMapping("/update/{name}")
    public String updateUser(@PathVariable("name") String name, Show show, BindingResult result, Model model) {
        final Optional<Show> showi = getSHow(name);

        if(showi.isPresent()) {
            showi.get().setName(show.getName());
            showi.get().setExecuting(show.isExecuting());
        }


        return "redirect:/index";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, Model model) {
     //   User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    //    userRepository.delete(user);

        return "redirect:/index";
    }

    @GetMapping("/edit/{name}")
    public String showUpdateForm(@PathVariable("name") String name, Model model) {
        final Optional<Show> show = getSHow(name);

        if(show.isPresent()) {
            model.addAttribute("show", show.get());

        }

        return "update-show";
    }

    @GetMapping("/edit/{name}/scene/{id}")
    public String showUpdateSceneForm(@PathVariable("name") String name, @PathVariable("id") String id, Model model) {

        return "redirect:/index";
    }

    private Optional<Show> getSHow(final String name){
        for(Show show: showList){
            if(show.getName().equals(name)) {
                return Optional.of(show);
            }
        }
        return Optional.empty();
    }
}
