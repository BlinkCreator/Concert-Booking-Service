package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class to represent a Concert. A Concert is characterised by an unique ID,
 * title, Image name, blurb, date and time, and Performers.
 */

@Entity
@Table(name = "CONCERTS")
public class Concert {

    @Id
    //The database will create a unique ID unless one is given.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID",nullable = false, unique = true)
    private Long id;

    @Column(name="TITLE")
    private String title;

    @Column(name="IMAGE_NAME")
    private String imageName;

    @Column(name="BLURB", length=1024)
    private String blurb;

    //Because we are using a collection with a many to many relationship it is standard to use lazy fetching.
    //When we load a date we only want to load the dates that relate to the concert not all the dates.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CONCERT_DATES")
    @Column(name="DATE")
    private Set<LocalDateTime> dates;

    //We use a many to many relationship because every concert can have many performers and every performer can have many concerts,
    @ManyToMany(cascade = CascadeType.PERSIST)
    @org.hibernate.annotations.Fetch(
            org.hibernate.annotations.FetchMode.SUBSELECT)
    //Creates join table so that only performers in this concert are inserted
    @JoinTable(name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name="CONCERT_ID", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID", referencedColumnName = "id"))
    @Column(name="PERFORMER")
    private Set<Performer> performers;

    public Concert(){
    }
    public Concert(Long id, String title, String imageName, String blurb,Set<Performer> performers){
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
        this.performers = performers;
    }

    //Setters

    public void setId(Long id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    //end of setters
    //Getters

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageName() {
        return imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public Set<LocalDateTime> getDates() {
        return this.dates;
    }

    public Set<Performer> getPerformers() {
        return this.performers;
    }

    //end of getters

    @Override
    public boolean equals(Object obj) {
        // Implement value-equality based on a Concert's title alone. ID isn't
        // included in the equality check because two Concert objects could
        // represent the same real-world Concert, where one is stored in the
        // database (and therefore has an ID - a primary key) and the other
        // doesn't (it exists only in memory).
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;

        return new EqualsBuilder().
                append(title, rhs.title).
                isEquals();
    }

    @Override
    public int hashCode() {
        // Hash-code value is derived from the value of the title field. It's
        // good practice for the hash code to be generated based on a value
        // that doesn't change.
        return new HashCodeBuilder(17, 31).
                append(title).hashCode();
    }
}