package cl.clillo.lighting.controller;


import cl.clillo.lighting.external.dmx.ArtNet;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArtNetController {

    private final ArtNet artNet = ArtNet.getInstance();
    @PostMapping("dmx/{channel}/{data}")
    public void send(final @PathVariable("channel")int channel, final @PathVariable("data") int data){
        artNet.send(channel, data);

    }
    @Scheduled(fixedRate = 1)
    public void tic(){
        artNet.broadCast();
    }
}
