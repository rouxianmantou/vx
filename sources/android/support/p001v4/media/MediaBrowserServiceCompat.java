package android.support.p001v4.media;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p001v4.app.BundleCompat;
import android.support.p001v4.media.MediaBrowserCompat.MediaItem;
import android.support.p001v4.media.MediaBrowserServiceCompatApi21.ServiceCompatProxy;
import android.support.p001v4.media.session.IMediaSession;
import android.support.p001v4.media.session.MediaSessionCompat.Token;
import android.support.p001v4.p003os.ResultReceiver;
import android.support.p001v4.util.ArrayMap;
import android.support.p001v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* renamed from: android.support.v4.media.MediaBrowserServiceCompat */
public abstract class MediaBrowserServiceCompat extends Service {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final float EPSILON = 1.0E-5f;
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static final String KEY_MEDIA_ITEM = "media_item";
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static final String KEY_SEARCH_RESULTS = "search_results";
    static final int RESULT_ERROR = -1;
    static final int RESULT_FLAG_ON_LOAD_ITEM_NOT_IMPLEMENTED = 2;
    static final int RESULT_FLAG_ON_SEARCH_NOT_IMPLEMENTED = 4;
    static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    static final int RESULT_OK = 0;
    static final int RESULT_PROGRESS_UPDATE = 1;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    static final String TAG = "MBServiceCompat";
    final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap<>();
    ConnectionRecord mCurConnection;
    final ServiceHandler mHandler = new ServiceHandler();
    private MediaBrowserServiceImpl mImpl;
    Token mSession;

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$BrowserRoot */
    public static final class BrowserRoot {
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";
        @Deprecated
        public static final String EXTRA_SUGGESTION_KEYWORDS = "android.service.media.extra.SUGGESTION_KEYWORDS";
        private final Bundle mExtras;
        private final String mRootId;

        public BrowserRoot(@NonNull String str, @Nullable Bundle bundle) {
            if (str != null) {
                this.mRootId = str;
                this.mExtras = bundle;
                return;
            }
            throw new IllegalArgumentException("The root id in BrowserRoot cannot be null. Use null for BrowserRoot instead.");
        }

        public String getRootId() {
            return this.mRootId;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ConnectionRecord */
    private class ConnectionRecord implements DeathRecipient {
        ServiceCallbacks callbacks;
        String pkg;
        BrowserRoot root;
        Bundle rootHints;
        HashMap<String, List<Pair<IBinder, Bundle>>> subscriptions = new HashMap<>();

        ConnectionRecord() {
        }

        public void binderDied() {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                public void run() {
                    MediaBrowserServiceCompat.this.mConnections.remove(ConnectionRecord.this.callbacks.asBinder());
                }
            });
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$MediaBrowserServiceImpl */
    interface MediaBrowserServiceImpl {
        Bundle getBrowserRootHints();

        void notifyChildrenChanged(String str, Bundle bundle);

        IBinder onBind(Intent intent);

        void onCreate();

        void setSessionToken(Token token);
    }

    @RequiresApi(21)
    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$MediaBrowserServiceImplApi21 */
    class MediaBrowserServiceImplApi21 implements MediaBrowserServiceImpl, ServiceCompatProxy {
        Messenger mMessenger;
        final List<Bundle> mRootExtrasList = new ArrayList();
        Object mServiceObj;

        MediaBrowserServiceImplApi21() {
        }

        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi21.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        public IBinder onBind(Intent intent) {
            return MediaBrowserServiceCompatApi21.onBind(this.mServiceObj, intent);
        }

        public void setSessionToken(final Token token) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                public void run() {
                    if (!MediaBrowserServiceImplApi21.this.mRootExtrasList.isEmpty()) {
                        IMediaSession extraBinder = token.getExtraBinder();
                        if (extraBinder != null) {
                            for (Bundle putBinder : MediaBrowserServiceImplApi21.this.mRootExtrasList) {
                                BundleCompat.putBinder(putBinder, MediaBrowserProtocol.EXTRA_SESSION_BINDER, extraBinder.asBinder());
                            }
                        }
                        MediaBrowserServiceImplApi21.this.mRootExtrasList.clear();
                    }
                    MediaBrowserServiceCompatApi21.setSessionToken(MediaBrowserServiceImplApi21.this.mServiceObj, token.getToken());
                }
            });
        }

        public void notifyChildrenChanged(String str, Bundle bundle) {
            notifyChildrenChangedForFramework(str, bundle);
            notifyChildrenChangedForCompat(str, bundle);
        }

        public Bundle getBrowserRootHints() {
            Bundle bundle = null;
            if (this.mMessenger == null) {
                return null;
            }
            if (MediaBrowserServiceCompat.this.mCurConnection != null) {
                if (MediaBrowserServiceCompat.this.mCurConnection.rootHints != null) {
                    bundle = new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
                }
                return bundle;
            }
            throw new IllegalStateException("This should be called inside of onLoadChildren, onLoadItem or onSearch methods");
        }

