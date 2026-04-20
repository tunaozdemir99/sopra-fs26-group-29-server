package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "votes")
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bucket_item_id", nullable = false)
    private BucketItem bucketItem;

    @Column(nullable = false)
    private int value; // +1 or -1

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public User getUser() { 
        return user; 
    }

    public void setUser(User user) { 
        this.user = user; 
    }

    public BucketItem getBucketItem() { 
        return bucketItem; 
    }

    public void setBucketItem(BucketItem bucketItem) { 
        this.bucketItem = bucketItem; 
    }

    public int getValue() { 
        return value; 
    }

    public void setValue(int value) { 
        this.value = value; 
    }
}
