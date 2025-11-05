package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

public enum TaskType implements Recorder{
    DEEP_WORK {
        @Override
        public void record(Schedule ctx, Statistics statistics) {
            statistics.addDeepWorkDuration(durationFor(ctx));
        }

        @Override
        public void rollback(Schedule ctx, Statistics statistics) {
            statistics.removeDeepWorkDuration(durationFor(ctx));
        }
    },
    QUICK_TASK {
        @Override
        public void record(Schedule ctx, Statistics statistics) {
            statistics.addQuickWorkDuration(durationFor(ctx));
        }

        @Override
        public void rollback(Schedule ctx, Statistics statistics) {
            statistics.removeQuickWorkDuration(durationFor(ctx));
        }
    },
    ADMIN_TASK {
        @Override
        public void record(Schedule ctx, Statistics statistics) {
            statistics.addAdminWorkDuration(durationFor(ctx));
        }

        @Override
        public void rollback(Schedule ctx, Statistics statistics) {
            statistics.removeAdminWorkDuration(durationFor(ctx));
        }
    };

    private static Duration durationFor(Schedule ctx) {
        return ctx.getHistory().getElapsedTime();
    }
}
