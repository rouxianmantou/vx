package mirror;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RefMethod<T> {
    private Method method;

    public RefMethod(Class<?> cls, Field field) throws NoSuchMethodException {
        int i = 0;
        if (!field.isAnnotationPresent(MethodParams.class)) {
            if (!field.isAnnotationPresent(MethodReflectParams.class)) {
                Method[] declaredMethods = cls.getDeclaredMethods();
                int length = declaredMethods.length;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    Method method2 = declaredMethods[i];
                    if (method2.getName().equals(field.getName())) {
                        this.method = method2;
                        this.method.setAccessible(true);
                        break;
                    }
                    i++;
                }
            } else {
                String[] value = ((MethodReflectParams) field.getAnnotation(MethodReflectParams.class)).value();
                Class[] clsArr = new Class[value.length];
                while (i < value.length) {
                    Class protoType = RefStaticMethod.getProtoType(value[i]);
                    if (protoType == null) {
                        try {
                            protoType = Class.forName(value[i]);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    clsArr[i] = protoType;
                    i++;
                }
                this.method = cls.getDeclaredMethod(field.getName(), clsArr);
                this.method.setAccessible(true);
            }
        } else {
            Class[] value2 = ((MethodParams) field.getAnnotation(MethodParams.class)).value();
            while (i < value2.length) {
                Class cls2 = value2[i];
                if (cls2.getClassLoader() == getClass().getClassLoader()) {
                    try {
                        Class.forName(cls2.getName());
                        value2[i] = (Class) cls2.getField("TYPE").get(null);
                    } catch (Throwable th) {
                        throw new RuntimeException(th);
                    }
                }
                i++;
            }
            this.method = cls.getDeclaredMethod(field.getName(), value2);
            this.method.setAccessible(true);
        }
        if (this.method == null) {
            throw new NoSuchMethodException(field.getName());
        }
    }

    public T call(Object obj, Object... objArr) {
        try {
            return this.method.invoke(obj, objArr);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public T callWithException(Object obj, Object... objArr) throws Throwable {
        try {
            return this.method.invoke(obj, objArr);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }

    public Class<?>[] paramList() {
        return this.method.getParameterTypes();
    }
}
