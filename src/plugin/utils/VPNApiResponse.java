package plugin.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VPNApiResponse {
    public boolean anon;
    public String status;
    public String ip;
    public String isp;
    public String message;
}
