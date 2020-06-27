package mirror.android.content;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.os.Bundle;
import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefLong;
import mirror.RefObject;

@TargetApi(19)
public class SyncRequest {
    public static Class<?> TYPE = RefClass.load(SyncRequest.class, android.content.SyncRequest.class);
    public static RefObject<Account> mAccountToSync;
    public static RefObject<String> mAuthority;
    public static RefObject<Bundle> mExtras;
    public static RefBoolean mIsPeriodic;
    public static RefLong mSyncRunTimeSecs;
}
