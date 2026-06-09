package com.idp.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewScheduleCalculator {

    public static class ReviewDate {
        public final LocalDate date;
        public final String label;
        public final String interval;

        public ReviewDate(LocalDate date, String label, String interval) {
            this.date = date;
            this.label = label;
            this.interval = interval;
        }
    }

    public static List<ReviewDate> calculateReviewDates(LocalDate transactionDate) {
        List<ReviewDate> reviewDates = new ArrayList<>();

        reviewDates.add(new ReviewDate(
            transactionDate.plusDays(30),
            "30-Day Review",
            "30 days"
        ));

        reviewDates.add(new ReviewDate(
            transactionDate.plusDays(90),
            "90-Day Review",
            "90 days"
        ));

        reviewDates.add(new ReviewDate(
            transactionDate.plusDays(180),
            "180-Day Review",
            "180 days"
        ));

        reviewDates.add(new ReviewDate(
            transactionDate.plusYears(1),
            "1-Year Review",
            "1 year"
        ));

        return reviewDates;
    }

    public static ReviewDate getNextReviewDate(LocalDate transactionDate) {
        LocalDate today = LocalDate.now();

        for (ReviewDate review : calculateReviewDates(transactionDate)) {
            if (review.date.isAfter(today) || review.date.isEqual(today)) {
                return review;
            }
        }

        // All reviews are in the past
        return null;
    }

    public static String getReviewStatus(LocalDate transactionDate) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDays = transactionDate.plusDays(30);
        LocalDate ninetyDays = transactionDate.plusDays(90);
        LocalDate oneEightyDays = transactionDate.plusDays(180);
        LocalDate oneYear = transactionDate.plusYears(1);

        if (today.isBefore(thirtyDays)) {
            return "scheduled";
        } else if (today.isBefore(ninetyDays)) {
            return "30-day_due";
        } else if (today.isBefore(oneEightyDays)) {
            return "90-day_due";
        } else if (today.isBefore(oneYear)) {
            return "180-day_due";
        } else {
            return "overdue";
        }
    }
}
