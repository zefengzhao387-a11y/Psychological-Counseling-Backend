package org.example.consultation.util;

import org.example.consultation.entity.ConsultationAppointment;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 咨询时段占用工具（默认 8 周同一星期 + 同一时间段）
 */
public final class WeeklySlotUtil {

    private WeeklySlotUtil() {}

    /** 新建安排：从 startDate 起连续 weeks 周 */
    public static Set<LocalDate> slotsForNewAppointment(LocalDate startDate, int weeks) {
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < weeks; i++) {
            dates.add(startDate.plusWeeks(i));
        }
        return dates;
    }

    /** 进行中安排：根据剩余周数计算未来仍占用的日期 */
    public static Set<LocalDate> slotsForExistingAppointment(ConsultationAppointment app) {
        int total = app.getOccupiedWeeks() != null ? app.getOccupiedWeeks() : 8;
        int remaining = app.getRemainingWeeks() != null ? app.getRemainingWeeks() : total;
        if (remaining <= 0) {
            return Set.of();
        }
        int used = Math.max(0, total - remaining);
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < remaining; i++) {
            dates.add(app.getStartDate().plusWeeks(used + (long) i));
        }
        return dates;
    }

    public static boolean hasOverlap(Set<LocalDate> a, Set<LocalDate> b) {
        for (LocalDate d : a) {
            if (b.contains(d)) {
                return true;
            }
        }
        return false;
    }
}
