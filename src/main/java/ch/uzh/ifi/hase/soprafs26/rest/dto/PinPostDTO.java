package ch.uzh.ifi.hase.soprafs26.rest.dto;

/**
 * ClassName: PinPostDTO
 * Package: ch.uzh.ifi.hase.soprafs26.rest.dto
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/4/21 22:25
 * @ version 1.0
 */
public class PinPostDTO {

    private String name;
    private Double latitude;
    private Double longitude;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
