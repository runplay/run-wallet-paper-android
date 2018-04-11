package run.wallet.paper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import jota.utils.SeedRandomGenerator;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class Main extends AppCompatActivity {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    private static final int SEED_LENGTH_MIN = 41;
    private static final int SEED_LENGTH_MAX = 81;
    private static LinearLayout container;
    private static Main activity;
    private static final int SCR_ONE=0;
    private static final int SCR_TWO=1;
    private static final int SCR_THREE=2;
    private static final int SCR_DEL=3;
    private static final int SCR_QR=4;

    private GoDigitalDialog dialog =null;

    private static TextInputEditText seedEditText;
    private LinearLayout genPod;
    private LinearLayout lockedPod;
    private LinearLayout numberPickerHolder;

    private ImageButton scrollLeft;
    private ImageButton scrollRight;

    private TextView btnScreenOne;
    private TextView btnScreenTwo;
    private TextView btnScreenThree;
    private TextView btnScreenDel;
    private static EditText seedName;

    private static CheckBox checkDigital;
    private static CheckBox checkPrint;
    private ListView delList;

    private Button btnPaper;
    private Button btnPrint;
    private Button btnPaperText;
    private Button btnCopy;

    private static Spinner spStart;
    private static Spinner spStop;
    private TextView finalSeedAddress;
    private TextView getFinalSeedPaper;

    private HorizontalScrollView scrollView;
    private AppCompatButton btnNext;
    private Button btnPaste;
    private AppCompatButton btnCreate;

    private Button btnUnlock;
    private Button btnStart;
    private Button btnQr;
    private Button btnClear;

    private TextView noFiles;

    private View addGenPod;
    private View addGenSelect;
    private View addGenNow;

    private String cameraSeed;


    private static boolean isLocked=false;

    private static int currentScreen=SCR_ONE;;

    private static final DecimalFormat df = new DecimalFormat( "#00" );

    protected static final String hdd=Environment.getExternalStorageDirectory().getAbsolutePath()+"/runIotaPaper/";
    private static final int REQUEST_WRITE_STORAGE = 9111;
    private static final Integer[] itemsStart = new Integer[]{0,10,20,30,40,50,100,150,200,250,300,350,400,450,500,600,700,800,900,1000};
    private static final Integer[] itemsTotal = new Integer[]{10,20,30,40,50,100};


    private final String pickerValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
    private final String[] pickerArray= pickerValues.split("(?!^)");

    private ScrollView screenOne;
    private ScrollView screenTwo;
    private ScrollView screenThree;
    private LinearLayout screenDel;
    private RelativeLayout screenQr;


    private Animation fadeIn;
    private Animation fadeOut;

    private SharedPreferences sp;
    private boolean warnFiles=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.main);
        activity=this;
        container=findViewById(R.id.container);
        sp=activity.getSharedPreferences("iotapaper",MODE_PRIVATE);
        warnFiles=sp.getBoolean("files",true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!AppService.isAppServiceRunning(this)) {
            Intent service = new Intent(this, AppService.class);
            this.startService(service);
        }


        seedEditText=this.findViewById(R.id.seed_first);
        // disable any ability to select text for copy
        seedEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        genPod=findViewById(R.id.seed_unlocked);
        lockedPod=findViewById(R.id.seed_locked);

        numberPickerHolder=findViewById(R.id.seed_gen_holder);
        btnCopy=findViewById(R.id.btn_copy);
        scrollLeft=findViewById(R.id.seed_add_scroll_left);
        scrollRight=findViewById(R.id.seed_add_scroll_right);
        scrollView=findViewById(R.id.seed_add_scroll_view);
        btnClear=findViewById(R.id.btn_clear);
        screenOne=findViewById(R.id.screen_one);
        screenTwo=findViewById(R.id.screen_two);
        screenThree=findViewById(R.id.screen_three);
        screenDel=findViewById(R.id.screen_del);
        screenQr=findViewById(R.id.qr_screen);

        spStart=findViewById(R.id.ain_start);
        spStop=findViewById(R.id.ain_end);
        seedName=findViewById(R.id.friendly_name);

        checkDigital=findViewById(R.id.check_digital);
        checkPrint=findViewById(R.id.check_print);
        btnQr=findViewById(R.id.btn_qr);

        addGenNow=findViewById(R.id.address_gen_now);
        addGenPod=findViewById(R.id.address_gen_pod);
        addGenSelect=findViewById(R.id.address_gen_select);
        btnScreenOne=this.findViewById(R.id.btn_screen_one);
        btnScreenTwo=this.findViewById(R.id.btn_screen_two);
        btnScreenThree=this.findViewById(R.id.btn_screen_three);
        btnScreenDel=this.findViewById(R.id.btn_screen_del);
        btnNext=this.findViewById(R.id.btn_next);
        btnPaste=this.findViewById(R.id.btn_paste);
        btnCreate=this.findViewById(R.id.seed_add_generate_seed);
        btnStart=this.findViewById(R.id.btn_gen);
        btnUnlock=this.findViewById(R.id.btn_unlock);
        btnPaper=findViewById(R.id.btn_paper);
        noFiles=findViewById(R.id.no_files);
        btnPrint= findViewById(R.id.btn_print);
        btnPaperText=findViewById(R.id.btn_save);
        //checkEncAddresses=findViewById(R.id.check_enc_addresses);

        finalSeedAddress=findViewById(R.id.seed_final_addresses);
        getFinalSeedPaper=findViewById(R.id.seed_final_save);
        delList = findViewById(R.id.del_list);

        btnScreenOne.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {goScreen(SCR_ONE);}});
        btnScreenTwo.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {goScreen(SCR_TWO);}});
        btnScreenThree.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {goScreen(SCR_THREE);}});
        btnScreenDel.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {goScreen(SCR_DEL);}});

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(hasFilePermission(activity)) {
                addGenNow.setVisibility(View.VISIBLE);
                addGenPod.setVisibility(View.GONE);
                addGenSelect.setVisibility(View.GONE);
                dialog = new GoDigitalDialog();
                dialog.isSeed = false;
                dialog.show(activity.getFragmentManager(), null);
            } else {
                currentScreen=SCR_THREE;
                requestFileWritePermission(activity);
            }

            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraSeed="";
                isLocked=false;
                seedEditText.setText("");
                goScreen(SCR_ONE);
            }
        });
        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasCameraPermission(activity)) {
                    goScreen(SCR_QR);
                } else {
                    requestPermissionCamera();
                }
            }
        });
        spStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateSpStop();
                checkAddGo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        spStop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                checkAddGo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                try {
                    String name=seedName.getText().toString();
                    if(name==null || name.isEmpty()) {
                        name=getFileDateYYYYMMDDHHMMSS();
                    }
                    data.put("seed", seedEditText.getText().toString());
                    data.put("name", name);

                    Paper.generateImage(data, activity, checkPrint.isChecked(), new BmpCallback() {
                        @Override
                        public void onFinishBmp(Bitmap bitmap) {
                            printImage(bitmap);
                        }
                    });
                } catch (Exception e) {}
            }
        });
        btnPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(hasFilePermission(activity)) {
                makeDigitalPaper();
            } else {
                currentScreen=SCR_TWO;
                requestFileWritePermission(activity);
            }
            }
        });
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(AppService.isForegroundServiceRunning()) {
                Snackbar.make(container,R.string.wait_now,Snackbar.LENGTH_SHORT).show();
            } else if(seedEditText.getText().toString().equals(cameraSeed)) {
                Snackbar.make(container,R.string.qr_no_edit,Snackbar.LENGTH_SHORT).show();
            } else {
                isLocked=false;
                goScreen(SCR_ONE);
            }

            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            seedEditText.setText(SeedRandomGenerator.generateNewSeed());
            genPod.setVisibility(View.VISIBLE);
            drawCustomise();
            }
        });
        scrollLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int scrollby =scrollView.getScrollX()-300;
                scrollby=scrollby<0?0:scrollby;
                scrollView.setScrollX(scrollby);
            }
        });
        scrollRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.setScrollX(scrollView.getScrollX()+300);
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CopySeed();
            }
        });
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd=clipboard.getPrimaryClip();
                if (cd != null && cd.getItemCount() > 0) {
                    seedEditText.setText(cd.getItemAt(0).coerceToText(activity));
                }
            }
        });
        seedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isSeedValid(activity,s.toString())==null) {
                    btnCopy.setAlpha(1f);
                    btnCopy.setEnabled(true);
                } else {
                    btnCopy.setAlpha(0.3f);
                    btnCopy.setEnabled(false);
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goScreen(SCR_TWO);
            }
        });
        drawCustomise();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(dialog!=null) {
            dialog.dismiss();
        }
        goScreen(currentScreen);
        AppService.setIsAppStarted(true);
    }
    @Override
    public void onPause() {
        super.onPause();
        wipeClipboard();

        AppService.setIsAppStarted(false);

    }
    @Override
    public void onDestroy() {
        wipeClipboard();
        seedEditText=null;
        seedName=null;
        super.onDestroy();
    }
    private void wipeClipboard() {
        // wipe any clipboard attempts, wrapper app protection
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("",""));
    }
    private void goScreen(final int SCR_) {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut=AnimationUtils.loadAnimation(this, R.anim.fade_out);
        if(screenOne.getVisibility()==View.VISIBLE)
            screenOne.setAnimation(fadeOut);
        if(screenTwo.getVisibility()==View.VISIBLE)
            screenTwo.setAnimation(fadeOut);
        if(screenThree.getVisibility()==View.VISIBLE)
            screenThree.setAnimation(fadeOut);
        if(screenDel.getVisibility()==View.VISIBLE)
            screenDel.setAnimation(fadeOut);

        screenOne.setVisibility(View.GONE);
        screenTwo.setVisibility(View.GONE);
        screenThree.setVisibility(View.GONE);
        screenDel.setVisibility(View.GONE);
        screenQr.setVisibility(View.GONE);

        String seedText=seedEditText.getText().toString();
        switch (SCR_) {
            case SCR_ONE:
                screenOne.setVisibility(View.VISIBLE);screenOne.setAnimation(fadeIn);
                renderScreenDetails(SCR_);
                break;
            case SCR_TWO:
                if(isSeedValid(this,seedText)==null) {
                    renderScreenDetails(SCR_);
                    screenTwo.setVisibility(View.VISIBLE);
                    screenTwo.setAnimation(fadeIn);

                } else {
                    Snackbar.make(findViewById(R.id.container),getString(R.string.create_first),Snackbar.LENGTH_SHORT).show();
                    goScreen(SCR_ONE);
                }
                break;
            case SCR_THREE:
                if(isSeedValid(this,seedText)==null) {
                    renderScreenDetails(SCR_);
                    screenThree.setVisibility(View.VISIBLE);
                    screenThree.setAnimation(fadeIn);

                } else {
                    Snackbar.make(findViewById(R.id.container),getString(R.string.create_first),Snackbar.LENGTH_SHORT).show();
                    goScreen(SCR_ONE);
                }
                break;
            case SCR_DEL:
                screenDel.setVisibility(View.VISIBLE);
                screenDel.setAnimation(fadeIn);
                renderScreenDetails(SCR_);
                break;
            case SCR_QR:
                screenQr.setVisibility(View.VISIBLE);
                openQrScanner(activity);
                break;
        }
        currentScreen=SCR_;
    }

    private void renderScreenDetails(int SCR_) {
        String seedText=seedEditText.getText().toString();
        switch(SCR_) {
            case SCR_ONE:
                if(AppService.isForegroundServiceRunning()
                        || isLocked
                        || (!seedText.isEmpty() && seedText.equals(cameraSeed))
                        ) {
                    genPod.setVisibility(View.GONE);
                    btnCreate.setVisibility(View.GONE);
                    lockedPod.setVisibility(View.VISIBLE);
                    btnPaste.setVisibility(View.GONE);
                    btnQr.setVisibility(View.GONE);

                } else {
                    if(!seedText.isEmpty()) {
                        genPod.setVisibility(View.VISIBLE);
                    }
                    btnCreate.setVisibility(View.VISIBLE);
                    lockedPod.setVisibility(View.GONE);
                    btnPaste.setVisibility(View.VISIBLE);
                    btnQr.setVisibility(View.VISIBLE);
                }

                break;
            case SCR_TWO:
                makeSeedShort();
                getFinalSeedPaper.setText(makeSeedShort());
                isLocked=true;
                break;
            case SCR_THREE:
                if(AppService.isForegroundServiceRunning()) {
                    addGenNow.setVisibility(View.VISIBLE);
                    addGenPod.setVisibility(View.GONE);
                    addGenSelect.setVisibility(View.GONE);
                } else {
                    addGenNow.setVisibility(View.GONE);
                    addGenPod.setVisibility(View.VISIBLE);
                    addGenSelect.setVisibility(View.VISIBLE);
                    makeSeedShort();
                    finalSeedAddress.setText(makeSeedShort());
                    drawAddresses();
                }
                isLocked = true;
                break;
            case SCR_DEL:
                drawFileList();
                break;

        }
    }
    private String makeSeedShort() {
        String seed=seedEditText.getText().toString();
        if(isSeedValid(activity,seed)==null) {
            return seed.substring(0,4)+"......"+seed.substring(seed.length()-4,seed.length());
        }
        // if this happens, ui bug
        return "Invalid Seed";

    }
    private void checkAddGo() {
        int st=(Integer)spStart.getSelectedItem();
        int en=(Integer)spStop.getSelectedItem();
        if(st<en) {
            btnStart.setEnabled(true);
        } else {
            btnStart.setEnabled(false);
        }
    }

    private void drawAddresses() {

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_dropdown_item, itemsStart);

        spStart.setAdapter(adapter);
    }
    private void updateSpStop() {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_dropdown_item, itemsTotal);
        spStop.setAdapter(adapter);

    }
    /*
    File methods
     */

    protected static void refreshFiles() {
        if(activity!=null) {
            FaList fa=new FaList();
            if(fa.getCount()>0)
                activity.delList.setAdapter(fa);
            else
                activity.delList.setAdapter(null);
        }
    }
    private static final List<File> getFiles() {

        File dir = new File(hdd);
        File[] files=dir.listFiles();
        List<File> show=new ArrayList<>();
        for(File f: files) {
            if(!f.getName().equals(".nomedia"))
                show.add(f);
        }
        return show;

    }
    private void drawFileList() {
        if(hasFilePermission(activity) && !getFiles().isEmpty()) {
            FaList fa = new FaList();
            delList.setAdapter(fa);
            noFiles.setVisibility(View.GONE);
            delList.setVisibility(View.VISIBLE);
        } else {
            noFiles.setVisibility(View.VISIBLE);
            delList.setVisibility(View.GONE);
        }
    }

    private static class FaList implements ListAdapter {
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        List<File> files=getFiles();
        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int i) {
            return files.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view ==null) {
                view=activity.getLayoutInflater().inflate(R.layout.file,null);
            }

            File fn = files.get(i);

            TextView tv=view.findViewById(R.id.file_text);
            tv.setText(fn.getName());
            ImageButton del = view.findViewById(R.id.btn_file);
            del.setTag(fn.getAbsolutePath());
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(hasFilePermission(activity)) {
                        String fn = (String) view.getTag();
                        if (fn != null) {
                            Log.e("DELF", fn);
                            AppService.deleteFile(activity, new File(fn));
                        }
                    } else {
                        currentScreen=SCR_THREE;
                        requestFileWritePermission(activity);
                    }
                }
            });
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return i;
        }

        @Override
        public int getViewTypeCount() {
            return files.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
    /*
    Seed generate methods
     */
    private void makeDigitalPaper() {
        dialog = new GoDigitalDialog();
        dialog.show(this.getFragmentManager(), null);
    }
    private void CopySeed() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.seed), seedEditText.getText().toString());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(findViewById(R.id.container),getString(R.string.copy_snack),Snackbar.LENGTH_SHORT).show();
    }

    private void drawCustomise() {
        numberPickerHolder.removeAllViews();
        final String useSeed=seedEditText.getText().toString();
        if(useSeed!=null) {
            char[] asArray = useSeed.toCharArray();
            for (int i = 0; i < asArray.length; i++) {
                char c = asArray[i];
                NumberPicker num = new NumberPicker(this);
                num.setMinValue(0);
                num.setMaxValue(26);
                num.setDisplayedValues(pickerArray);
                num.setValue(pickerValues.indexOf(c));
                num.setClickable(false);
                num.setTag(Integer.valueOf(i));
                num.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        try {
                            String inSeed=seedEditText.getText().toString();
                            Integer index = (Integer) picker.getTag();
                            char[] asArray = inSeed.toCharArray();
                            asArray[index] = pickerArray[newVal].charAt(0);
                            seedEditText.setText(new String(asArray));
                        } catch(Exception e){}
                    }
                });
                numberPickerHolder.addView(num);
            }
        }
    }

    /*
    Encrypt
     */

    private static String bitmapToString(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private static SecretKey generateKey(String salt, String passphrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), 1000, 256);
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e("ENCRYPT",e.getMessage());
        }
        return null;
    }
    private static byte[] hex(String str) {
        try {
            return Hex.decode(str);
        }
        catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private static String generateSalt() {

        StringBuilder builder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 32; i++) {
            char c = hexArray[random.nextInt(hexArray.length)];
            builder.append(c);
        }
        return builder.toString();
    }


    /*
    IOTA methods and classes
     */
    private enum IotaUnits {
        IOTA("i", 0),
        KILO_IOTA("Ki", 3), // 10^3
        MEGA_IOTA("Mi", 6), // 10^6
        GIGA_IOTA("Gi", 9), // 10^9
        TERA_IOTA("Ti", 12), // 10^12
        PETA_IOTA("Pi", 15); // 10^15
        private String unit;
        private long value;

        IotaUnits(String unit, long value) {
            this.unit = unit;
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public long getValue() {
            return value;
        }
    }
    private static class IotaToText {
        public static long convertUnits(long amount, IotaUnits fromUnit, IotaUnits toUnit) {
            long amountInSource = (long) (amount * Math.pow(10, fromUnit.getValue()));
            return convertUnits(amountInSource, toUnit);
        }
        private static long convertUnits(long amount, IotaUnits toUnit) {
            return (long) (amount / Math.pow(10, toUnit.getValue()));
        }
        public static String convertRawIotaAmountToDisplayText(long amount, boolean extended) {
            IotaUnits unit = findOptimalIotaUnitToDisplay(amount);
            double amountInDisplayUnit = convertAmountTo(amount, unit);

            return createAmountWithUnitDisplayText(amountInDisplayUnit, unit, extended);
        }
        public static class IotaDisplayData {
            public String value="0";
            public String thirdDecimal="";
            public String unit="i";

        }
        public static IotaDisplayData getIotaDisplayData(long amount) {
            IotaDisplayData data = new IotaDisplayData();
            IotaUnits unit = findOptimalIotaUnitToDisplay(amount);
            double amountInDisplayUnit = convertAmountTo(amount, unit);

            if(unit!=IotaUnits.IOTA) {
                String result=createAmountDisplayText(amountInDisplayUnit, unit, true);
                //Log.e("RES",""+result);
                data.thirdDecimal=result.substring(result.length()-1,result.length());
            }
            data.value = createAmountDisplayText(amountInDisplayUnit, unit, false);
            data.unit = unit.getUnit();

            return data;
        }
        public static double convertAmountTo(long amount, IotaUnits target) {
            return amount / Math.pow(10, target.getValue());
        }
        private static String createAmountWithUnitDisplayText(double amountInUnit, IotaUnits unit, boolean extended) {
            String result = createAmountDisplayText(amountInUnit, unit, extended);
            result += " " + unit.getUnit();
            return result;
        }
        public static String createAmountDisplayText(double amountInUnit, IotaUnits unit, boolean extended) {
            DecimalFormat df;
            if (extended)
                df = new DecimalFormat("##0.0000");
            else
                df = new DecimalFormat("##0.000");

            String result = "";
            // display unit as integer if value is between 1-999 or in decimal format
            String converted=df.format(amountInUnit);
            result += unit == IotaUnits.IOTA ? (long) amountInUnit : converted.substring(0,converted.length()-1);
            return result;
        }
        public static IotaUnits findOptimalIotaUnitToDisplay(long amount) {
            int length = String.valueOf(amount).length();
            if (amount < 0) {// do not count "-" sign
                length -= 1;
            }
            IotaUnits units = IotaUnits.IOTA;
            if (length >= 1 && length <= 3) {
                units = IotaUnits.IOTA;
            } else if (length > 3 && length <= 6) {
                units = IotaUnits.KILO_IOTA;
            } else if (length > 6 && length <= 9) {
                units = IotaUnits.MEGA_IOTA;
            } else if (length > 9 && length <= 12) {
                units = IotaUnits.GIGA_IOTA;
            } else if (length > 12 && length <= 15) {
                units = IotaUnits.TERA_IOTA;
            } else if (length > 15 && length <= 18) {
                units = IotaUnits.PETA_IOTA;
            }
            return units;
        }

    }


    /*
    IOTA Address Generation methods
     */

    private static String isSeedValid(Context context, String seed) {
        if (!seed.matches("^[A-Z9a-z]+$")) {
            if (seed.length() > SEED_LENGTH_MAX)
                return context.getString(R.string.messages_invalid_characters_seed) + " " + context.getString(R.string.messages_seed_to_long);
            else if (seed.length() < SEED_LENGTH_MIN)
                return context.getString(R.string.messages_invalid_characters_seed) + " " + context.getString(R.string.messages_seed_to_short);
            else
                return context.getString(R.string.messages_invalid_characters_seed);

        } else if (seed.matches(".*[A-Z].*") && seed.matches(".*[a-z].*")) {
            if (seed.length() > SEED_LENGTH_MAX)
                return context.getString(R.string.messages_mixed_seed) + " " + context.getString(R.string.messages_seed_to_long);
            else if (seed.length() < SEED_LENGTH_MIN)
                return context.getString(R.string.messages_mixed_seed) + " " + context.getString(R.string.messages_seed_to_short);
            else
                return context.getString(R.string.messages_mixed_seed);

        } else if (seed.length() > SEED_LENGTH_MAX) {
            return context.getString(R.string.messages_to_long_seed);

        } else if (seed.length() < SEED_LENGTH_MIN) {
            return context.getString(R.string.messages_to_short_seed);
        }

        return null;
    }

    private static String getSeed(String seed) {

        seed = seed.toUpperCase();

        if (seed.length() > SEED_LENGTH_MAX)
            seed = seed.substring(0, SEED_LENGTH_MAX);

        seed = seed.replaceAll("[^A-Z9]", "9");

        seed = StringUtils.rightPad(seed, SEED_LENGTH_MAX, '9');

        return seed;
    }



    /*
    Dialogs
     */


    @SuppressWarnings("deprecation")
    private static Drawable getAppDrawable(Context context, int Rdrawable) {
        if(android.os.Build.VERSION.SDK_INT>= 21) {
            return context.getDrawable(Rdrawable);
        } else {
            return context.getResources().getDrawable(Rdrawable);
        }
    }

    /*
    QR generation
     */
    private static interface BmpCallback {
        void onFinishBmp(Bitmap bitmap);
    }
    private static interface CompletedCallback {
        void onComplete(boolean success);
    }
    protected static class Paper {

        protected static class EncPack {
            protected String encrypted;
            protected String ivHex;
            protected String salt;
            protected String name;

        }

        protected static EncPack encryptJson(JSONObject data, String password) throws Exception {
            EncPack pack = new EncPack();
            byte[] clean = data.toString().getBytes();
            // Generating IV.
            int ivSize = 16;
            byte[] iv = new byte[ivSize];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            StringBuilder buffer = new StringBuilder();
            for(int i=0;i < iv.length;i++){
                buffer.append((char)iv[i]);
            }
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            pack.ivHex=bytesToHex(iv);
            pack.salt=generateSalt();
            SecretKey skey = generateKey(pack.salt,password);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey,ivParameterSpec);

            pack.encrypted=Base64.encodeToString(cipher.doFinal(clean),Base64.DEFAULT);
            return pack;
        }
        public static void generateDigitalPaper(final String password, final Activity activity){
            if(seedEditText.getText().toString().trim().isEmpty()){
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                try {
                    String seed=seedEditText.getText().toString();
                    String seedImg=null;
                    String name=seedName.getText().toString();
                    String datefile=getFileDateYYYYMMDDHHMMSS();
                    if(name==null || name.isEmpty()) {
                        name=getFileDateYYYYMMDDHHMMSS();
                        datefile="";
                    }

                    if(checkDigital.isChecked()) {
                        JSONObject data = new JSONObject();
                        data.put("seed",seedEditText.getText().toString());
                        data.put("name",name);
                        Bitmap qr=Paper.generateImage(data,activity,checkDigital.isChecked());
                        if(qr!=null) {
                            //pack.imgbytes= encryptBitmap(qr,upass,pack.salt,pack.iv);
                            seedImg=bitmapToString(qr);
                        }
                    }
                    if(seedImg==null)
                        seedImg="";



                    JSONObject data = new JSONObject();
                    data.put("seed",seed);
                    data.put("date",getDisplayDate());
                    data.put("seedImg",seedImg);
                    addRandomPadData(data);

                    EncPack pack = encryptJson(data,password);

                    String htmlContent = getResourceFileContent("paper.html");
                    htmlContent=htmlContent.replace("$DATA",pack.encrypted)
                            .replace("$IV",pack.ivHex)
                            .replace("$SALT",pack.salt)
                            .replace("$NAME",name);

                    name=name.replaceAll(" ","");
                    String file=hdd+"paper-"+name+datefile+".html";
                    writeToFile(file,htmlContent);
                    pack=null;
                    Snackbar.make(container,R.string.gen_paper_done+" "+file,Snackbar.LENGTH_LONG).show();
                } catch(Exception e) {
                    Snackbar.make(container,R.string.task_failed,Snackbar.LENGTH_LONG).show();
                }

                }
            }).start();
        }

        private static void addRandomPadData(JSONObject ob) {
            int size=getRandom(3,5);
            for(int i=0; i<size; i++) {
                int esize=getRandom(8,12);
                StringBuffer sb=new StringBuffer();
                for(int j=0; j<size; j++) {
                    String pad=bytesToHex((getRandom(910101011,2101010101)+"").getBytes());
                    sb.append(pad);
                }
                try {
                    ob.put("pad" + i, sb.toString());
                } catch(Exception e){}
            }
        }
        private static int getRandom(int min, int max) {
            int rand = Double.valueOf(((max+1-min)*Math.random())+min).intValue();
            if(rand>max)
                rand=max;
            return rand;
        }
        public static void generateImage(final JSONObject qrjson, final Activity activity, final boolean withQR, final BmpCallback callback){

            if(qrjson==null){
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    callback.onFinishBmp(generateImage(qrjson,activity,withQR));
                }
            }).start();
        }

        public static Bitmap generateImage(final JSONObject qrjson, final Activity activity, boolean withQR){
            if(qrjson==null){
                return null;
            }
            int size = 390;
            int fixed = 200;
            int sizeM=230;
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 1);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            try {
                BitMatrix byteMatrix = qrCodeWriter.encode(qrjson.toString(), BarcodeFormat.QR_CODE, size,
                        size, hintMap);
                int height = byteMatrix.getHeight();
                int width = byteMatrix.getWidth();
                final Bitmap qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++){
                    for (int y = 0; y < height; y++){
                        qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                    }
                }
                Bitmap background= Bitmap.createScaledBitmap(((BitmapDrawable) getAppDrawable(activity, R.drawable.qr_bg)).getBitmap(),390,90,false);
                Bitmap bmOverlay = null;
                if(withQR) {
                    bmOverlay = Bitmap.createBitmap(qrImage.getWidth(), qrImage.getHeight() + sizeM, qrImage.getConfig());
                } else {
                    bmOverlay =Bitmap.createBitmap(size, fixed, qrImage.getConfig());
                }
                Canvas canvas = new Canvas(bmOverlay);
                Rect bg = new Rect();
                bg.set(0,0,qrImage.getWidth(),qrImage.getHeight()+sizeM);
                Paint bgp = new Paint(Paint.ANTI_ALIAS_FLAG);
                // text color - #3D3D3D
                bgp.setColor(Color.rgb(255,255,255));
                canvas.drawRect(bg,bgp);
                if(withQR) {
                    canvas.drawBitmap(qrImage, 0, sizeM, null);
                }
                canvas.drawBitmap(background, 0, 0,null );

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(3,10,3));
                paint.setTextSize((int) (16));
                paint.setShadowLayer(1f, 0f, 1f, Color.argb(10,10,20,10));

                Paint spaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                spaint.setColor(Color.rgb(1,1,1));
                spaint.setTextSize((int) (12));

                Rect bounds = new Rect();
                String seedStr=qrjson.optString("seed");
                paint.getTextBounds(seedStr.substring(0,30), 0, 30, bounds);

                int x = (qrImage.getWidth() - bounds.width())/2;
                int y = 120;


                canvas.drawText(seedStr.substring(0,30), x, y+20 , paint);
                canvas.drawText(seedStr.substring(30,60), x, y+40, paint);
                canvas.drawText(seedStr.substring(60,seedStr.length()), x, y+60, paint);
                canvas.drawText("Store this somewhere safe, the seed allows anyone", x, y+90 , spaint);
                canvas.drawText("who knows it to have full access the IOTA secured", x, y+110 , spaint);
                String name=seedName.getText().toString();
                if(name==null || name.isEmpty()) {
                    name=getFileDateYYYYMMDDHHMMSS();
                }
                canvas.drawText(activity.getString(R.string.name)+": "+name, x, y , paint);
                return bmOverlay;

            } catch (WriterException e) {
                Log.e("QR.ex",""+e.getMessage());

            }
            return null;
        }


    }
    protected static void writeToFile(String filePath, String content) {
        try {
            File f=new File(filePath);
            if(!f.exists()) {
                File dir=new File(f.getParent());
                dir.mkdirs();
                f.createNewFile();
            }
            FileWriter fw=new FileWriter(filePath);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(content);
            out.flush();
            out.close();
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(f));
            activity.sendBroadcast(intent);
            if(activity!=null && AppService.isAppStarted()) {
                Snackbar.make(activity.findViewById(R.id.container), activity.getString(R.string.created_file) + " " + f.getAbsolutePath(), Snackbar.LENGTH_SHORT).show();
            }
            try {
                fw.flush();
                fw.close();
            } catch(Exception e){}
        } catch(Exception e) {
            Log.e("EX",e.getMessage());
            if(activity!=null && AppService.isAppStarted()) {
                Snackbar.make(activity.findViewById(R.id.container), activity.getString(R.string.task_failed), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
    private void printImage(Bitmap bitmap) {
        try {
            PrintHelper photoPrinter = new PrintHelper(activity);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("Ink check", bitmap);
        } catch(Exception e) {
            Snackbar.make(activity.findViewById(R.id.container),activity.getString(R.string.task_failed),Snackbar.LENGTH_SHORT).show();
        }
    }
    protected static String getFileDateYYYYMMDDHHMMSS()  {
        GregorianCalendar gc = new GregorianCalendar();
        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
        int month = gc.get(GregorianCalendar.MONTH)+1;
        int year = gc.get(GregorianCalendar.YEAR);
        int hour = gc.get(GregorianCalendar.HOUR_OF_DAY);
        int minutes = gc.get(GregorianCalendar.MINUTE);
        return year + "-" + df.format(month) + "-"+ df.format(day) + "-" +df.format(hour)+"h"+df.format(minutes)+"m";
    }
    public static String getDisplayDate()  {
        GregorianCalendar gc = new GregorianCalendar();
        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
        int month = gc.get(GregorianCalendar.MONTH)+1;
        int year = gc.get(GregorianCalendar.YEAR);
        int hour = gc.get(GregorianCalendar.HOUR_OF_DAY);
        int min = gc.get(GregorianCalendar.MINUTE);
        int sec = gc.get(GregorianCalendar.SECOND);
        return year + "-" + df.format(month) + "-"+ df.format(day) + " " +df.format(hour)+":"+df.format(min)+":"+df.format(sec);
    }
    /*

    FILE PERMISSIONS
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("result",""+resultCode+" -- "+requestCode);
        switch (resultCode) {

            case REQUEST_WRITE_STORAGE:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Thanks
                }
                return;
            case REQUEST_CAMERA_PERMISSION:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    goScreen(SCR_QR);
                }
                return;

        }
    }

    private static void requestFileWritePermission(Activity activity) {

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);

    }
    private static final boolean hasFilePermission(Context context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        return hasPermission;
    }

    /*
    Enter Password Dialog
     */
    public static class GoDigitalDialog extends DialogFragment {

        TextInputLayout textInputLayoutPassword;
        TextInputLayout textInputLayoutPasswordConfirm;
        TextInputEditText textInputEditTextPassword;
        TextInputEditText textInputEditTextPasswordConfirm;
        AlertDialog alertDialog;
        boolean isSeed=true;

        public GoDigitalDialog() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.dialog, null, false);

            textInputLayoutPassword=view.findViewById(R.id.password_input_layout);
            textInputLayoutPasswordConfirm=view.findViewById(R.id.password_confirm_input_layout);
            textInputEditTextPassword=view.findViewById(R.id.password);
            textInputEditTextPasswordConfirm=view.findViewById(R.id.password_confirm);

            alertDialog = new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.password)
                    .setCancelable(false)
                    .setPositiveButton(R.string.save, null)
                    .setNegativeButton(R.string.buttons_cancel, null)
                    .create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkGo();
                        }
                    });
                }
            });

            alertDialog.show();
            setKeyboard(getActivity(),view.findViewById(R.id.password),true);
            return alertDialog;

        }

        private void checkGo() {
            String password = textInputEditTextPassword.getText().toString();
            String passwordConfirm = textInputEditTextPasswordConfirm.getText().toString();

            //reset errors
            textInputLayoutPassword.setError(null);
            textInputLayoutPasswordConfirm.setError(null);

            if (password.isEmpty())
                textInputLayoutPassword.setError(getActivity().getString(R.string.messages_empty_password));
            else if (!password.equals(passwordConfirm))
                textInputLayoutPasswordConfirm.setError(getActivity().getString(R.string.messages_match_password));
            else if (passwordConfirm.length()<8 || passwordConfirm.length()>40)
                textInputLayoutPassword.setError(getActivity().getString(R.string.messages_minmax_password));
            else {


                if(isSeed) {
                    Snackbar.make(container,R.string.gen_paper,Snackbar.LENGTH_SHORT).show();
                    Paper.generateDigitalPaper(password, activity);
                } else {
                    Snackbar.make(container,R.string.gen_paper,Snackbar.LENGTH_SHORT).show();
                    int st=(Integer)spStart.getSelectedItem();
                    int en=(Integer)spStop.getSelectedItem();
                    String name=seedName.getText().toString();
                    String datefile=getFileDateYYYYMMDDHHMMSS();
                    if(name==null || name.isEmpty()) {
                        name=getFileDateYYYYMMDDHHMMSS();
                        datefile="";
                    }

                    AppService.generateAddressTask(name,datefile,password,seedEditText.getText().toString(),st,en-st);
                }

                alertDialog.hide();
                alertDialog.cancel();
                alertDialog=null;

            }
        }

    }
    private static void setKeyboard(Activity activity, View editTextView, boolean showKeyboard) {
        editTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            if(showKeyboard) {
                imm.showSoftInput(editTextView, 0);
            } else {
                imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
            }
        }
    }
    protected static String getResourceFileContent(String file)   {
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open(file)));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                contents.append(mLine);
                contents.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return contents.toString();
    }

    /*
    QR scanner
     */
    private static final int REQUEST_CAMERA_PERMISSION = 12;
    public static boolean hasCameraPermission(Context context) {
        int result = activity.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);

            } else {

                //Camera permissions have not been granted yet so request them directly
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        }
    }
    private static boolean openQrScanner(Activity activity) {
        if(activity!=null) {
            FragmentManager fm = activity.getFragmentManager();
            FragmentTransaction tr = fm.beginTransaction();
            tr.replace(R.id.qr_screen, Fragment.instantiate(activity, QRScannerFragment.class.getName()),QRScannerFragment.class.getClass().getCanonicalName());

            tr.commit();
        }
        return true;
    }
    public static class QRScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

        private ZXingScannerView scannerView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            scannerView = new ZXingScannerView(getActivity());
            return scannerView;
        }

        @Override
        public void onResume() {
            super.onResume();
            //Store.setCurrentFragment(this.getClass());
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }

        @Override
        public void onPause() {
            super.onPause();
            scannerView.stopCamera();
        }

        @Override
        public void handleResult(Result result) {

            String strRes=String.valueOf(result);

            if(strRes!=null && !strRes.isEmpty()) {
                JSONObject res=null;
                try {
                    res=new JSONObject(strRes);
                } catch (Exception e) {
                    Log.e("JSON","ex: "+e.getMessage());
                }
                if(res!=null) {
                    String smsg=isSeedValid(activity,res.optString("seed"));
                    if(smsg==null) {
                        activity.cameraSeed=res.optString("seed");
                        seedEditText.setText(res.optString("seed"));
                        seedName.setText(res.optString("name"));
                    } else  {
                        Snackbar.make(container,smsg,Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    if(isSeedValid(activity,strRes)==null) {
                        seedEditText.setText(strRes);
                        seedName.setText("");
                    } else  {
                        Snackbar.make(container,R.string.no_seed_data,Snackbar.LENGTH_SHORT).show();
                    }
                }

            } else {
                Snackbar.make(container,R.string.no_seed_data,Snackbar.LENGTH_SHORT).show();
            }
            scannerView.stopCamera();
            scannerView.invalidate();
            activity.goScreen(SCR_ONE);
        }

    }

}
