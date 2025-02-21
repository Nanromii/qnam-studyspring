package vn.qnam.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Constraint(validatedBy = EnumValueValidator.class)
public @interface EnumValue {
    String name();
    String message() default "{name} must be any of enum{enumClass}";
    Class<? extends Enum<?>> enumClass();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
