package plugin.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class VPNApiResponse {
    var anon: Boolean = false
    var status: String? = null
    var ip: String? = null
    var isp: String? = null
    var message: String? = null
}