        public BrowserRoot onGetRoot(String str, int i, Bundle bundle) {
            Bundle bundle2;
            IBinder iBinder;
            if (bundle == null || bundle.getInt(MediaBrowserProtocol.EXTRA_CLIENT_VERSION, 0) == 0) {
                bundle2 = null;
            } else {
                bundle.remove(MediaBrowserProtocol.EXTRA_CLIENT_VERSION);
                this.mMessenger = new Messenger(MediaBrowserServiceCompat.this.mHandler);
                bundle2 = new Bundle();
                bundle2.putInt(MediaBrowserProtocol.EXTRA_SERVICE_VERSION, 2);
                BundleCompat.putBinder(bundle2, MediaBrowserProtocol.EXTRA_MESSENGER_BINDER, this.mMessenger.getBinder());
                if (MediaBrowserServiceCompat.this.mSession != null) {
                    IMediaSession extraBinder = MediaBrowserServiceCompat.this.mSession.getExtraBinder();
                    String str2 = MediaBrowserProtocol.EXTRA_SESSION_BINDER;
                    if (extraBinder == null) {
                        iBinder = null;
                    } else {
                        iBinder = extraBinder.asBinder();
                    }
                    BundleCompat.putBinder(bundle2, str2, iBinder);
                } else {
                    this.mRootExtrasList.add(bundle2);
                }
            }
            BrowserRoot onGetRoot = MediaBrowserServiceCompat.this.onGetRoot(str, i, bundle);
            if (onGetRoot == null) {
                return null;
            }
            if (bundle2 == null) {
                bundle2 = onGetRoot.getExtras();
            } else if (onGetRoot.getExtras() != null) {
                bundle2.putAll(onGetRoot.getExtras());
            }
            return new BrowserRoot(onGetRoot.getRootId(), bundle2);
        }

        public void onLoadChildren(String str, final ResultWrapper<List<Parcel>> resultWrapper) {
            MediaBrowserServiceCompat.this.onLoadChildren(str, new Result<List<MediaItem>>(str) {
                /* access modifiers changed from: 0000 */
                public void onResultSent(List<MediaItem> list) {
                    ArrayList arrayList;
                    if (list != null) {
                        arrayList = new ArrayList();
                        for (MediaItem mediaItem : list) {
                            Parcel obtain = Parcel.obtain();
                            mediaItem.writeToParcel(obtain, 0);
                            arrayList.add(obtain);
                        }
                    } else {
                        arrayList = null;
                    }
                    resultWrapper.sendResult(arrayList);
                }

                public void detach() {
                    resultWrapper.detach();
                }
            });
        }

        /* access modifiers changed from: 0000 */
        public void notifyChildrenChangedForFramework(String str, Bundle bundle) {
            MediaBrowserServiceCompatApi21.notifyChildrenChanged(this.mServiceObj, str);
        }

