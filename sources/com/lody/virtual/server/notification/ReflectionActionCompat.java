package com.lody.virtual.server.notification;

import android.widget.RemoteViews;

class ReflectionActionCompat {
    static final int BITMAP = 12;
    static final int BOOLEAN = 1;
    static final int BUNDLE = 13;
    static final int BYTE = 2;
    static final int CHAR = 8;
    static final int CHAR_SEQUENCE = 10;
    static final int COLOR_STATE_LIST = 15;
    static final int DOUBLE = 7;
    static final int FLOAT = 6;
    static final int ICON = 16;
    static final int INT = 4;
    static final int INTENT = 14;
    static final int LONG = 5;
    private static final String ReflectionAction = "ReflectionAction";
    private static Class ReflectionActionClass = null;
    static final int SHORT = 3;
    static final int STRING = 9;
    static final int TAG = 2;
    static final int URI = 11;

    ReflectionActionCompat() {
    }

    static {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(RemoteViews.class.getName());
            sb.append("$");
            sb.append(ReflectionAction);
            ReflectionActionClass = Class.forName(sb.toString());
        } catch (ClassNotFoundException unused) {
        }
    }

    static boolean isInstance(Object obj) {
        return ReflectionActionClass != null && ReflectionActionClass.isInstance(obj);
    }
}
