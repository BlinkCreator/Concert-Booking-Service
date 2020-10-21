package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * Class to represent a User . A
 * User is characterized by an ID (a database primary key value),
 * Username, Password, Version and Cookie.
 */

@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="USERNAME")
    private String username;

    @Column(name="PASSWORD")
    private String password;

    @Column(name="VERSION")
    @Version
    private Long version;

    //Sets up cookie's

    @Column(name="COOKIE")
    private String cookie;

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getCookie() {
        return cookie;
    }

    //Hibernate needs an empty instructor
    public User(){

    }

    public User(Long id, String username, String password, Long version){
        this.id = id;
        this.username = username;
        this.password = password;
        this.version = version;
    }

    //Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    //End of setters
    //Getters

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Long getVersion() {
        return version;
    }

    //end of getters

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
                append(username, rhs.username).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(username).hashCode();
    }
}