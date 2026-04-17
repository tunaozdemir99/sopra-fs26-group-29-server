package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class BucketItemPatchDTO {

    private String name;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getLocation() { 
        return location; 
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
