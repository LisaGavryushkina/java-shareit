package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StartBeforeEndDateValidator implements ConstraintValidator<StartBeforeEndDateValid, HasStartEndDate> {

    @Override
    public void initialize(StartBeforeEndDateValid annotation) {
    }

    @Override
    public boolean isValid(HasStartEndDate bean, ConstraintValidatorContext context) {
        final LocalDateTime startDate = bean.getStart();
        final LocalDateTime endDate = bean.getEnd();

        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
}
