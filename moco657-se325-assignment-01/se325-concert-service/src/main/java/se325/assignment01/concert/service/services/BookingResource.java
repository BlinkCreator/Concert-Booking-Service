package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.common.dto.BookingRequestDTO;
import se325.assignment01.concert.common.dto.ConcertInfoNotificationDTO;
import se325.assignment01.concert.common.dto.ConcertInfoSubscriptionDTO;
import se325.assignment01.concert.service.config.Config;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Seat;
import se325.assignment01.concert.service.domain.User;
import se325.assignment01.concert.service.mapper.BookingMapper;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Path("/concert-service")
public class BookingResource {

    private static Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);
    private ExecutorService executorService = Executors.newCachedThreadPool();

    // Initialize map: so the collection won't be overwritten
    // each time a resource is created
    private static final Map<Long, List<Subscription>> subscribersMap = new ConcurrentHashMap<>(); //ConcertId to List of Subscriptions

    /*
     * Return: a User from the database whose cookie matches the cookie in the argument.
     * If no such user can be found, null is returned.
     */
    private User getAuthenticatedUser(EntityManager em, Cookie _cookie) {
        TypedQuery<User> userQuery = em.createQuery("select _user from User _user where _user.cookie = :cookie", User.class)
                .setParameter("cookie", _cookie.getValue());
        User _user = userQuery.getResultList().stream().findFirst().orElse(null);

        return _user;
    }

    private NewCookie appendCookie(Cookie clientCookie) {
        return new NewCookie(Config.AUTH_COOKIE, clientCookie.getValue());
    }

    /**
     * Fetch: All bookings associated with specified user.
     * @param cookie
     * @return
     */
    @GET
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBookingsForUser(@CookieParam(Config.AUTH_COOKIE) Cookie cookie) {
        if (cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        LOGGER.info("Fetching all bookings");

        EntityManager em = PersistenceManager.instance().createEntityManager();
        GenericEntity<List<BookingDTO>> entity;

        //searching for: An associated user of this auth token
        try {
            em.getTransaction().begin();

            //Authenticate user
            User user = this.getAuthenticatedUser(em, cookie);

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //get all bookings of specified user
            TypedQuery<Booking> bookingQuery = em.createQuery("select _booking from Booking _booking where _booking.user = :user", Booking.class)
                    .setParameter("user", user);
            List<Booking> bookingList = bookingQuery.getResultList();

            //convert to List of BookingDTOs
            List<BookingDTO> bookingDTOList = BookingMapper.MakeDTOList(bookingList);
            entity = new GenericEntity<List<BookingDTO>>(bookingDTOList) {
            };
        } finally {
            em.close();
        }
        return Response.ok(entity).cookie(appendCookie(cookie)).build();
    }


    /**
     * Make: New booking for user in the system given a valid concert and if all seats are available.
     * @param _bookingRequestDTO
     * @param _cookie
     * @return
     */
    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response tryMakeBooking(BookingRequestDTO _bookingRequestDTO, @CookieParam(Config.AUTH_COOKIE) Cookie _cookie) {

        if (_cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        LOGGER.info("Making booking for concert: " + _bookingRequestDTO.getConcertId());

        Booking _booking;
        //int variables used as inputs for notifySubscribers()
        int _availableSeats;
        int _totalSeats;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            User _user = this.getAuthenticatedUser(em, _cookie);

            if (_user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Concert _concert = em.find(Concert.class, _bookingRequestDTO.getConcertId());

            //check: Concert with specified concert id exists
            //or check: Date within concert exists
            if (_concert == null || !_concert.getDates().contains(_bookingRequestDTO.getDate())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            //try: Persist booking
            _booking = this.makeBooking(_bookingRequestDTO, _user);

            //failed: Booking, no available seats
            if (_booking == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            /**
             * Fetch: Number of seats for specified concert
             *    Calculate: Availability
             *    Notify: subscribers
             */
            TypedQuery<Seat> availableSeatsQuery = em.createQuery("select _seat from Seat _seat where _seat.date = :requestDate and _seat.isBooked = false ", Seat.class)
                    .setParameter("requestDate", _bookingRequestDTO.getDate());
            _availableSeats = availableSeatsQuery.getResultList().size();

            TypedQuery<Seat> allSeatsQuery = em.createQuery("select _seat from Seat _seat where _seat.date = :requestDate", Seat.class)
                    .setParameter("requestDate", _bookingRequestDTO.getDate());
            _totalSeats = allSeatsQuery.getResultList().size();

        } finally {
            em.close();
        }

        this.SubscriptionNotification(_availableSeats, _totalSeats, _bookingRequestDTO.getConcertId(), _bookingRequestDTO.getDate());
        return Response.created(URI.create("concert-service/bookings/" + _booking.getId())).cookie(appendCookie(_cookie)).build();
    }

    /*
    Persists: Modification of relevant seats to database.
    Skip: User authentication query called within tryMakeBooking()
     */
    private Booking makeBooking(BookingRequestDTO _bookingRequestDTO, User _user) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Booking _booking;

        try {
            em.getTransaction().begin();

            TypedQuery<Seat> seatQuery = em.createQuery("select _seat from Seat _seat where _seat.date = :requestDate and _seat.isBooked = false and _seat.label in :seats", Seat.class)
                    .setParameter("seats", _bookingRequestDTO.getSeatLabels())
                    .setParameter("requestDate", _bookingRequestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC);
            List<Seat> _seatList = seatQuery.getResultList();

            //check if all seats are available
            if (_seatList.size() != _bookingRequestDTO.getSeatLabels().size()) {
                return null;
            }

            //set all seats too booked
            for (Seat _seat : _seatList) {
                _seat.setBooked(true);
            }
            _booking = new Booking(_bookingRequestDTO.getConcertId(), _bookingRequestDTO.getDate(), _seatList, _user);
            em.persist(_booking);

            em.getTransaction().commit();

        } catch (OptimisticLockException e) {
            em.close();
            _booking = this.makeBooking(_bookingRequestDTO, _user); //retry, skip authentication of user
        } finally {
            em.close();
        }
        return _booking;
    }

    /**
     * Fetches: Booking by id, only returns the booking if possessed by specified user
     * @param _id
     * @param _cookie
     * @return
     */

    @GET
    @Path("/bookings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsById(@PathParam("id") long _id, @CookieParam(Config.AUTH_COOKIE) Cookie _cookie) {
        if (_cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        LOGGER.info("Fetching booking with id: " + _id);
        BookingDTO _bookingDTO;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            User _user = this.getAuthenticatedUser(em, _cookie);

            if (_user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Booking _booking = em.find(Booking.class, _id);
            if (_booking == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //check: Forbidden request, user does not possess booking
            if (_booking.getUser().getId() != _user.getId()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            _bookingDTO = BookingMapper.MakeDTO(_booking);
        } finally {
            em.close();
        }

        return Response.ok(_bookingDTO).cookie(appendCookie(_cookie)).build();
    }

    /**
     Subscribe: user to specified concert on specific date
     Notifies: User when the number of available seats falls below specified amount.
     Note:All active subscriptions are temporarily stored in map until Notification is sent.
     so response is asynchronous
     * @param response
     * @param _cookie
     * @param _concertInfoSubscriptionDTO
     */

    @POST
    @Path("/subscribe/concertInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void SubscribeUser(@Suspended AsyncResponse response, @CookieParam(Config.AUTH_COOKIE) Cookie _cookie, ConcertInfoSubscriptionDTO _concertInfoSubscriptionDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        if (_cookie == null) {
            LOGGER.info("Unauthorized to subscribe");
            executorService.submit(() -> {
                response.resume(Response.status(Response.Status.UNAUTHORIZED).build());
            });
            return;
        }
        try {
            em.getTransaction().begin();

            User user = this.getAuthenticatedUser(em, _cookie);

            if (user == null) {
                executorService.submit(() -> {
                    response.resume(Response.status(Response.Status.UNAUTHORIZED).build());
                });
                return;
            }

            long id = _concertInfoSubscriptionDTO.getConcertId();
            Concert concert = em.find(Concert.class, id);

            //Check: concert exists
            //or Check: date exits
            if (concert == null || !concert.getDates().contains(_concertInfoSubscriptionDTO.getDate())) {
                executorService.submit(() -> {
                    response.resume(Response.status(Response.Status.BAD_REQUEST).build());
                });
                return;
            }

            //Subscribe: User
            List<Subscription> _subscribersList = subscribersMap.getOrDefault(concert.getId(), new ArrayList<>());
            _subscribersList.add(new Subscription(_concertInfoSubscriptionDTO, response));
            subscribersMap.put(concert.getId(), _subscribersList);

        } finally {
            em.close();
        }
    }

    /**
     * Hook method:
     *  Executes: every time a new booking is created.
     *  Check: all subscribers for a specified concert.
     *  send: Response if the available seats for concert falls below given amount.
     * @param _availableSeats
     * @param _totalSeats
     * @param _concertId
     * @param _localDateTime
     */

    public void SubscriptionNotification(int _availableSeats, int _totalSeats, long _concertId, LocalDateTime _localDateTime) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            List<Subscription> _subscriberList = subscribersMap.get(_concertId);

            if (_subscriberList == null) {
                return;
            }

            List<Subscription> _updateSubscriptions = new ArrayList<>();

            for (Subscription _subscriber : _subscriberList) {
                ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO = _subscriber.getConcertInfoSubscriptionDTO();

                //Ensures: updates only sent to subscribed users of specified concert
                if (concertInfoSubscriptionDTO.getDate().isEqual(_localDateTime)) {
                    //Checks: Number of avaliable seats is below specified amount
                    if (concertInfoSubscriptionDTO.getPercentageBooked() < 100 - _availableSeats * 100 / _totalSeats) {
                        AsyncResponse response = _subscriber.getResponse();

                        synchronized (response) {
                            ConcertInfoNotificationDTO notification = new ConcertInfoNotificationDTO(_availableSeats);
                            response.resume(Response.ok(notification).build());
                        }
                    } else {
                        _updateSubscriptions.add(_subscriber);
                    }
                } else {
                    _updateSubscriptions.add(_subscriber);
                }
            }
            subscribersMap.put(_concertId, _updateSubscriptions);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

/**
 * Class to represent a Subscription . A
 *  subscription is characterized by an AsyncResponse object
 *  AsyncResponse Object:Used to notify Users when requirements are met
 *  specified by the ConcertInfoSubscriptionDTO object.
 */
class Subscription {

        private ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO;
        private AsyncResponse response;

        public Subscription(ConcertInfoSubscriptionDTO _concertInfoSubscriptionDTO, AsyncResponse response) {
            this.concertInfoSubscriptionDTO = _concertInfoSubscriptionDTO;
            this.response = response;
        }

        public AsyncResponse getResponse() {
            return this.response;
        }

        public ConcertInfoSubscriptionDTO getConcertInfoSubscriptionDTO() {
            return this.concertInfoSubscriptionDTO;
        }

    }
