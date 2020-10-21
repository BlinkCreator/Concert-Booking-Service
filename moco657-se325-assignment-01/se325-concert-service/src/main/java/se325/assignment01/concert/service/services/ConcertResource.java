package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.mapper.ConcertMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/concert-service/concerts")
public class ConcertResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    /**
     * Fetch: List of all Performers in database
     * @return
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConcerts() {

        GenericEntity<List<ConcertDTO>> entity;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select _concert from Concert _concert", Concert.class);

            List<Concert> concertList = concertQuery.getResultList();
            List<ConcertDTO> concertDTOList = ConcertMapper.MakeDTOList(concertList); //Convert list of concerts to list of ConcertDTOs
            entity = new GenericEntity<List<ConcertDTO>>(concertDTOList) {};
        } finally {
            em.close();
        }
        return Response.ok(entity).build();
    }

    /**
     * Fetching: Single concert with specified id
     * @param id
     * @return
     */

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertById(@PathParam("id") long id) {
        LOGGER.info("Fetching concert with id: " + id);

        //Declare: Empty concert value
        //Note:so concert value can be returned outside try statement

        Concert concert;

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            concert = em.find(Concert.class, id);

            //Response: NOT_FOUND if Concert is non-existant
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } finally {
            em.close();
        }
        return Response.ok(ConcertMapper.MakeDTO(concert)).build();
    }

    /**
     * Fetches: list of summaries for all concerts in database
     * @return
     */
    @GET
    @Path("/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConcertSummaries() {

        GenericEntity<List<ConcertSummaryDTO>> entity;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select _concert from Concert _concert", Concert.class);
            List<Concert> concertList = concertQuery.getResultList();
            List<ConcertSummaryDTO> concertSummaryDTOList = ConcertMapper.MakeDTOSummaryList(concertList);
            entity = new GenericEntity<List<ConcertSummaryDTO>>(concertSummaryDTOList) {};

        } finally {
            em.close();
        }

        return Response.ok(entity).build();
    }

}