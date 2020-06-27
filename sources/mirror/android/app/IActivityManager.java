package mirror.android.app;

import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.IInterface;
import mirror.MethodParams;
import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;

public class IActivityManager {
    public static Class<?> TYPE = RefClass.load(IActivityManager.class, "android.app.IActivityManager");
    @MethodParams({IBinder.class, boolean.class})
    public static RefMethod<Integer> getTaskForActivity;
    @MethodParams({IBinder.class, String.class, int.class, int.class})
    public static RefMethod<Void> overridePendingTransition;
    @MethodParams({IBinder.class, int.class})
    public static RefMethod<Void> setRequestedOrientation;
    public static RefMethod<Integer> startActivities;
    public static RefMethod<Integer> startActivity;

    public static class ContentProviderHolder {
        public static Class<?> TYPE = RefClass.load(ContentProviderHolder.class, "android.app.IActivityManager$ContentProviderHolder");
        public static RefObject<ProviderInfo> info;
        public static RefBoolean noReleaseNeeded;
        public static RefObject<IInterface> provider;
    }
}
