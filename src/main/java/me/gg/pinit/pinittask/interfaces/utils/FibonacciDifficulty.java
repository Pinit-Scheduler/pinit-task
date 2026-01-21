package me.gg.pinit.pinittask.interfaces.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = FibonacciDifficultyValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface FibonacciDifficulty {
    String message() default "난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
