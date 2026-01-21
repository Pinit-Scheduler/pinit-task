package me.gg.pinit.pinittask.interfaces.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class FibonacciDifficultyValidator implements ConstraintValidator<FibonacciDifficulty, Integer> {
    private static final Set<Integer> VALID_LEVELS = Set.of(1, 2, 3, 5, 8, 13, 21);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return VALID_LEVELS.contains(value);
    }
}
