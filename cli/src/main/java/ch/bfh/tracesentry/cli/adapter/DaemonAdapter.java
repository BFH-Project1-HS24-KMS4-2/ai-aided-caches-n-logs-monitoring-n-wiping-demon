package ch.bfh.tracesentry.cli.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
}