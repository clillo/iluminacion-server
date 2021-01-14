package cl.clillo.ilumination.resources;

import cl.clillo.ilumination.config.ShowsConfig;
import cl.clillo.ilumination.config.scenes.Scene;
import cl.clillo.ilumination.model.Show;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @GetMapping("/index")
    public String showList(Model model) {
        model.addAttribute("shows", showList);
        return "index";
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
    public String updateShow(@PathVariable("name") String name, Show show, BindingResult result, Model model) {
        final Optional<Show> optShow = getSHow(name);

        if(optShow.isPresent()) {
            optShow.get().setName(show.getName());
            optShow.get().setExecuting(show.isExecuting());

        }

        return "redirect:/edit/"+name;
    }

    @PostMapping("/update/{name}/scene/{id}")
    public String updateScene(@PathVariable("name") String name, @PathVariable("id") String id, Scene scene, BindingResult result, Model model) {
        final Optional<Show> optShow = getSHow(name);
        final Optional<Scene> optScene = getScene(id, optShow);

        if(optScene.isPresent()) {
            optScene.get().update(scene);
            showsConfig.write(optShow.get(), optScene.get());
        }

        return "redirect:/edit/"+name;
    }

    @GetMapping("/edit/{name}")
    public String showUpdateForm(@PathVariable("name") String name, Model model) {
        final Optional<Show> optShow = getSHow(name);

        if(optShow.isPresent()) {
            model.addAttribute("show", optShow.get());

        }

        return "update-show";
    }

    @GetMapping("/edit/{name}/scene/{id}")
    public String showUpdateSceneForm(@PathVariable("name") String name, @PathVariable("id") String id, Model model) {
        final Optional<Show> show = getSHow(name);
        final Optional<Scene> scene = getScene(id, show);

        if(scene.isPresent()) {
            model.addAttribute("scene", scene.get());

            List<Test> test = new ArrayList<>();
            model.addAttribute("test", test);
            List<Test> tests = Lists.newArrayList(
                    Test.builder().price(100).testCode("1").testName("uno").build(),
                    Test.builder().price(200).testCode("2").testName("dos").build(),
                    Test.builder().price(300).testCode("3").testName("tres").build()


            );
            model.addAttribute("tests", tests);
        }
        return "update-scene";
    }

    private Optional<Show> getSHow(final String name){
        for(Show show: showList){
            if(show.getName().equals(name)) {
                return Optional.of(show);
            }
        }
        return Optional.empty();
    }

    private Optional<Scene> getScene(final String id, final Optional<Show> show){
        if (show.isEmpty())
            return Optional.empty();

        for(Scene scene: show.get().getScenesLists()){
            if(scene.getId().equals(id))
                return Optional.of(scene);
        }
        return Optional.empty();
    }
}
