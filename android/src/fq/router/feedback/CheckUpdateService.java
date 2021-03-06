package fq.router.feedback;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import fq.router.life.LaunchService;
import fq.router.utils.DnsUtils;
import fq.router.utils.HttpUtils;
import fq.router.utils.LogUtils;
import fq.router.utils.ShellUtils;

import java.util.HashMap;

public class CheckUpdateService extends IntentService {

    public CheckUpdateService() {
        super("CheckUpdate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        checkUpdate();
    }


    public void checkUpdate() {
        try {
            appendLog("checking update...");
            String versionInfo = DnsUtils.resolveTXT("beta.android.ver.fqrouter.com");
            String latestVersion = versionInfo.split("\\|")[0];
            String upgradeUrl = versionInfo.split("\\|")[1];
            if (isNewer(latestVersion, LaunchService.getMyVersion(this))) {
                appendLog("latest version is: " + latestVersion);
                sendBroadcast(new UpdateFoundIntent(latestVersion, upgradeUrl));
            } else {
                appendLog("already running the latest version");
            }
        } catch (Exception e) {
            appendLog("check updates failed");
            LogUtils.e("check updates failed", e);
        }
    }

    private void appendLog(String log) {
        sendBroadcast(new AppendLogIntent(log));
    }


    private static boolean isNewer(String latestVersion, String currentVersion) {
        int[] latestVersionParts = parseVersion(latestVersion);
        int[] currentVersionParts = parseVersion(currentVersion);
        if (latestVersionParts[0] > currentVersionParts[0]) {
            return true;
        }
        if (latestVersionParts[0] < currentVersionParts[0]) {
            return false;
        }
        if (latestVersionParts[1] > currentVersionParts[1]) {
            return true;
        }
        if (latestVersionParts[1] < currentVersionParts[1]) {
            return false;
        }
        return latestVersionParts[2] > currentVersionParts[2];
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        };
    }

    public static void execute(Context context) {
        context.startService(new Intent(context, CheckUpdateService.class));
    }
}
