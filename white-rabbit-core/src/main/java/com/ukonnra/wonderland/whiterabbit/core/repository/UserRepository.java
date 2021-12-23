package com.ukonnra.wonderland.whiterabbit.core.repository;

import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UserRepository
    extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User> {}
