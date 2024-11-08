package ch.bfh.tracesentry.cli.adapter;

import ch.bfh.tracesentry.lib.dto.MonitorPathDTO;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Paths;
import java.util.List;

@Service
public class DaemonAdapter {

    private final RestTemplate restTemplate;

    public final static int PORT = 8087;
    public final static String BASE_URL = "http://localhost:" + PORT + "/";

    @Autowired
    public DaemonAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * @return true if the daemon is running, false otherwise
     */
    public boolean checkStatus() {
        try {
            var resp = restTemplate.getForObject(BASE_URL + "status", String.class);
            if (resp == null) {
                return false;
            }
            return resp.equals("tracesentry");
        } catch (RestClientException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return true if the daemon was killed or was not running, false otherwise
     */
    public boolean kill() {
        try {
            restTemplate.getForObject(BASE_URL + "kill", String.class);
            return !this.checkStatus();
        } catch (Exception e) {
            return !this.checkStatus();
        }
    }

    /**
     * @param path relative or absolute path to the directory to search
     * @return SearchResponse object
     */
    public ResponseEntity<SearchResponseDTO> search(String path) {
        var absolutePath = Paths.get(path).toAbsolutePath().toString();
        return restTemplate.getForEntity(BASE_URL + "search?path=" + absolutePath, SearchResponseDTO.class);
    }

    /**
     * @param path relative or absolute path to the directory to monitor
     * @return void
     */
    public ResponseEntity<Void> monitorAdd(String path) {
        var absolutePath = Paths.get(path).toAbsolutePath().toString();
        return restTemplate.postForEntity(BASE_URL + "monitor", absolutePath, Void.class);
    }

    /**
     * @return List of MonitorPathDTO objects
     */
    public ResponseEntity<List<MonitorPathDTO>> monitorList() {
        ParameterizedTypeReference<List<MonitorPathDTO>> responseType =
                new ParameterizedTypeReference<List<MonitorPathDTO>>() {};
        return restTemplate.exchange(BASE_URL + "monitor", HttpMethod.GET, null, responseType);
    }
}