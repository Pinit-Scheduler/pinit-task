package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

public enum TaskType implements Recorder{
    DEEP_WORK {
        @Override
        public void record(Statistics statistics, Duration duration) {
            statistics.addDeepWorkDuration(duration);
        }

        @Override
        public void rollback(Statistics statistics, Duration duration) {
            statistics.removeDeepWorkDuration(duration);
        }
    },
    QUICK_TASK {
        @Override
        public void record(Statistics statistics, Duration duration) {
            statistics.addQuickWorkDuration(duration);
        }

        @Override
        public void rollback(Statistics statistics, Duration duration) {
            statistics.removeQuickWorkDuration(duration);
        }
    },
    ADMIN_TASK {
        @Override
        public void record(Statistics statistics, Duration duration) {
            statistics.addAdminWorkDuration(duration);
        }

        @Override
        public void rollback(Statistics statistics, Duration duration) {
            statistics.removeAdminWorkDuration(duration);
        }
    };

}
