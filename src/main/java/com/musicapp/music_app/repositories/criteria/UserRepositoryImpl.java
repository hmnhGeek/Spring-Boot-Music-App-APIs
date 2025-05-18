package com.musicapp.music_app.repositories.criteria;

import com.musicapp.music_app.constants.UserRoles;
import com.musicapp.music_app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

public class UserRepositoryImpl {
    @Autowired
    private MongoTemplate mongoTemplate;

    private boolean isAdmin(String userName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userName").is(userName));
        List<User> users = mongoTemplate.find(query, User.class);
        if (users.size() > 0) {
            User user = users.get(0);
            return user.getRoles().contains(UserRoles.ADMIN.toString());
        }
        return false;
    }

    public List<User> getUsersHavingPlaylists(String adminUserName) {
        boolean userIsAdmin = isAdmin(adminUserName);
        if (!userIsAdmin) {
            return null;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("playlists").ne(Collections.emptyList()));
        return mongoTemplate.find(query, User.class);
    }
}
