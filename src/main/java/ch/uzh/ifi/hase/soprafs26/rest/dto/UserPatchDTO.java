package ch.uzh.ifi.hase.soprafs26.rest.dto;


public class UserPatchDTO {

    private String bio;
    private String profilePicture;
    private String password;

    public String getBio() { 
        return bio; 
    }

    public void setBio(String bio) { 
        this.bio = bio; 
    }

    public String getProfilePicture() { 
        return profilePicture; 
    }

    public void setProfilePicture(String profilePicture) { 
        this.profilePicture = profilePicture; 
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }
}
