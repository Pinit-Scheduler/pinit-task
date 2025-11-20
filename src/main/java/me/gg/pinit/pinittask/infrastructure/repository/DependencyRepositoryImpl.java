package me.gg.pinit.pinittask.infrastructure.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepositoryCustom;
import me.gg.pinit.pinittask.domain.dependency.repository.FromToPair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.QDependency.dependency;

@Repository
public class DependencyRepositoryImpl implements DependencyRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public DependencyRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    @Transactional
    public int deleteByFromToPairs(List<FromToPair> pairs) {
        BooleanBuilder builder = new BooleanBuilder();
        pairs.forEach(pair -> {
            builder.or(dependency.fromId.eq(pair.fromId()).and(dependency.toId.eq(pair.toId())));
        });
        long affected = jpaQueryFactory.delete(dependency)
                .where(builder)
                .execute();
        return (int) affected;
    }
}
