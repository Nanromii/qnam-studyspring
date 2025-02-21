package vn.qnam.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class GenderSubsetValidator implements ConstraintValidator<GenderSubset, Gender> {
    private Gender[] genders;

    @Override
    public void initialize(GenderSubset constraintAnnotation) {
        this.genders = constraintAnnotation.anyOf();
    }

    @Override
    public boolean isValid(Gender gender, ConstraintValidatorContext constraintValidatorContext) {
        return gender == null || Arrays.asList(genders).contains(gender);
    }
}
