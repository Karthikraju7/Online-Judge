package com.karthikd.server.service;

import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;

public interface UserService {

    User registerUser(UserModel userModel);

    User loginAndFetchUser(String email, String password);

    User loginAndFetchAdmin(String email, String password);

    User verifyUser(String token);

    void changePassword(String email, String currentPassword, String newPassword);

    void initiatePasswordReset(String email);

    void resetPassword(String token, String newPassword);

}
