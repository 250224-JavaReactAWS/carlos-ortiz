package com.caom.repos.user;

import com.caom.models.User;
import com.caom.repos.GeneralDAO;

public interface UserDAO extends GeneralDAO<User> {

    User getUserByUsername(String username);

}
