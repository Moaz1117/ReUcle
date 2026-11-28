package edu.aiu.reucle.collection.repository;

import edu.aiu.reucle.collection.model.CollectionTask;
import java.util.List;
import java.util.Optional;

// PM2: collection-service Class/Collaboration Diagram -> ICollectionRepository
public interface ICollectionRepository {
    Optional<CollectionTask> findById(String taskId);
    List<CollectionTask> findByStatus(CollectionTask.TaskStatus status);
    CollectionTask save(CollectionTask task);
}
