package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.common.types.BookingStatus;
import se325.assignment01.concert.service.domain.Seat;
import se325.assignment01.concert.service.jaxrs.LocalDateTimeParam;
import se325.assignment01.concert.service.mapper.SeatMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Path("/concert-service/seats/")

public class SeatResource {

    /**
     * Fetches: All seats for specified concert
     * Option: Filter by seat status
     * @param _dateTimeParam
     * @param _bookingStatus
     * @return
     */
    @GET
    @Path("{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeats(@PathParam("date") LocalDateTimeParam _dateTimeParam, @QueryParam("status") BookingStatus _bookingStatus) {

        LocalDateTime date = _dateTimeParam.getLocalDateTime();
        GenericEntity<List<SeatDTO>> entity;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery; //query depends on booking status

            if (_bookingStatus == BookingStatus.Any) {
                seatQuery = em.createQuery("select _seat from Seat _seat Where _seat.date = :date", Seat.class)
                        .setParameter("date", date);
            } else {

                boolean isBooked = (_bookingStatus == BookingStatus.Booked);
                seatQuery = em.createQuery("select _seat from Seat _seat Where _seat.date=:date and _seat.isBooked = :isBooked", Seat.class)
                        .setParameter("isBooked", isBooked)
                        .setParameter("date", date);
            }

            List<Seat> _bookedSeatList = seatQuery.getResultList();
            List<SeatDTO> _bookedDTOSeatList = SeatMapper.MakeDTOList(_bookedSeatList);
            entity = new GenericEntity<List<SeatDTO>>(_bookedDTOSeatList) {};

        } finally {
            em.close();
        }
        return Response.ok(entity).build();
    }
}
