package mirror.android.content.p016pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.DisplayMetrics;
import java.io.File;
import java.util.List;
import mirror.MethodParams;
import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.RefObject;
import mirror.RefStaticMethod;

/* renamed from: mirror.android.content.pm.PackageParser */
public class PackageParser {
    public static Class<?> TYPE = RefClass.load(PackageParser.class, "android.content.pm.PackageParser");
    @MethodReflectParams({"android.content.pm.PackageParser$Package", "int"})
    public static RefMethod<Void> collectCertificates;
    @MethodParams({String.class})
    public static RefConstructor<android.content.p000pm.PackageParser> ctor;
    @MethodReflectParams({"android.content.pm.PackageParser$Activity", "int"})
    public static RefStaticMethod<ActivityInfo> generateActivityInfo;
    @MethodReflectParams({"android.content.pm.PackageParser$Package", "int"})
    public static RefStaticMethod<ApplicationInfo> generateApplicationInfo;
    @MethodReflectParams({"android.content.pm.PackageParser$Package", "[I", "int", "long", "long"})
    public static RefStaticMethod<PackageInfo> generatePackageInfo;
    @MethodReflectParams({"android.content.pm.PackageParser$Provider", "int"})
    public static RefStaticMethod<ProviderInfo> generateProviderInfo;
    @MethodReflectParams({"android.content.pm.PackageParser$Service", "int"})
    public static RefStaticMethod<ServiceInfo> generateServiceInfo;
    @MethodParams({File.class, String.class, DisplayMetrics.class, int.class})
    public static RefMethod<android.content.p000pm.PackageParser.Package> parsePackage;

    /* renamed from: mirror.android.content.pm.PackageParser$Activity */
    public static class Activity {
        public static Class<?> TYPE = RefClass.load(Activity.class, "android.content.pm.PackageParser$Activity");
        public static RefObject<ActivityInfo> info;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$Component */
    public static class Component {
        public static Class<?> TYPE = RefClass.load(Component.class, "android.content.pm.PackageParser$Component");
        public static RefObject<String> className;
        public static RefObject<ComponentName> componentName;
        public static RefObject<List<IntentFilter>> intents;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$Package */
    public static class Package {
        public static Class<?> TYPE = RefClass.load(Package.class, "android.content.pm.PackageParser$Package");
        public static RefObject<List> activities;
        public static RefObject<Bundle> mAppMetaData;
        public static RefObject<String> mSharedUserId;
        public static RefObject<Signature[]> mSignatures;
        public static RefObject<Object> mSigningDetails;
        public static RefObject<Integer> mVersionCode;
        public static RefObject<String> packageName;
        public static RefObject<List> permissionGroups;
        public static RefObject<List> permissions;
        public static RefObject<List<String>> protectedBroadcasts;
        public static RefObject<List> providers;
        public static RefObject<List> receivers;
        public static RefObject<List<String>> requestedPermissions;
        public static RefObject<List> services;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$Permission */
    public static class Permission {
        public static Class<?> TYPE = RefClass.load(Permission.class, "android.content.pm.PackageParser$Permission");
        public static RefObject<PermissionInfo> info;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$PermissionGroup */
    public static class PermissionGroup {
        public static Class<?> TYPE = RefClass.load(PermissionGroup.class, "android.content.pm.PackageParser$PermissionGroup");
        public static RefObject<PermissionGroupInfo> info;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$Provider */
    public static class Provider {
        public static Class<?> TYPE = RefClass.load(Provider.class, "android.content.pm.PackageParser$Provider");
        public static RefObject<ProviderInfo> info;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$Service */
    public static class Service {
        public static Class<?> TYPE = RefClass.load(Provider.class, "android.content.pm.PackageParser$Service");
        public static RefObject<ServiceInfo> info;
    }

    /* renamed from: mirror.android.content.pm.PackageParser$SigningDetails */
    public static class SigningDetails {
        public static Class<?> TYPE = RefClass.load(SigningDetails.class, "android.content.pm.PackageParser$SigningDetails");
        public static RefMethod<Boolean> hasPastSigningCertificates;
        public static RefMethod<Boolean> hasSignatures;
        public static RefObject<Signature[]> pastSigningCertificates;
        public static RefObject<Signature[]> signatures;
    }
}
