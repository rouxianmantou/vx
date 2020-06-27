package mirror;

import java.lang.reflect.Field;

public class RefDouble {
    private Field field;

    public RefDouble(Class cls, Field field2) throws NoSuchFieldException {
        this.field = cls.getDeclaredField(field2.getName());
        this.field.setAccessible(true);
    }

    public double get(Object obj) {
        try {
            return this.field.getDouble(obj);
        } catch (Exception unused) {
            return 0.0d;
        }
    }

    public void set(Object obj, double d) {
        try {
            this.field.setDouble(obj, d);
        } catch (Exception unused) {
        }
    }
}
