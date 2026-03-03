package plugin.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    public boolean anon;
    public String status;
    public String ip;
    public String isp;
}
