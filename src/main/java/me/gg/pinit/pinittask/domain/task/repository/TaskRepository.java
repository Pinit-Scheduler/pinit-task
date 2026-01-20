package me.gg.pinit.pinittask.domain.task.repository;

import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerId(Long ownerId);
}
