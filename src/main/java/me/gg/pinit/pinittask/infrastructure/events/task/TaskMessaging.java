package me.gg.pinit.pinittask.infrastructure.events.task;

/**
 * Task에 대한 RabbitMQ Queue와 Exchange 정의
 */
public class TaskMessaging {
    public static final String DIRECT_EXCHANGE = "task.task.direct";
    public static final String RK_TASK_COMPLETED = "task.completed";
    public static final String RK_TASK_CANCELED = "task.canceled";
}
