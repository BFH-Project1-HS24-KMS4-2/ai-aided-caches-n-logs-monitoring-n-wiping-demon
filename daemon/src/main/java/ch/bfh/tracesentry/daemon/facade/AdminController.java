package ch.bfh.tracesentry.daemon.facade;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/kill")
    public void kill() {
        System.exit(0);
    }

    @GetMapping("/status")
    public String status() {
        return "tracesentry";
    }
}
