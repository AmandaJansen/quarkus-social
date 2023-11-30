package org.acme.sample.quarkussocial.rest;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.acme.sample.quarkussocial.domain.model.Follower;
import org.acme.sample.quarkussocial.domain.model.User;
import org.acme.sample.quarkussocial.domain.repository.FollowerRepository;
import org.acme.sample.quarkussocial.domain.repository.UserRepository;
import org.acme.sample.quarkussocial.rest.dto.FollowerRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;
    Long userId;
    Long followerId;


    @BeforeEach
    @Transactional
    void setUp() {

        var user = new User();
        user.setAge(35);
        user.setName("Carla");
        userRepository.persist(user);
        userId = user.getId();

//seguidor
        var follower = new User();
        follower.setAge(45);
        follower.setName("Mauricio");
        userRepository.persist(follower);
        followerId = follower.getId();

        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when followerId is equal to UserId")

    public void sameUserAsFollowerTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(409)
                .body(Matchers.is("You can't follow yourself"));

    }

    @Test
    @DisplayName("should return 404 on follow a user when UserId doesn't exist")

    public void userNotFoundWhenUserTryingFollowTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);
        var noneexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", noneexistentUserId)
                .when()
                .put()
                .then()
                .statusCode(404);


    }

    @Test
    @DisplayName("should follow an user")

    public void followerUserTest() {

        var body = new FollowerRequest();
        body.setFollowerId(followerId);


        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(204);

    }

    @Test
    @DisplayName("should return 404 on list user followers and userId doesn't exist")

    public void userNotFoundWhenListingFollowersTest() {
        var noneexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", noneexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(404);


    }

    @Test
    @DisplayName("should list a user's followers")

    public void listingFollowersTest() {
        var response =
                given()
                        .contentType(ContentType.JSON)
                        .pathParam("userId", userId)
                        .when()
                        .get()
                        .then()
                        .extract().response();
        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());


    }

    @Test
    @DisplayName("should return 404 on unfollow user followers and userId doesn't exist")

    public void userNotFoundWhenUnfollowingUserTest() {
        var noneexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", noneexistentUserId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("should unfollow an user")

    public void unfollowingUserTest() {
        var noneexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(204);


    }


}
