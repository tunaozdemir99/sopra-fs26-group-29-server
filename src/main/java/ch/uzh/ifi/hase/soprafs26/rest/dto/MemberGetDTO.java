package ch.uzh.ifi.hase.soprafs26.rest.dto;

/**
 * ClassName: MemberGetDTO
 * Package: ch.uzh.ifi.hase.soprafs26.rest.dto
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/5/20 16:57
 * @ version 1.0
 */
public class MemberGetDTO {
    private Long id;
    private String username;
    private boolean isAdmin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}
