package cricket.merstham.website.frontend.service;

import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import cricket.merstham.website.frontend.configuration.GraphConfiguration;
import okio.ByteString;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.Principal;

import static javax.ws.rs.core.Response.Status.OK;

@Service
public class GraphService {
    private static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

    private final GraphConfiguration graphConfiguration;
    private final AccessTokenManager accessTokenManager;

    @Autowired
    public GraphService(
            GraphConfiguration graphConfiguration, AccessTokenManager accessTokenManager) {
        this.graphConfiguration = graphConfiguration;
        this.accessTokenManager = accessTokenManager;
    }

    public <T extends Query, R extends Operation.Data> Response<R> executeQuery(T query, Principal principal) throws IOException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal)token.getPrincipal();

        LOG.info("Sending `{}` GraphQL API request with user token", query.name().name());
        return getResult(query, keycloakPrincipal.getKeycloakSecurityContext().getTokenString());
    }

    public <T extends Query, R extends Operation.Data> Response<R> executeQuery(T query) throws IOException {
        String accessToken = accessTokenManager.getAccessToken();
        LOG.info("Sending `{}` GraphQL API request with client credentials token", query.name().name());
        return getResult(query, accessToken);
    }

    private <T extends Query, R extends Operation.Data> Response<R> getResult(T query, String accessToken) throws IOException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget
                = client.target(graphConfiguration.getGraphUri());

        Invocation invocation =
                webTarget
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .buildPost(Entity.json(query.composeRequestBody().utf8()));
        javax.ws.rs.core.Response response = invocation.invoke();
        LOG.info("Received `{}` GraphQL API response: {}", query.name().name(), response.getStatus());
        if (response.getStatus() == OK.getStatusCode()) {
            String body = response.readEntity(String.class);
            LOG.debug("Response Body = {}", body);

            return query.parse(ByteString.of(body.getBytes()));
        }
        throw new RuntimeException("Failed to get GraphQL response from service");
    }
}