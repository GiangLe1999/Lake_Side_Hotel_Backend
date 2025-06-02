package vn.riverlee.lake_side_hotel.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.riverlee.lake_side_hotel.validators.EnumValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValid {
    String message() default "Invalid value. This is not a valid enum value.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
