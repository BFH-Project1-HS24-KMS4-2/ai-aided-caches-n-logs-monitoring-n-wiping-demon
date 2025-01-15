package ch.bfh.tracesentry.cli.adapter;

import ch.bfh.tracesentry.lib.dto.*;
import ch.bfh.tracesentry.lib.model.SearchMode;
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

    public boolean kill() {
        try {
            restTemplate.getForObject(BASE_URL + "kill", String.class);
            return !this.checkStatus();
        } catch (Exception e) {
            return !this.checkStatus();
        }
    }

    public ResponseEntity<SearchResponseDTO> search(String canonicalPath, SearchMode mode, boolean noSubdirs) {
        StringBuilder urlBuilder = buildSearchBaseUrl(canonicalPath, mode, noSubdirs);
        return restTemplate.getForEntity(urlBuilder.toString(), SearchResponseDTO.class);
    }

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

    public ResponseEntity<Void> monitorAdd(String path, SearchMode mode, boolean noSubdirs) {
        var createMonitorPathDTO = new CreateMonitorPathDTO(path, mode, noSubdirs);
        return restTemplate.postForEntity(BASE_URL + "monitored-path", createMonitorPathDTO, Void.class);
    }

    public ResponseEntity<Void> monitorAdd(String path, SearchMode mode, boolean noSubdirs, Pattern pattern) {
        var createMonitorPathDTO = new CreateMonitorPathDTO(path, mode, noSubdirs, pattern.pattern());
        return restTemplate.postForEntity(BASE_URL + "monitored-path", createMonitorPathDTO, Void.class);
    }

    public ResponseEntity<List<MonitoredPathDTO>> monitorList() {
        ParameterizedTypeReference<List<MonitoredPathDTO>> responseType =
                new ParameterizedTypeReference<>() {
                };
        return restTemplate.exchange(BASE_URL + "monitored-path", HttpMethod.GET, null, responseType);
    }

    public ResponseEntity<Void> monitorRemove(Integer id) {
        return restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path/" + id, HttpMethod.DELETE, null, Void.class);
    }

    public ResponseEntity<MonitoredChangesDTO> getMonitoredChanges(int id, int start, int end) {
        return restTemplate.getForEntity(BASE_URL + "monitored-path/" + id + "/snapshots/changes?start=" + start + "&end=" + end, MonitoredChangesDTO.class);
    }

    public ResponseEntity<List<SnapshotDTO>> getSnapshotsOf(Integer monitoredPathId) {
        ParameterizedTypeReference<List<SnapshotDTO>> responseType = new ParameterizedTypeReference<>() {};
        return restTemplate.exchange(BASE_URL + "monitored-path/" + monitoredPathId + "/snapshots", HttpMethod.GET, null, responseType);
    }

    public String inspect(String canonicalPath) {
        return restTemplate.getForObject(BASE_URL + "inspect?path=" + canonicalPath, String.class);
    }

    public ResponseEntity<Void> wipe(String path, boolean remove) {
        var dto = new WipeFileDTO(path, remove);
        return restTemplate.postForEntity(BASE_URL + "wipe", dto, Void.class);
    }
}
