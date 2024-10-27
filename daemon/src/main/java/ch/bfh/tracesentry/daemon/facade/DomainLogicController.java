package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.dto.SearchDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class DomainLogicController {

    @GetMapping("/search")
    public SearchDTO search() {
        return new SearchDTO(3,
                List.of(
                        "C:\\Users\\user\\Desktop\\test.txt",
                        "C:\\Users\\user\\Desktop\\test2.txt",
                        "C:\\Users\\user\\Desktop\\test3.txt",
                        "/etc/test.txt"
                )
        );
    }

    @PostMapping("/monitor")
    @ResponseStatus()
    public ResponseEntity<String> status() {
        return new ResponseEntity<>(null, null, 201);
    }
}
