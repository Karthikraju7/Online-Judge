package com.karthikd.server.service;

import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserCode;
import com.karthikd.server.repository.UserCodeRepository;
import com.karthikd.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCodeServiceImpl implements UserCodeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCodeRepository codeRepository;

    @Override
    public UserCode saveOrUpdateCode(String email, String slug, String language, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserCode userCode = codeRepository
                .findByUserAndProblemSlugAndLanguage(user, slug, language)
                .orElse(new UserCode());

        userCode.setUser(user);
        userCode.setProblemSlug(slug);
        userCode.setLanguage(language);
        userCode.setCode(code);

        return codeRepository.save(userCode);
    }

    @Override
    public String getCodeForUser(String email, String slug, String language) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return codeRepository
                .findByUserAndProblemSlugAndLanguage(user, slug, language)
                .map(UserCode::getCode)
                .orElse(null);
    }
}
