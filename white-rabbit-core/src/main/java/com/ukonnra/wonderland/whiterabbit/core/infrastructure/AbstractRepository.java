package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractRepository<
        T extends AbstractEntity<P>, P extends AbstractPresentationModel>
    extends JpaRepository<T, UUID>, QuerydslPredicateExecutor<T> {}
