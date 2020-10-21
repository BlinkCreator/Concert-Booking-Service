package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;
import se325.assignment01.concert.service.mapper.PerformerMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/concert-service/performers")
public class PerformerResource {

    /**
     * Fetches: List of all performers in database
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPerformers() {
        LOGGER.info("Retrieving all Performers");
        List<PerformerDTO> performerDTOList;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try{
            em.getTransaction().begin();
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p",Performer.class);
            List<Performer> performerList = performerQuery.getResultList();

            if (performerList == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            em.getTransaction().commit();
            performerDTOList = PerformerMapper.MakeDTOList(performerList); //convert list of performers to list of performerDTOs
        } finally {
            em.close();
        }
        return Response.ok(performerDTOList).build();
    }

    private static Logger LOGGER = LoggerFactory.getLogger(PerformerResource.class);

    /**
     * Fetches: specified performed with specified id
     * @param id
     * @return
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerformerById(@PathParam("id") long id){

        LOGGER.info("Fetching performer with id: " + id);
        PerformerDTO performerDTO;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            Performer performer = em.find(Performer.class, id);

            //if no such performer exists
            if (performer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            performerDTO = PerformerMapper.MakeDTO(performer);
        } finally {
            em.close();
        }
        return Response.ok(performerDTO).build();
    }
}