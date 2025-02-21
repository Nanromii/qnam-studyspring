package vn.qnam.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {
    private List<String> values;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        values = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null) return true;
        return values.contains(charSequence.toString().toUpperCase());
    }
}
