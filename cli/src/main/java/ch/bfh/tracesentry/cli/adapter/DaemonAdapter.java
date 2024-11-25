package ch.bfh.tracesentry.cli.adapter;

import ch.bfh.tracesentry.lib.model.SearchMode;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

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
     * @param canonicalPath canonical path to the directory to search
     * @param mode        search mode
     * @param noSubdirs   do not search in subdirectories
     * @return SearchResponse object
     */
    public ResponseEntity<SearchResponseDTO> search(String canonicalPath, SearchMode mode, boolean noSubdirs) {
        StringBuilder urlBuilder = buildSearchBaseUrl(canonicalPath, mode, noSubdirs);
        return restTemplate.getForEntity(urlBuilder.toString(), SearchResponseDTO.class);
    }

    /**
     * @param canonicalPath absolute path to the directory to search
     * @param mode         search mode
     * @param noSubdirs    do not search in subdirectories
     * @param pattern      pattern to search for, may be null
     * @return SearchResponse object
     */
    public ResponseEntity<SearchResponseDTO> search(String canonicalPath, SearchMode mode, boolean noSubdirs, Pattern pattern) {
        StringBuilder urlBuilder = buildSearchBaseUrl(canonicalPath, mode, noSubdirs);
        urlBuilder.append("&pattern=");
        urlBuilder.append(URLEncoder.encode(pattern.pattern(), StandardCharsets.UTF_8));
        String url = urlBuilder.toString();
        return restTemplate.getForEntity(url, SearchResponseDTO.class);

    }

    private StringBuilder buildSearchBaseUrl(String canonicalPath, SearchMode mode, boolean noSubdirs) {
        var sb = new StringBuilder(BASE_URL)
                .append("search?path=")
                .append(canonicalPath)
                .append("&mode=")
                .append(mode.toString().toLowerCase());

        if (noSubdirs) {
            sb.append("&no-subdirs=true");
        }
        return sb;
    }

    /**
     * @param path relative or absolute path to the directory to monitor
     * @return void
     */
    public ResponseEntity<Void> monitorAdd(String path) {
        return restTemplate.postForEntity(BASE_URL + "monitored-path", path, Void.class);
    }

    /**
     * @return List of MonitoredPathDTO objects
     */
    public ResponseEntity<List<MonitoredPathDTO>> monitorList() {
        ParameterizedTypeReference<List<MonitoredPathDTO>> responseType =
                new ParameterizedTypeReference<>() {
                };
        return restTemplate.exchange(BASE_URL + "monitored-path", HttpMethod.GET, null, responseType);
    }

    /**
     * @param id id of the monitored path to remove
     * @return void
     */
    public ResponseEntity<Void> monitorRemove(Integer id) {
        return restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path/" + id, HttpMethod.DELETE, null, Void.class);
    }

    /**
     * @param id of the monitored path to get changes from
     * @return MonitoredChangesDTO
     */
    public ResponseEntity<MonitoredChangesDTO> getMonitoredChanges(Integer id) {
        return restTemplate.getForEntity(BASE_URL + "monitored-path/" + id + "/changes", MonitoredChangesDTO.class);
    }
}
