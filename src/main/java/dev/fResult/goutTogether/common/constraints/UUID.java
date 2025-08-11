package dev.fResult.goutTogether.common.constraints;

import dev.fResult.goutTogether.common.validators.UuidValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UuidValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UUID {

  String message() default "must be a valid UUID";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
