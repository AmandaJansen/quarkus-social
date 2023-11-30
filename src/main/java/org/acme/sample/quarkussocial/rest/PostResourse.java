package org.acme.sample.quarkussocial.rest;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.sample.quarkussocial.domain.model.Post;
import org.acme.sample.quarkussocial.domain.model.User;
import org.acme.sample.quarkussocial.domain.repository.FollowerRepository;
import org.acme.sample.quarkussocial.domain.repository.PostRepository;
import org.acme.sample.quarkussocial.domain.repository.UserRepository;
import org.acme.sample.quarkussocial.rest.dto.CreatePostRequest;
import org.acme.sample.quarkussocial.rest.dto.PostResponse;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class PostResourse {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResourse(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(
            @PathParam("userId") Long userId, CreatePostRequest request) {

        User user = userRepository.findById(userId);

        if (user == null) {

            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);


        postRepository.persist(post);
        return Response.status(Response.Status.CREATED).build();

    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId,
                              @HeaderParam("followerId") Long followerId) {
        User user = userRepository.findById(userId);

        if (user == null) {

            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (followerId == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("You forgot the header followerId")
                    .build();
        }
        User follower = userRepository.findById(followerId);

        if (follower == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("This followerId doesn't exist ")
                    .build();
        }
        boolean follows = followerRepository.follows(follower, user);

        if (!follows) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You can't see this post").build();
        }

        var query = postRepository.find(
                "user", Sort.by("dateTime", Sort.Direction.Descending), user); //procura a postagem do usuario
        var list = query.list();

        var postResponseList = list.stream()
                .map(post -> PostResponse.fromEntity(post))
                .collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }
}
