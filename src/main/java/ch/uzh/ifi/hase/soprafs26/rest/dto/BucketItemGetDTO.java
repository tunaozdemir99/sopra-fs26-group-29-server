package ch.uzh.ifi.hase.soprafs26.rest.dto;


public class BucketItemGetDTO {

	private Long bucketItemId;
	private String name;
    private String description;
    private String location;
    private UserGetDTO addedBy;
    private int voteScore;
    private int myVote;
    private Double latitude;
    private Double longitude;
    private boolean isScheduled;

	public Long getBucketItemId() {
		return bucketItemId;
	}

	public void setBucketItemId(Long bucketItemId) {
		this.bucketItemId = bucketItemId;
	}

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

    public UserGetDTO getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(UserGetDTO addedBy) {
		this.addedBy = addedBy;
	}

    public int getVoteScore() {
		return voteScore;
	}

	public void setVoteScore(int voteScore) {
		this.voteScore = voteScore;
	}

    public int getMyVote() {
		return myVote;
	}

	public void setMyVote(int myVote) {
		this.myVote = myVote;
	}

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean isScheduled() { return isScheduled; }
    public void setScheduled(boolean isScheduled) { this.isScheduled = isScheduled; }
}
