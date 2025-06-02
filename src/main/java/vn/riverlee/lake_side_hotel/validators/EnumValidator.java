package vn.riverlee.lake_side_hotel.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.riverlee.lake_side_hotel.annotations.EnumValid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumValidator implements ConstraintValidator<EnumValid, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(EnumValid annotation) {
        acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Cho phép null, nếu cần kiểm tra @NotNull thì thêm annotation riêng
        }
        return acceptedValues.contains(value.toUpperCase());
    }
}
