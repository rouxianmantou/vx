package android.support.animation;

import android.support.annotation.RequiresApi;
import android.util.FloatProperty;

public abstract class FloatPropertyCompat<T> {
    final String mPropertyName;

    public abstract float getValue(T t);

    public abstract void setValue(T t, float f);

    public FloatPropertyCompat(String str) {
        this.mPropertyName = str;
    }

    @RequiresApi(24)
    public static <T> FloatPropertyCompat<T> createFloatPropertyCompat(final FloatProperty<T> floatProperty) {
        return new FloatPropertyCompat<T>(floatProperty.getName()) {
            public float getValue(T t) {
                return ((Float) floatProperty.get(t)).floatValue();
            }

            public void setValue(T t, float f) {
                floatProperty.setValue(t, f);
            }
        };
    }
}