        /* access modifiers changed from: 0000 */
        public void notifyChildrenChangedForCompat(final String str, final Bundle bundle) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                public void run() {
                    for (IBinder iBinder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                        ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(iBinder);
                        List<Pair> list = (List) connectionRecord.subscriptions.get(str);
                        if (list != null) {
                            for (Pair pair : list) {
                                if (MediaBrowserCompatUtils.hasDuplicatedItems(bundle, (Bundle) pair.second)) {
                                    MediaBrowserServiceCompat.this.performLoadChildren(str, connectionRecord, (Bundle) pair.second);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @RequiresApi(23)
    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$MediaBrowserServiceImplApi23 */
    class MediaBrowserServiceImplApi23 extends MediaBrowserServiceImplApi21 implements MediaBrowserServiceCompatApi23.ServiceCompatProxy {
        MediaBrowserServiceImplApi23() {
            super();
        }

        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi23.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        public void onLoadItem(String str, final ResultWrapper<Parcel> resultWrapper) {
            MediaBrowserServiceCompat.this.onLoadItem(str, new Result<MediaItem>(str) {
                /* access modifiers changed from: 0000 */
                public void onResultSent(MediaItem mediaItem) {
                    if (mediaItem == null) {
                        resultWrapper.sendResult(null);
                        return;
                    }
                    Parcel obtain = Parcel.obtain();
                    mediaItem.writeToParcel(obtain, 0);
                    resultWrapper.sendResult(obtain);
                }

                public void detach() {
                    resultWrapper.detach();
                }
            });
        }
    }

    @RequiresApi(26)
    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$MediaBrowserServiceImplApi26 */
    class MediaBrowserServiceImplApi26 extends MediaBrowserServiceImplApi23 implements MediaBrowserServiceCompatApi26.ServiceCompatProxy {
        MediaBrowserServiceImplApi26() {
            super();
        }

        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi26.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        public void onLoadChildren(String str, final ResultWrapper resultWrapper, Bundle bundle) {
            MediaBrowserServiceCompat.this.onLoadChildren(str, new Result<List<MediaItem>>(str) {
                /* access modifiers changed from: 0000 */
                public void onResultSent(List<MediaItem> list) {
                    ArrayList arrayList;
                    if (list != null) {
                        arrayList = new ArrayList();
                        for (MediaItem mediaItem : list) {
                            Parcel obtain = Parcel.obtain();
                            mediaItem.writeToParcel(obtain, 0);
                            arrayList.add(obtain);
                        }
                    } else {
                        arrayList = null;
                    }
                    resultWrapper.sendResult(arrayList, getFlags());
                }

                public void detach() {
                    resultWrapper.detach();
                }
            }, bundle);
        }

        public Bundle getBrowserRootHints() {
            if (MediaBrowserServiceCompat.this.mCurConnection == null) {
                return MediaBrowserServiceCompatApi26.getBrowserRootHints(this.mServiceObj);
            }
            return MediaBrowserServiceCompat.this.mCurConnection.rootHints == null ? null : new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
        }

        /* access modifiers changed from: 0000 */
        public void notifyChildrenChangedForFramework(String str, Bundle bundle) {
            if (bundle != null) {
                MediaBrowserServiceCompatApi26.notifyChildrenChanged(this.mServiceObj, str, bundle);
            } else {
                super.notifyChildrenChangedForFramework(str, bundle);
            }
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$MediaBrowserServiceImplBase */
    class MediaBrowserServiceImplBase implements MediaBrowserServiceImpl {
        private Messenger mMessenger;

        MediaBrowserServiceImplBase() {
        }

        public void onCreate() {
            this.mMessenger = new Messenger(MediaBrowserServiceCompat.this.mHandler);
        }

        public IBinder onBind(Intent intent) {
            if (MediaBrowserServiceCompat.SERVICE_INTERFACE.equals(intent.getAction())) {
                return this.mMessenger.getBinder();
            }
            return null;
        }

        public void setSessionToken(final Token token) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                public void run() {
                    Iterator it = MediaBrowserServiceCompat.this.mConnections.values().iterator();
                    while (it.hasNext()) {
                        ConnectionRecord connectionRecord = (ConnectionRecord) it.next();
                        try {
                            connectionRecord.callbacks.onConnect(connectionRecord.root.getRootId(), token, connectionRecord.root.getExtras());
                        } catch (RemoteException unused) {
                            String str = MediaBrowserServiceCompat.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("Connection for ");
                            sb.append(connectionRecord.pkg);
                            sb.append(" is no longer valid.");
                            Log.w(str, sb.toString());
                            it.remove();
                        }
                    }
                }
            });
        }

        public void notifyChildrenChanged(@NonNull final String str, final Bundle bundle) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                public void run() {
                    for (IBinder iBinder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                        ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(iBinder);
                        List<Pair> list = (List) connectionRecord.subscriptions.get(str);
                        if (list != null) {
                            for (Pair pair : list) {
                                if (MediaBrowserCompatUtils.hasDuplicatedItems(bundle, (Bundle) pair.second)) {
                                    MediaBrowserServiceCompat.this.performLoadChildren(str, connectionRecord, (Bundle) pair.second);
                                }
                            }
                        }
                    }
                }
            });
        }

        public Bundle getBrowserRootHints() {
            if (MediaBrowserServiceCompat.this.mCurConnection == null) {
                throw new IllegalStateException("This should be called inside of onLoadChildren, onLoadItem or onSearch methods");
            } else if (MediaBrowserServiceCompat.this.mCurConnection.rootHints == null) {
                return null;
            } else {
                return new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
            }
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$Result */
    public static class Result<T> {
        private final Object mDebug;
        private boolean mDetachCalled;
        private int mFlags;
        private boolean mSendErrorCalled;
        private boolean mSendProgressUpdateCalled;
        private boolean mSendResultCalled;

        /* access modifiers changed from: 0000 */
        public void onResultSent(T t) {
        }

        Result(Object obj) {
            this.mDebug = obj;
        }

        public void sendResult(T t) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                StringBuilder sb = new StringBuilder();
                sb.append("sendResult() called when either sendResult() or sendError() had already been called for: ");
                sb.append(this.mDebug);
                throw new IllegalStateException(sb.toString());
            }
            this.mSendResultCalled = true;
            onResultSent(t);
        }

        public void sendProgressUpdate(Bundle bundle) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                StringBuilder sb = new StringBuilder();
                sb.append("sendProgressUpdate() called when either sendResult() or sendError() had already been called for: ");
                sb.append(this.mDebug);
                throw new IllegalStateException(sb.toString());
            }
            checkExtraFields(bundle);
            this.mSendProgressUpdateCalled = true;
            onProgressUpdateSent(bundle);
        }

