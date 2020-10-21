package se325.assignment01.concert.service.domain;

import java.util.Set;
/**
 * Class to represent a Performer (an artist or band that plays at Concerts). A
 * Performer object has an ID (a database primary key value),
 * Name, Image, Genre and blurb
 */

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se325.assignment01.concert.common.types.Genre;

@Entity
//Data table name
@Table(name = "PERFORMERS")
public class Performer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;
    @Column(name = "IMAGE_NAME")
    private String imageName;

    //Tells the compiler that this is an enum number
    @Enumerated(EnumType.STRING)
    @Column(name = "GENRE")
    private Genre genre;

    @Column(name = "BLURB", length = 1024)
    private String blurb;

    /*Cascade Persists maintains persistance across the database.
    So that duplicate concerts are not created and if a concerte
    doesn't exist it will be added.
    Many to many uses lazy fetchtype
     */
    //Because we are using a collection with a many to many relationship it is standard to use lazy fetching.
    //When we load a Concert we only want to load the concert that relate to the performer not all the concerts.
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Concert> concerts;

    //Creates setters for all fields
    public void setId(Long id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setImageName(String imageName){
        this.imageName = imageName;
    }

    public void setGenre(Genre genre){
        this.genre = genre;
    }

    public void setBlurb(String blurb){
        this.blurb = blurb;
    }

    public void setConcerts(Set<Concert> concerts){
        this.concerts = concerts;
    }
    //end of setters
    //start of getters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }
    //end of getters

    //Overides toString to return customized fields
    @Override
    public String toString(){

        String string = "Performer, id: " + id +
                ", name: " + name +
                ", s3 image: " + imageName +
                ", genre: " + genre.toString();
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;


        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;

        return new EqualsBuilder().
                append(name, rhs.name).
                isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).hashCode();
    }

}