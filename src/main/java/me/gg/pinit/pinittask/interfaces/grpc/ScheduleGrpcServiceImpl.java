package me.gg.pinit.pinittask.interfaces.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.grpc.GetScheduleRequest;
import me.gg.pinit.pinittask.grpc.GetScheduleResponse;
import me.gg.pinit.pinittask.grpc.ScheduleGrpcServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.ZonedDateTime;

@GrpcService
@RequiredArgsConstructor
public class ScheduleGrpcServiceImpl extends ScheduleGrpcServiceGrpc.ScheduleGrpcServiceImplBase {
    private final ScheduleService scheduleService;

    @Override
    public void getScheduleBasics(GetScheduleRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
        try {
            Schedule schedule = scheduleService.getSchedule(request.getOwnerId(), request.getScheduleId());
            ZonedDateTime startTime = schedule.getDesignatedStartTime();
            GetScheduleResponse response = GetScheduleResponse.newBuilder()
                    .setScheduleId(schedule.getId())
                    .setOwnerId(schedule.getOwnerId())
                    .setScheduleTitle(schedule.getTitle())
                    .setDesignatedStartTime(toTimestamp(startTime))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ScheduleNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to load schedule").augmentDescription(e.getMessage()).asRuntimeException());
        }
    }

    private Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        Instant instant = zonedDateTime.toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
