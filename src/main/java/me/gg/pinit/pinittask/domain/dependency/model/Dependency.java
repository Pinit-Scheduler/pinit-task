package me.gg.pinit.pinittask.domain.dependency.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(
        name = "dependency",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dependency_from_to_schedule",
                        columnNames = {"from_id", "to_id"}
                )
        }
)
public class Dependency {
    @Id
    @Column(name = "dependency_id")
    @GeneratedValue
    private Long id;

    @Getter
    private Long ownerId;

    @Getter
    private Long fromId;

    @Getter
    private Long toId;

    protected Dependency() {
    }

    public Dependency(Long ownerId, Long fromId, Long toId) {
        validateOwner(ownerId);
        validateNotNull(fromId, toId);
        validateFromAndTo(fromId, toId);
        this.ownerId = ownerId;
        this.fromId = fromId;
        this.toId = toId;
    }

    private void validateOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId는 null일 수 없습니다.");
        }
    }

    private void validateNotNull(Long fromId, Long toId) {
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("fromId와 toId는 null일 수 없습니다.");
        }
    }

    private void validateFromAndTo(Long fromId, Long toId) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("fromId와 toId는 동일할 수 없습니다.");
        }
    }
}

/**
 * 의존관계 사용처
 * - 이전에 완료되어야 할 작업이 전부 완료되었는가? 체크하기 위한 용도
 * - 위상정렬의 의미
 * indegree가 0인 스케줄은 거기서 시작할 수 있다 - 사이클 체크
 * - 이전에 완료되어야 할 작업의 정보를 알아야 한다 -> 이전 노드의 정보 확인 필요
 * <p>
 * 이러면 Dependency가 하나의 애그리거트로 승격해야 한다.
 * 그래프 관련 관리 -> Dependency 애그리거트에서 관리
 * <p>
 * 단독으로 뭔가 작업을 처리할 게 있는가?
 * - 위상정렬 후 순서가 정상인가
 * - 이전 일정과 이후 일청의 위치에 따른 현재 일정 위치 배정
 * - 이전 일정이 모두 마무리되었는지 확인
 */

/**
 * 데드라인에 관한 문제
 * 다음 일정 시작 시점보다 반드시 앞에 있어야 하는가?
 *
 */
