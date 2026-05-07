package com.ooad.calendar;

import com.ooad.calendar.config.Database;

public class SmokeCheck {
    public static void main(String[] args) throws Exception {
        Database.initialize();
        System.out.println("OK: H2 database initialized.");
    }
}
