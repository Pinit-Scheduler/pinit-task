package me.gg.pinit.pinittask.domain.dependency.repository;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyRepository extends JpaRepository<Dependency, Long> {
}
