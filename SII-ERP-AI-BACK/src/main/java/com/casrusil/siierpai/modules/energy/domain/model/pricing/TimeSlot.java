package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

/**
 * Value object representing a time slot for pricing rules.
 */
public class TimeSlot {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<DayOfWeek> daysOfWeek;

    private TimeSlot(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> daysOfWeek) {
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "End time cannot be null");
        this.daysOfWeek = daysOfWeek != null ? Set.copyOf(daysOfWeek) : Set.of(DayOfWeek.values());

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    public static TimeSlot of(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> daysOfWeek) {
        return new TimeSlot(startTime, endTime, daysOfWeek);
    }

    /**
     * Creates a time slot for all days of the week.
     */
    public static TimeSlot allDays(LocalTime startTime, LocalTime endTime) {
        return new TimeSlot(startTime, endTime, null);
    }

    /**
     * Creates a time slot for weekdays only.
     */
    public static TimeSlot weekdays(LocalTime startTime, LocalTime endTime) {
        return new TimeSlot(startTime, endTime, Set.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    }

    /**
     * Creates a time slot for weekends only.
     */
    public static TimeSlot weekends(LocalTime startTime, LocalTime endTime) {
        return new TimeSlot(startTime, endTime, Set.of(
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
    }

    /**
     * Checks if the given time falls within this time slot.
     */
    public boolean contains(LocalTime time, DayOfWeek dayOfWeek) {
        return daysOfWeek.contains(dayOfWeek)
                && !time.isBefore(startTime)
                && !time.isAfter(endTime);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(daysOfWeek, timeSlot.daysOfWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, daysOfWeek);
    }

    @Override
    public String toString() {
        return startTime + " - " + endTime + " (" + daysOfWeek + ")";
    }
}
