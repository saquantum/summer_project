package org.example.mail.model;

// 此处做临时存储，没有使用数据库

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class EmailStorage {
    private static final Set<String> emailSet = new ConcurrentSkipListSet<>();

    public static void addEmail(String email) {
        emailSet.add(email);
    }

    public static Set<String> getEmails() {
        return emailSet;
    }

    public static void clear() {
        emailSet.clear();
    }
}