        public void sendError(Bundle bundle) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                StringBuilder sb = new StringBuilder();
                sb.append("sendError() called when either sendResult() or sendError() had already been called for: ");
                sb.append(this.mDebug);
                throw new IllegalStateException(sb.toString());
            }
            this.mSendErrorCalled = true;
            onErrorSent(bundle);
        }

        public void detach() {
            if (this.mDetachCalled) {
                StringBuilder sb = new StringBuilder();
                sb.append("detach() called when detach() had already been called for: ");
                sb.append(this.mDebug);
                throw new IllegalStateException(sb.toString());
            } else if (this.mSendResultCalled) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("detach() called when sendResult() had already been called for: ");
                sb2.append(this.mDebug);
                throw new IllegalStateException(sb2.toString());
            } else if (!this.mSendErrorCalled) {
                this.mDetachCalled = true;
            } else {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("detach() called when sendError() had already been called for: ");
                sb3.append(this.mDebug);
                throw new IllegalStateException(sb3.toString());
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean isDone() {
            return this.mDetachCalled || this.mSendResultCalled || this.mSendErrorCalled;
        }

        /* access modifiers changed from: 0000 */
        public void setFlags(int i) {
            this.mFlags = i;
        }

        /* access modifiers changed from: 0000 */
        public int getFlags() {
            return this.mFlags;
        }

        /* access modifiers changed from: 0000 */
        public void onProgressUpdateSent(Bundle bundle) {
            StringBuilder sb = new StringBuilder();
            sb.append("It is not supported to send an interim update for ");
            sb.append(this.mDebug);
            throw new UnsupportedOperationException(sb.toString());
        }

        /* access modifiers changed from: 0000 */
        public void onErrorSent(Bundle bundle) {
            StringBuilder sb = new StringBuilder();
            sb.append("It is not supported to send an error for ");
            sb.append(this.mDebug);
            throw new UnsupportedOperationException(sb.toString());
        }

        private void checkExtraFields(Bundle bundle) {
            if (bundle != null && bundle.containsKey(MediaBrowserCompat.EXTRA_DOWNLOAD_PROGRESS)) {
                float f = bundle.getFloat(MediaBrowserCompat.EXTRA_DOWNLOAD_PROGRESS);
                if (f < -1.0E-5f || f > 1.00001f) {
                    throw new IllegalArgumentException("The value of the EXTRA_DOWNLOAD_PROGRESS field must be a float number within [0.0, 1.0].");
                }
            }
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ResultFlags */
    private @interface ResultFlags {
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ServiceBinderImpl */
    private class ServiceBinderImpl {
        ServiceBinderImpl() {
        }

        public void connect(String str, int i, Bundle bundle, ServiceCallbacks serviceCallbacks) {
            if (MediaBrowserServiceCompat.this.isValidPackage(str, i)) {
                ServiceHandler serviceHandler = MediaBrowserServiceCompat.this.mHandler;
                final ServiceCallbacks serviceCallbacks2 = serviceCallbacks;
                final String str2 = str;
                final Bundle bundle2 = bundle;
                final int i2 = i;
                C02601 r1 = new Runnable() {
                    public void run() {
                        IBinder asBinder = serviceCallbacks2.asBinder();
                        MediaBrowserServiceCompat.this.mConnections.remove(asBinder);
                        ConnectionRecord connectionRecord = new ConnectionRecord();
                        connectionRecord.pkg = str2;
                        connectionRecord.rootHints = bundle2;
                        connectionRecord.callbacks = serviceCallbacks2;
                        connectionRecord.root = MediaBrowserServiceCompat.this.onGetRoot(str2, i2, bundle2);
                        if (connectionRecord.root == null) {
                            String str = MediaBrowserServiceCompat.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("No root for client ");
                            sb.append(str2);
                            sb.append(" from service ");
                            sb.append(getClass().getName());
                            Log.i(str, sb.toString());
                            try {
                                serviceCallbacks2.onConnectFailed();
                            } catch (RemoteException unused) {
                                String str2 = MediaBrowserServiceCompat.TAG;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Calling onConnectFailed() failed. Ignoring. pkg=");
                                sb2.append(str2);
                                Log.w(str2, sb2.toString());
                            }
                        } else {
                            try {
                                MediaBrowserServiceCompat.this.mConnections.put(asBinder, connectionRecord);
                                asBinder.linkToDeath(connectionRecord, 0);
                                if (MediaBrowserServiceCompat.this.mSession != null) {
                                    serviceCallbacks2.onConnect(connectionRecord.root.getRootId(), MediaBrowserServiceCompat.this.mSession, connectionRecord.root.getExtras());
                                }
                            } catch (RemoteException unused2) {
                                String str3 = MediaBrowserServiceCompat.TAG;
                                StringBuilder sb3 = new StringBuilder();
                                sb3.append("Calling onConnect() failed. Dropping client. pkg=");
                                sb3.append(str2);
                                Log.w(str3, sb3.toString());
                                MediaBrowserServiceCompat.this.mConnections.remove(asBinder);
                            }
                        }
                    }
                };
                serviceHandler.postOrRun(r1);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Package/uid mismatch: uid=");
            sb.append(i);
            sb.append(" package=");
            sb.append(str);
            throw new IllegalArgumentException(sb.toString());
        }

        public void disconnect(final ServiceCallbacks serviceCallbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                public void run() {
                    ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.remove(serviceCallbacks.asBinder());
                    if (connectionRecord != null) {
                        connectionRecord.callbacks.asBinder().unlinkToDeath(connectionRecord, 0);
                    }
                }
            });
        }

        public void addSubscription(String str, IBinder iBinder, Bundle bundle, ServiceCallbacks serviceCallbacks) {
            ServiceHandler serviceHandler = MediaBrowserServiceCompat.this.mHandler;
            final ServiceCallbacks serviceCallbacks2 = serviceCallbacks;
            final String str2 = str;
            final IBinder iBinder2 = iBinder;
            final Bundle bundle2 = bundle;
            C02623 r1 = new Runnable() {
                public void run() {
                    ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(serviceCallbacks2.asBinder());
                    if (connectionRecord == null) {
                        String str = MediaBrowserServiceCompat.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("addSubscription for callback that isn't registered id=");
                        sb.append(str2);
                        Log.w(str, sb.toString());
                        return;
                    }
                    MediaBrowserServiceCompat.this.addSubscription(str2, connectionRecord, iBinder2, bundle2);
                }
            };
            serviceHandler.postOrRun(r1);
        }

        public void removeSubscription(final String str, final IBinder iBinder, final ServiceCallbacks serviceCallbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                public void run() {
                    ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(serviceCallbacks.asBinder());
                    if (connectionRecord == null) {
                        String str = MediaBrowserServiceCompat.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("removeSubscription for callback that isn't registered id=");
                        sb.append(str);
                        Log.w(str, sb.toString());
                        return;
                    }
                    if (!MediaBrowserServiceCompat.this.removeSubscription(str, connectionRecord, iBinder)) {
                        String str2 = MediaBrowserServiceCompat.TAG;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("removeSubscription called for ");
                        sb2.append(str);
                        sb2.append(" which is not subscribed");
                        Log.w(str2, sb2.toString());
                    }
                }
            });
        }

        public void getMediaItem(final String str, final ResultReceiver resultReceiver, final ServiceCallbacks serviceCallbacks) {
            if (!TextUtils.isEmpty(str) && resultReceiver != null) {
                MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                    public void run() {
                        ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(serviceCallbacks.asBinder());
                        if (connectionRecord == null) {
                            String str = MediaBrowserServiceCompat.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("getMediaItem for callback that isn't registered id=");
                            sb.append(str);
                            Log.w(str, sb.toString());
                            return;
                        }
                        MediaBrowserServiceCompat.this.performLoadItem(str, connectionRecord, resultReceiver);
                    }
                });
            }
        }

        public void registerCallbacks(final ServiceCallbacks serviceCallbacks, final Bundle bundle) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                public void run() {
                    IBinder asBinder = serviceCallbacks.asBinder();
                    MediaBrowserServiceCompat.this.mConnections.remove(asBinder);
                    ConnectionRecord connectionRecord = new ConnectionRecord();
                    connectionRecord.callbacks = serviceCallbacks;
                    connectionRecord.rootHints = bundle;
                    MediaBrowserServiceCompat.this.mConnections.put(asBinder, connectionRecord);
                    try {
                        asBinder.linkToDeath(connectionRecord, 0);
                    } catch (RemoteException unused) {
                        Log.w(MediaBrowserServiceCompat.TAG, "IBinder is already dead.");
                    }
                }
            });
        }

        public void unregisterCallbacks(final ServiceCallbacks serviceCallbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                public void run() {
                    IBinder asBinder = serviceCallbacks.asBinder();
                    ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.remove(asBinder);
                    if (connectionRecord != null) {
                        asBinder.unlinkToDeath(connectionRecord, 0);
                    }
                }
            });
        }

        public void search(String str, Bundle bundle, ResultReceiver resultReceiver, ServiceCallbacks serviceCallbacks) {
            if (!TextUtils.isEmpty(str) && resultReceiver != null) {
                ServiceHandler serviceHandler = MediaBrowserServiceCompat.this.mHandler;
                final ServiceCallbacks serviceCallbacks2 = serviceCallbacks;
                final String str2 = str;
                final Bundle bundle2 = bundle;
                final ResultReceiver resultReceiver2 = resultReceiver;
                C02678 r1 = new Runnable() {
                    public void run() {
                        ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(serviceCallbacks2.asBinder());
                        if (connectionRecord == null) {
                            String str = MediaBrowserServiceCompat.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("search for callback that isn't registered query=");
                            sb.append(str2);
                            Log.w(str, sb.toString());
                            return;
                        }
                        MediaBrowserServiceCompat.this.performSearch(str2, bundle2, connectionRecord, resultReceiver2);
                    }
                };
                serviceHandler.postOrRun(r1);
            }
        }

        public void sendCustomAction(String str, Bundle bundle, ResultReceiver resultReceiver, ServiceCallbacks serviceCallbacks) {
            if (!TextUtils.isEmpty(str) && resultReceiver != null) {
                ServiceHandler serviceHandler = MediaBrowserServiceCompat.this.mHandler;
                final ServiceCallbacks serviceCallbacks2 = serviceCallbacks;
                final String str2 = str;
                final Bundle bundle2 = bundle;
                final ResultReceiver resultReceiver2 = resultReceiver;
                C02689 r1 = new Runnable() {
                    public void run() {
                        ConnectionRecord connectionRecord = (ConnectionRecord) MediaBrowserServiceCompat.this.mConnections.get(serviceCallbacks2.asBinder());
                        if (connectionRecord == null) {
                            String str = MediaBrowserServiceCompat.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append("sendCustomAction for callback that isn't registered action=");
                            sb.append(str2);
                            sb.append(", extras=");
                            sb.append(bundle2);
                            Log.w(str, sb.toString());
                            return;
                        }
                        MediaBrowserServiceCompat.this.performCustomAction(str2, bundle2, connectionRecord, resultReceiver2);
                    }
                };
                serviceHandler.postOrRun(r1);
            }
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ServiceCallbacks */
    private interface ServiceCallbacks {
        IBinder asBinder();

        void onConnect(String str, Token token, Bundle bundle) throws RemoteException;

        void onConnectFailed() throws RemoteException;

        void onLoadChildren(String str, List<MediaItem> list, Bundle bundle) throws RemoteException;
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ServiceCallbacksCompat */
    private static class ServiceCallbacksCompat implements ServiceCallbacks {
        final Messenger mCallbacks;

        ServiceCallbacksCompat(Messenger messenger) {
            this.mCallbacks = messenger;
        }

        public IBinder asBinder() {
            return this.mCallbacks.getBinder();
        }

        public void onConnect(String str, Token token, Bundle bundle) throws RemoteException {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putInt(MediaBrowserProtocol.EXTRA_SERVICE_VERSION, 2);
            Bundle bundle2 = new Bundle();
            bundle2.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, str);
            bundle2.putParcelable(MediaBrowserProtocol.DATA_MEDIA_SESSION_TOKEN, token);
            bundle2.putBundle(MediaBrowserProtocol.DATA_ROOT_HINTS, bundle);
            sendRequest(1, bundle2);
        }

        public void onConnectFailed() throws RemoteException {
            sendRequest(2, null);
        }

        public void onLoadChildren(String str, List<MediaItem> list, Bundle bundle) throws RemoteException {
            Bundle bundle2 = new Bundle();
            bundle2.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, str);
            bundle2.putBundle(MediaBrowserProtocol.DATA_OPTIONS, bundle);
            if (list != null) {
                bundle2.putParcelableArrayList(MediaBrowserProtocol.DATA_MEDIA_ITEM_LIST, list instanceof ArrayList ? (ArrayList) list : new ArrayList(list));
            }
            sendRequest(3, bundle2);
        }

        private void sendRequest(int i, Bundle bundle) throws RemoteException {
            Message obtain = Message.obtain();
            obtain.what = i;
            obtain.arg1 = 2;
            obtain.setData(bundle);
            this.mCallbacks.send(obtain);
        }
    }

    /* renamed from: android.support.v4.media.MediaBrowserServiceCompat$ServiceHandler */
    private final class ServiceHandler extends Handler {
        private final ServiceBinderImpl mServiceBinderImpl = new ServiceBinderImpl();

        ServiceHandler() {
        }

        public void handleMessage(Message message) {
            Bundle data = message.getData();
            switch (message.what) {
                case 1:
                    this.mServiceBinderImpl.connect(data.getString(MediaBrowserProtocol.DATA_PACKAGE_NAME), data.getInt(MediaBrowserProtocol.DATA_CALLING_UID), data.getBundle(MediaBrowserProtocol.DATA_ROOT_HINTS), new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 2:
                    this.mServiceBinderImpl.disconnect(new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 3:
                    this.mServiceBinderImpl.addSubscription(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), BundleCompat.getBinder(data, MediaBrowserProtocol.DATA_CALLBACK_TOKEN), data.getBundle(MediaBrowserProtocol.DATA_OPTIONS), new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 4:
                    this.mServiceBinderImpl.removeSubscription(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), BundleCompat.getBinder(data, MediaBrowserProtocol.DATA_CALLBACK_TOKEN), new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 5:
                    this.mServiceBinderImpl.getMediaItem(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 6:
                    this.mServiceBinderImpl.registerCallbacks(new ServiceCallbacksCompat(message.replyTo), data.getBundle(MediaBrowserProtocol.DATA_ROOT_HINTS));
                    return;
                case 7:
                    this.mServiceBinderImpl.unregisterCallbacks(new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 8:
                    this.mServiceBinderImpl.search(data.getString(MediaBrowserProtocol.DATA_SEARCH_QUERY), data.getBundle(MediaBrowserProtocol.DATA_SEARCH_EXTRAS), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(message.replyTo));
                    return;
                case 9:
                    this.mServiceBinderImpl.sendCustomAction(data.getString(MediaBrowserProtocol.DATA_CUSTOM_ACTION), data.getBundle(MediaBrowserProtocol.DATA_CUSTOM_ACTION_EXTRAS), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(message.replyTo));
                    return;
                default:
                    String str = MediaBrowserServiceCompat.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unhandled message: ");
                    sb.append(message);
                    sb.append("\n  Service version: ");
                    sb.append(2);
                    sb.append("\n  Client version: ");
                    sb.append(message.arg1);
                    Log.w(str, sb.toString());
                    return;
            }
        }

        public boolean sendMessageAtTime(Message message, long j) {
            Bundle data = message.getData();
            data.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            data.putInt(MediaBrowserProtocol.DATA_CALLING_UID, Binder.getCallingUid());
            return super.sendMessageAtTime(message, j);
        }

        public void postOrRun(Runnable runnable) {
            if (Thread.currentThread() == getLooper().getThread()) {
                runnable.run();
            } else {
                post(runnable);
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    @Nullable
    public abstract BrowserRoot onGetRoot(@NonNull String str, int i, @Nullable Bundle bundle);

    public abstract void onLoadChildren(@NonNull String str, @NonNull Result<List<MediaItem>> result);

    public void onCreate() {
        super.onCreate();
        if (VERSION.SDK_INT >= 26) {
            this.mImpl = new MediaBrowserServiceImplApi26();
        } else if (VERSION.SDK_INT >= 23) {
            this.mImpl = new MediaBrowserServiceImplApi23();
        } else if (VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaBrowserServiceImplApi21();
        } else {
            this.mImpl = new MediaBrowserServiceImplBase();
        }
        this.mImpl.onCreate();
    }

    public IBinder onBind(Intent intent) {
        return this.mImpl.onBind(intent);
    }

    public void onLoadChildren(@NonNull String str, @NonNull Result<List<MediaItem>> result, @NonNull Bundle bundle) {
        result.setFlags(1);
        onLoadChildren(str, result);
    }

    public void onLoadItem(String str, @NonNull Result<MediaItem> result) {
        result.setFlags(2);
        result.sendResult(null);
    }

    public void onSearch(@NonNull String str, Bundle bundle, @NonNull Result<List<MediaItem>> result) {
        result.setFlags(4);
        result.sendResult(null);
    }

    public void onCustomAction(@NonNull String str, Bundle bundle, @NonNull Result<Bundle> result) {
        result.sendError(null);
    }

    public void setSessionToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        } else if (this.mSession == null) {
            this.mSession = token;
            this.mImpl.setSessionToken(token);
        } else {
            throw new IllegalStateException("The session token has already been set.");
        }
    }

    @Nullable
    public Token getSessionToken() {
        return this.mSession;
    }

    public final Bundle getBrowserRootHints() {
        return this.mImpl.getBrowserRootHints();
    }

    public void notifyChildrenChanged(@NonNull String str) {
        if (str != null) {
            this.mImpl.notifyChildrenChanged(str, null);
            return;
        }
        throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
    }

    public void notifyChildrenChanged(@NonNull String str, @NonNull Bundle bundle) {
        if (str == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        } else if (bundle != null) {
            this.mImpl.notifyChildrenChanged(str, bundle);
        } else {
            throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isValidPackage(String str, int i) {
        if (str == null) {
            return false;
        }
        for (String equals : getPackageManager().getPackagesForUid(i)) {
            if (equals.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public void addSubscription(String str, ConnectionRecord connectionRecord, IBinder iBinder, Bundle bundle) {
        List<Pair> list = (List) connectionRecord.subscriptions.get(str);
        if (list == null) {
            list = new ArrayList<>();
        }
        for (Pair pair : list) {
            if (iBinder == pair.first && MediaBrowserCompatUtils.areSameOptions(bundle, (Bundle) pair.second)) {
                return;
            }
        }
        list.add(new Pair(iBinder, bundle));
        connectionRecord.subscriptions.put(str, list);
        performLoadChildren(str, connectionRecord, bundle);
    }

    /* access modifiers changed from: 0000 */
    public boolean removeSubscription(String str, ConnectionRecord connectionRecord, IBinder iBinder) {
        boolean z = true;
        boolean z2 = false;
        if (iBinder == null) {
            if (connectionRecord.subscriptions.remove(str) == null) {
                z = false;
            }
            return z;
        }
        List list = (List) connectionRecord.subscriptions.get(str);
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (iBinder == ((Pair) it.next()).first) {
                    it.remove();
                    z2 = true;
                }
            }
            if (list.size() == 0) {
                connectionRecord.subscriptions.remove(str);
            }
        }
        return z2;
    }

    /* access modifiers changed from: 0000 */
    public void performLoadChildren(String str, ConnectionRecord connectionRecord, Bundle bundle) {
        final ConnectionRecord connectionRecord2 = connectionRecord;
        final String str2 = str;
        final Bundle bundle2 = bundle;
        C02481 r0 = new Result<List<MediaItem>>(str) {
            /* access modifiers changed from: 0000 */
            public void onResultSent(List<MediaItem> list) {
                if (MediaBrowserServiceCompat.this.mConnections.get(connectionRecord2.callbacks.asBinder()) != connectionRecord2) {
                    if (MediaBrowserServiceCompat.DEBUG) {
                        String str = MediaBrowserServiceCompat.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Not sending onLoadChildren result for connection that has been disconnected. pkg=");
                        sb.append(connectionRecord2.pkg);
                        sb.append(" id=");
                        sb.append(str2);
                        Log.d(str, sb.toString());
                    }
                    return;
                }
                if ((getFlags() & 1) != 0) {
                    list = MediaBrowserServiceCompat.this.applyOptions(list, bundle2);
                }
                try {
                    connectionRecord2.callbacks.onLoadChildren(str2, list, bundle2);
                } catch (RemoteException unused) {
                    String str2 = MediaBrowserServiceCompat.TAG;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Calling onLoadChildren() failed for id=");
                    sb2.append(str2);
                    sb2.append(" package=");
                    sb2.append(connectionRecord2.pkg);
                    Log.w(str2, sb2.toString());
                }
            }
        };
        this.mCurConnection = connectionRecord;
        if (bundle == null) {
            onLoadChildren(str, r0);
        } else {
            onLoadChildren(str, r0, bundle);
        }
        this.mCurConnection = null;
        if (!r0.isDone()) {
            StringBuilder sb = new StringBuilder();
            sb.append("onLoadChildren must call detach() or sendResult() before returning for package=");
            sb.append(connectionRecord.pkg);
            sb.append(" id=");
            sb.append(str);
            throw new IllegalStateException(sb.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public List<MediaItem> applyOptions(List<MediaItem> list, Bundle bundle) {
        if (list == null) {
            return null;
        }
        int i = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE, -1);
        int i2 = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, -1);
        if (i == -1 && i2 == -1) {
            return list;
        }
        int i3 = i2 * i;
        int i4 = i3 + i2;
        if (i < 0 || i2 < 1 || i3 >= list.size()) {
            return Collections.EMPTY_LIST;
        }
        if (i4 > list.size()) {
            i4 = list.size();
        }
        return list.subList(i3, i4);
    }

    /* access modifiers changed from: 0000 */
    public void performLoadItem(String str, ConnectionRecord connectionRecord, final ResultReceiver resultReceiver) {
        C02492 r0 = new Result<MediaItem>(str) {
            /* access modifiers changed from: 0000 */
            public void onResultSent(MediaItem mediaItem) {
                if ((getFlags() & 2) != 0) {
                    resultReceiver.send(-1, null);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(MediaBrowserServiceCompat.KEY_MEDIA_ITEM, mediaItem);
                resultReceiver.send(0, bundle);
            }
        };
        this.mCurConnection = connectionRecord;
        onLoadItem(str, r0);
        this.mCurConnection = null;
        if (!r0.isDone()) {
            StringBuilder sb = new StringBuilder();
            sb.append("onLoadItem must call detach() or sendResult() before returning for id=");
            sb.append(str);
            throw new IllegalStateException(sb.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public void performSearch(String str, Bundle bundle, ConnectionRecord connectionRecord, final ResultReceiver resultReceiver) {
        C02503 r0 = new Result<List<MediaItem>>(str) {
            /* access modifiers changed from: 0000 */
            public void onResultSent(List<MediaItem> list) {
                if ((getFlags() & 4) != 0 || list == null) {
                    resultReceiver.send(-1, null);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArray(MediaBrowserServiceCompat.KEY_SEARCH_RESULTS, (Parcelable[]) list.toArray(new MediaItem[0]));
                resultReceiver.send(0, bundle);
            }
        };
        this.mCurConnection = connectionRecord;
        onSearch(str, bundle, r0);
        this.mCurConnection = null;
        if (!r0.isDone()) {
            StringBuilder sb = new StringBuilder();
            sb.append("onSearch must call detach() or sendResult() before returning for query=");
            sb.append(str);
            throw new IllegalStateException(sb.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public void performCustomAction(String str, Bundle bundle, ConnectionRecord connectionRecord, final ResultReceiver resultReceiver) {
        C02514 r0 = new Result<Bundle>(str) {
            /* access modifiers changed from: 0000 */
            public void onResultSent(Bundle bundle) {
                resultReceiver.send(0, bundle);
            }

            /* access modifiers changed from: 0000 */
            public void onProgressUpdateSent(Bundle bundle) {
                resultReceiver.send(1, bundle);
            }

            /* access modifiers changed from: 0000 */
            public void onErrorSent(Bundle bundle) {
                resultReceiver.send(-1, bundle);
            }
        };
        this.mCurConnection = connectionRecord;
        onCustomAction(str, bundle, r0);
        this.mCurConnection = null;
        if (!r0.isDone()) {
            StringBuilder sb = new StringBuilder();
            sb.append("onCustomAction must call detach() or sendResult() or sendError() before returning for action=");
            sb.append(str);
            sb.append(" extras=");
            sb.append(bundle);
            throw new IllegalStateException(sb.toString());
        }
    }
}
