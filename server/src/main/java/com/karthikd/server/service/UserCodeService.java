package com.karthikd.server.service;

import com.karthikd.server.entity.UserCode;

public interface UserCodeService {
    UserCode saveOrUpdateCode(String email, String slug, String language, String code);
    String getCodeForUser(String email, String slug, String language);
}
