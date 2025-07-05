package com.karthikd.server.service;

import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;

public interface UserService {
    User registerUser(UserModel userModel);

    User loginAndFetchUser(String email, String password);
}
