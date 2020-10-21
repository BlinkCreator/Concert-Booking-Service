package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.UserDTO;
import se325.assignment01.concert.service.config.Config;
import se325.assignment01.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/concert-service")

public class LoginResource {

    private static Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    /**
     * Verify: User credentials
     * Assign: New auth token to user and persists it to DB if verified
     * @param userDTO
     * @return
     */
    @POST
    @Path("/login")
    public Response login(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Response response;

        try {
            LOGGER.info("Attempting Login");
            em.getTransaction().begin();

            TypedQuery<User> userQuery = em.createQuery("select _user from User _user where _user.username = :inputUserName AND _user.password = :inputPassword", User.class)
                    .setParameter("inputUserName", username)
                    .setParameter("inputPassword", password)
                    .setLockMode(LockModeType.OPTIMISTIC);

            User _user = userQuery.getResultList().stream().findFirst().orElse(null);

            //Login: Failed
            if (_user == null) {
                response = Response.status(Response.Status.UNAUTHORIZED).build();
            }

            else {

                NewCookie cookie = new NewCookie(Config.AUTH_COOKIE, UUID.randomUUID().toString());
                _user.setCookie(cookie.getValue());
                response = Response.ok().cookie(cookie).build();
                em.merge(_user);
                em.getTransaction().commit();
            }

        } finally {
            em.close();
        }

        return response;
    }
}
