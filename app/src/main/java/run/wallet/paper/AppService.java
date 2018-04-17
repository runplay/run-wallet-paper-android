package run.wallet.paper;

import android.app.Activity;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jota.error.ArgumentException;
import jota.pow.ICurl;
import jota.pow.SpongeFactory;
import jota.utils.Checksum;
import jota.utils.Converter;
import jota.utils.Signing;
import run.wallet.common.delete.SecureDeleteFile;


public final class AppService extends Service {
	
	private static AppService SERVICE;//=new AppService();
    private Activity activity;
    private final IBinder mBinder = new LocalBinder();
    private static boolean isAppStarted=false;
    private static ICurl customCurl = SpongeFactory.create(SpongeFactory.Mode.KERL);

    public static void setIsAppStarted(boolean started) {
        isAppStarted=started;
    }

    public static boolean isAppStarted() {
        return isAppStarted;
    }


    @Override
    public void onCreate() {
    	super.onCreate();
    }


    public static void ensureStartups(Context context) {

    }
    public class LocalBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
    	if(SERVICE==null) {
    		SERVICE=this;

    	}
        //ensureForegroundService();
    	return START_STICKY;
    }
    private static final int FOREGROUND_ID=17675549;

    protected static boolean isForegroundServiceRunning() {
        return foreground;
    }
    private static boolean foreground=false;
    private void ensureForegroundService() {
        Intent intent = new Intent(SERVICE, Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("TICKER").setContentTitle(getString(R.string.app_short)+" "+getString(R.string.gen_addresses)).setContentText("")
                .setSmallIcon(R.drawable.notify)
                .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                .setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendIntent);
        Notification notification = builder.build();
        foreground=true;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notification);

    }
    private void stopForegroundService() {
        stopForeground(true);
        foreground=false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	
    public static boolean isAppServiceRunning(Context context) {
        if(context!=null) {
            android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AppService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkStartAppService(Context context) {
        if(context!=null && !isAppServiceRunning(context)) {
            Intent service = new Intent(context, AppService.class);
            startService(service);
        }
    }

    public static void deleteFile(Activity activity, File file) {
        GoDeleteTask gdt=new GoDeleteTask(activity,file);
        gdt.execute(true);
    }

    private static class GoDeleteTask extends AsyncTask<Boolean,Void,Boolean> {
        private Activity activity;
        private File deleteMe;
        public GoDeleteTask(Activity activity,File file) {
            deleteMe=file;
            this.activity=activity;
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {

            SecureDeleteFile.delete(deleteMe);

            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(isAppStarted) {

                Snackbar.make(activity.findViewById(R.id.container),activity.getString(R.string.deleted)+deleteMe.getName(),Snackbar.LENGTH_SHORT).show();
                Main.refreshFiles();
            }
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(deleteMe));
            activity.sendBroadcast(intent);
        }

    }

    private static double pctCompleted=0;
    private static GoAddressTask gat;
    public static void generateAddressTask(String name,String datefile, String password, String seed, int indexStart, int total) {
        SERVICE.ensureForegroundService();
        gat = new GoAddressTask();
        gat.startIndex=indexStart;
        gat.password=password;
        gat.name=name;
        gat.total=total;
        gat.seed=seed;
        gat.datefile=datefile;
        gat.execute(true);
    }
    private static class GoAddressTask extends AsyncTask<Boolean,Void,Boolean> {

        protected int startIndex;
        protected int total;
        protected String seed;
        protected String name;
        protected String password;
        protected String datefile;
        protected List<IotaAddress> addresses=new ArrayList<>();

        @Override
        protected Boolean doInBackground(Boolean... booleans) {

            try {
                GetNewAddressResponse addresp = getNewAddress(seed, 2, startIndex, false, total);
                String file=Main.makeAddressFileName(datefile,startIndex,total);
                seed=null;
                int pctcount=0;
                JSONArray jar=new JSONArray();
                for(String add: addresp.getAddresses()) {
                    try {
                        IotaAddress ia = new IotaAddress();
                        ia.address = Checksum.addChecksum(add);
                        ia.index = startIndex++;
                        pctcount++;
                        pctCompleted = (100 / total) * pctcount;
                        jar.put(ia.toJson());
                    } catch(Exception e) {
                        // this would only happen if address fails the checksum add, so should never
                    }
                }
                JSONObject data = new JSONObject();
                data.put("add",jar);

                Main.Paper.EncPack pack=Main.Paper.encryptJson(data,password);

                String htmlContent = Main.getResourceFileContent("addresses.html");
                htmlContent=htmlContent.replace("$DATA",pack.encrypted)
                        .replace("$IV",pack.ivHex)
                        .replace("$SALT",pack.salt)
                        .replace("$NAME",name)
                        .replace("$ENC","true");
                name=name.replaceAll(" ","");

                Main.writeToFile(file,htmlContent);

                return true;
            } catch(Exception e) {

            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.stopForegroundService();
            vibrate(SERVICE);
            if(result) {
                Toast.makeText(SERVICE,R.string.addresses_finish,Toast.LENGTH_LONG);
            } else {
                Toast.makeText(SERVICE,R.string.task_failed,Toast.LENGTH_LONG);
            }
        }

    }
    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(640);
    }

    private static class IotaAddress {
        protected int index;
        protected String address;
        protected JSONObject toJson() {
            JSONObject j=new JSONObject();
            try {
                j.put("add", address);
                j.put("ind", index);
            } catch(Exception e){}
            return  j;
        }
    }

    public static String newAddress(String seed, int security, int index, boolean checksum, ICurl curl) throws ArgumentException {
        Signing signing = new Signing(curl);
        final int[] key = signing.key(Converter.trits(seed), index, security);
        final int[] digests = signing.digests(key);
        final int[] addressTrits = signing.address(digests);
        String address = Converter.trytes(addressTrits);
        if (checksum) {
            address = Checksum.addChecksum(address);
        }
        return address;
    }
    public static GetNewAddressResponse getNewAddress(final String seed, int security, final int index, final boolean checksum, final int total) throws ArgumentException {
        StopWatch stopWatch = new StopWatch();
        List<String> allAddresses = new ArrayList<>();
        if (total != 0) {
            for (int i = index; i < index + total; i++) {
                allAddresses.add(newAddress(seed, security, i, checksum, customCurl.clone()));
            }

        }
        return GetNewAddressResponse.create(allAddresses, stopWatch.getTime());
    }

    public static class GetNewAddressResponse extends AbstractResponse {

        private List<String> addresses;
        public static GetNewAddressResponse create(List<String> addresses, long duration) {
            GetNewAddressResponse res = new GetNewAddressResponse();
            res.addresses = addresses;
            res.setDuration(duration);
            return res;
        }
        public List<String> getAddresses() {
            return addresses;
        }
    }
    public static abstract class AbstractResponse {

        private Long duration;
        public Long getDuration() {
            return duration;
        }
        public void setDuration(Long duration) {
            this.duration = duration;
        }
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }
        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
    }
}
