/*
 * Copyright 2017 BNY Mellon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bnymellon.codekatas.calendarkata;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.predicate.Predicate2;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.multimap.sortedset.MutableSortedSetMultimap;
import org.eclipse.collections.api.set.sorted.SortedSetIterable;
import org.eclipse.collections.impl.block.factory.Predicates2;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Multimaps;
import org.threeten.extra.Interval;

import java.time.*;
import java.util.TimeZone;

public class MyCalendar
{

    private TimeZone timezone = TimeZone.getDefault();
    private MutableSortedSetMultimap<LocalDate, Meeting> meetings;

    public MyCalendar(TimeZone timezone)
    {
        this.timezone = timezone;
        this.meetings = Multimaps.mutable.sortedSet.with(Meeting.COMPARATOR);
    }

    public ZoneId getZoneId()
    {
        return this.timezone.toZoneId();
    }

    public FullMonth getMeetingsForYearMonth(int year, Month month)
    {
        return new FullMonth(LocalDate.of(year, month, 1), this.meetings);
    }

    public SortedSetIterable<Meeting> getMeetingsForDate(LocalDate date)
    {
        SortedSetIterable<Meeting> set = this.meetings.get(date);
        return set;
    }

    public WorkWeek getMeetingsForWorkWeekOf(LocalDate value)
    {
        return new WorkWeek(value, this.meetings);
    }

    public FullWeek getMeetingsForFullWeekOf(LocalDate value)
    {
        return new FullWeek(value, this.meetings);
    }

    public boolean addMeeting(String subject, LocalDate date, LocalTime startTime, Duration duration)
    {
        if (!this.hasOverlappingMeeting(date, startTime, duration))
        {
            Meeting meeting = new Meeting(subject, date, startTime, duration, this.getZoneId());
            return this.meetings.put(date, meeting);
        }
        return false;
    }

    /**
     * Hint: Look at {@link Meeting#getInterval()}
     * Hint: Look at {@link Interval#overlaps(Interval)}
     * Hint: Look at {@link MyCalendar#getMeetingsForDate(LocalDate)}
     * Hint: Look at {@link RichIterable#anySatisfyWith(Predicate2, Object)}
     */
    public boolean hasOverlappingMeeting(LocalDate date, LocalTime startTime, Duration duration)
    {
        Meeting meeting = new Meeting("New Meeting", date, startTime, duration, this.getZoneId());
        Interval interval = meeting.getInterval();
        final SortedSetIterable<Meeting> meetingsForDate = getMeetingsForDate(date);
        return meetingsForDate.collect(Meeting::getInterval)
                .anySatisfy(interval::overlaps);
    }

    /**
     * Hint: Look at {@link MyCalendar#getMeetingsForDate(LocalDate)}
     * Hint: Look at {@link RichIterable#injectInto(Object, Function2)}
     * Hint: Look at {@link LocalDate#atTime(LocalTime)}
     * Hint: Look at {@link LocalDateTime#atZone(ZoneId)}
     * Hint: Look at {@link ZonedDateTime#toInstant()}
     * Hint: Look at {@link Interval#of(Instant, Duration)}
     */
    public MutableList<Interval> getAvailableTimeslots(LocalDate date)
    {
        final SortedSetIterable<Meeting> meetings = getMeetingsForDate(date);
        final MutableList<Interval> timeSlots = Lists.mutable.empty();

        ZonedDateTime dayStart = date.atTime(LocalTime.MIN).atZone(this.getZoneId());
        ZonedDateTime dayEnd = date.atTime(LocalTime.MAX).atZone(this.getZoneId());

        for (Meeting meeting : meetings) {
            ZonedDateTime meetingStart = date.atTime(meeting.getStartTime()).atZone(this.getZoneId());
            if (!(isTimeOverlapped(dayStart, meetingStart))) {
                addAvailableTimeSolts(timeSlots, dayStart, meetingStart);
            }
            dayStart = date.atTime(meeting.getEndTime()).atZone(this.getZoneId());
        }

        addAvailableTimeSolts(timeSlots, dayStart, dayEnd);

        return timeSlots;
    }

    private void addAvailableTimeSolts(MutableList<Interval> availableTimeSlots, ZonedDateTime startDateTime, ZonedDateTime meetingStartDateTime) {
        Duration meetingDuration = calculateDuration(startDateTime, meetingStartDateTime);
        if (!meetingDuration.equals(Duration.ZERO)) {
            Interval interval = Interval.of(startDateTime.toInstant(), meetingDuration);
            availableTimeSlots.add(interval);
        }
    }

    private boolean isTimeOverlapped(ZonedDateTime startDateTime, ZonedDateTime meetingStartDateTime) {
        return startDateTime.isAfter(meetingStartDateTime);
    }

    private Duration calculateDuration(ZonedDateTime startDateTime, ZonedDateTime meetingStartDateTime) {
        return Duration.between(startDateTime, meetingStartDateTime);
    }

    @Override
    public String toString()
    {
        return "MyCalendar(" +
                "meetings=" + this.meetings +
                ')';
    }
}
