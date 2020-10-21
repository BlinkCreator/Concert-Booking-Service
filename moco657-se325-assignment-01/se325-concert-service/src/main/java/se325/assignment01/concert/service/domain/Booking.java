package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class to represent a Booking . A
 * booking is characterized by an ID (a database primary key value),
 * date and time, ConcertId, seat and User.
 */

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;
    private Long concertId;

    //Because we are using a collection with a many to many relationship it is standard to use lazy fetching.
    //When we load a seat we only want to load the seats that relate to the booking.
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Fetch(
            org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<Seat> seatList;

    //One user can have many bookings but only one booking can have one User.
    @ManyToOne
    private User user;

    public Booking(Long concertId, LocalDateTime date, List<Seat> seatList, User user) {
        this.date = date;
        this.concertId = concertId;
        this.seatList = seatList;
        this.user = user;
    }

    public Booking() {

    }
    //Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public void setSeats(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public void setUser(User user) {
        this.user = user;
    }

    //End of setters
    //Getters

    public Long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Long getConcertId() {
        return concertId;
    }

    public List<Seat> getSeats() {
        return seatList;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public User getUser() {
        return user;
    }

    //end of getters

}