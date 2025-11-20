package me.gg.pinit.pinittask.domain.dependency.repository;

import java.util.List;

public interface DependencyRepositoryCustom {
    int deleteByFromToPairs(List<FromToPair> pairs);
}
