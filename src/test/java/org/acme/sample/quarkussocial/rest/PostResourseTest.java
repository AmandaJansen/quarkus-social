package org.acme.sample.quarkussocial.rest;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.sample.quarkussocial.domain.model.Follower;
import org.acme.sample.quarkussocial.domain.model.Post;
import org.acme.sample.quarkussocial.domain.model.User;
import org.acme.sample.quarkussocial.domain.repository.FollowerRepository;
import org.acme.sample.quarkussocial.domain.repository.PostRepository;
import org.acme.sample.quarkussocial.domain.repository.UserRepository;
import org.acme.sample.quarkussocial.rest.dto.CreatePostRequest;
import org.acme.sample.quarkussocial.rest.dto.CreateUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResourse.class)
class PostResourseTest {
    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    @Inject
    PostRepository postRepository;
    Long userId;
    Long userNotFollowerId;

    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp() {

        //usuario padrão dos testes
        var user = new User();
        user.setAge(35);
        user.setName("Carla");
        userRepository.persist(user);
        userId = user.getId();

        //usuario que não segue ninguem
        var userNotFollower = new User();
        user.setAge(56);
        user.setName("Marta");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

//usuario seguidor
        var userFollower = new User();
        user.setAge(23);
        user.setName("Jorge");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);

//postagem do usuario
        Post post = new Post();
        post.setText("Hi");
        post.setUser(user);
        postRepository.persist(post);
    }

    @Test
    @DisplayName("should create a post for an user")
    public void createPostTest() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParams("userId", userId)
                .when()
                .post()
                .then()
                .statusCode(201);

    }

    @Test
    @DisplayName("should return 404 when trying to make a post for an nonexistent user")
    public void postForNonexistentUserTest() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var nonexistentUserId = 999;
        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParams("userId", nonexistentUserId)
                .when()
                .post()
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundPostTest() {

        var nonexistentUserId = 999;

        given()
                .pathParam("userId", nonexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when follower is not present")
    public void listPostFollowerHeaderNotSendTest() {

        given()
                .pathParams("userId", userId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("You forgot the header followerId"));

    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest() {

        var nonexistentFollowerId = 999;
        given()
                .pathParams("userId", userId)
                .header("followerId", nonexistentFollowerId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("This followerId doesn't exist "));

    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest() {

        given()
                .pathParams("userId", userId)
                .header("followerId", userNotFollowerId)
                .when()
                .get()
                .then()
                .statusCode(403)
                .body(Matchers.is("You can't see this post"));
    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest() {

        given()
                .pathParams("userId", userId)
                .header("followerId", userFollowerId)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }
}