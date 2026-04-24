package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class VotePostDTO {

    private int value; // +1, -1, or 0 (retract)

    public int getValue() { 
        return value; 
    }

    public void setValue(int value) { 
        this.value = value; 
    }
}
