package ch.bfh.tracesentry.daemon.facade;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DomainLogicController {

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @PostMapping("/monitor")
    @ResponseStatus()
    public ResponseEntity<String> status() {
        return new ResponseEntity<>(null, null, 201);
    }
}
