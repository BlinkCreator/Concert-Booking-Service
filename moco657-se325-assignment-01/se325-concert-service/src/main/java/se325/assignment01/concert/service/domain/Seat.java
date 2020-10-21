package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Class to represent a Seat. A Seat is characterised by an unique ID,
 * Version, Label, T/F if it's been booked , date and time, and price.
 */
@Entity
public class Seat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//Allows concurancy in updates and throws the OptimisticLockException if the version is different
	//than the one the client specifies.
	@Version
	private int version;
	private String label;
	private boolean isBooked;
	private LocalDateTime date;
	private BigDecimal price;

	public Seat() {}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}
	//Setters
	public void setId(Long id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	//End of setter
	//Getters
	public Long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public BigDecimal getPrice() {
		return price;
	}
	//End of getters
}