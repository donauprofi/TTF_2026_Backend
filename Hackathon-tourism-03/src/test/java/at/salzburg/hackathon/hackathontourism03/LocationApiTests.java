package at.salzburg.hackathon.hackathontourism03;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.data-dir=src/test/resources/testdata")
@AutoConfigureMockMvc
class LocationApiTests {
    private static final String CAFE = "/api/locations/osm:node_1";

    @Autowired
    private MockMvc mvc;

    @Test
    void placesAreShapedForTheFrontend() throws Exception {
        mvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("osm:node_1"))
                .andExpect(jsonPath("$[0].address").value("Seestraße 4, Gmunden"))
                .andExpect(jsonPath("$[0].wheelchairAccess").value("limited"))
                .andExpect(jsonPath("$[0].facts[0].conflict").value(true))
                .andExpect(jsonPath("$[0].obstacles[0]").value("wheelchair-limited"));
        mvc.perform(get("/api/locations").param("q", "GROSSE")).andExpect(jsonPath("$[0].name").value("Café Größe"));
    }

    @Test
    void contradictoryReviewIsRejected() throws Exception {
        mvc.perform(post(CAFE + "/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"amenities\":[\"ramp\"],\"obstacles\":[\"steps\"]}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Please check the conflicting amenities and obstacles."));
    }

    @Test
    void unknownPlaceUsesTheErrorContract() throws Exception {
        mvc.perform(get("/api/locations/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("This place could not be found."));
    }

    @Test
    void reviewUpdatesFactsScoreAndPoints() throws Exception {
        String body = "{\"rating\":4,\"amenities\":[\"wheelchair\"],\"obstacles\":[],\"text\":\"Easy.\"}";
        mvc.perform(post("/api/locations/osm:node_1/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsAwarded").value(10))
                .andExpect(jsonPath("$.location.score").value(4.0))
                .andExpect(jsonPath("$.location.wheelchairAccess").value("yes"))
                .andExpect(jsonPath("$.location.amenities[0]").value("wheelchair"));
    }
}
