package com.android.printApis.Mini;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.android.printApis.Print.PrinterData;
import com.prints.printerservice.IPrinterCallback;
import com.prints.printerservice.IPrinterService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guangyi.peng on 2017/3/1.
 */
public class MiniPrinterManager {
    /**
     * <item>"NORMAL"</item> 0 <item>"BOLD"</item> 1 <item>"SERIF"</item> 2
     * <item>"ttf_Arial.ttf"</item> 3 <item>"ttf_FangHei.ttf"</item> 4
     * <item>"ttf_FranklinGothic.ttf"</item> 5
     * <item>"ttf_Haettenschweiler.ttf"</item> 6 <item>"ttf_HuaSong.ttf"</item>
     * 7 <item>"ttf_SansMono.ttf"</item> 8
     **/

    private final static String KEY_ALIGN = "key_attributes_align";
    private final static String KEY_TEXTSIZE = "key_attributes_textsize";
    private final static String KEY_TYPEFACE = "key_attributes_typeface";
    private final static String KEY_MARGINLEFT = "key_attributes_marginleft";
    private final static String KEY_MARGINRIGHT = "key_attributes_marginright";

    private final static String KEY_LINESPACE = "key_attributes_linespace";
    private final static String KEY_WEIGHT = "key_attributes_weight";
    private static final String TAG = "PrinterSampleManager";

    private static MiniPrinterManager sPrinterManager;


    public interface PrinterManagerListener {
        public void onServiceConnected();
    }

    public static void init(Activity activity) {
        sPrinterManager = new MiniPrinterManager(activity);
        sPrinterManager.onPrinterStart();
    }

    public MiniPrinterManager(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity.getApplicationContext();
    }

    private Activity mActivity;
    private Context mContext;

    private IPrinterCallback mCallback = null;
    private IPrinterService mPrinterService;
    private boolean mPrinterServiceConnected = false;
    public static long sCurrentLength = 0;
    public static long sTotalLength = 0;

    private ServiceConnection mConnectionService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
            mPrinterService = null;
            mPrinterServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            mPrinterService = IPrinterService.Stub.asInterface(service);
            printerInit();
//			mListener.onServiceConnected();
            mPrinterServiceConnected = true;
        }
    };

    public boolean hasAndroidPrinter(Context context) {
        boolean hasPrinter = false;
        final String PKG_PRINT = "com.print.printerservice";
        List<String> packagesName = getInstalledPackagesName(context);
        if (packagesName.contains(PKG_PRINT)) {
            hasPrinter = true;
        }
        return hasPrinter;
    }

    private static List<String> getInstalledPackagesName(Context context) {
        List<String> packagesName = new ArrayList<String>();
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);
        packagesName.clear();
        for (PackageInfo pi : packages) {
            packagesName.add(pi.packageName);
        }
        return packagesName;
    }

    private IPrinterService getPrinterService() {
        if (mPrinterService == null) {
            // reconnect printerservice
            Intent intent = new Intent();
            intent.setPackage("com.prints.printerservice");
            intent.setAction("com.prints.printerservice.IPrinterService");
            mActivity.startService(intent);
            mActivity.bindService(intent, mConnectionService,
                    Context.BIND_AUTO_CREATE);
        }
        return mPrinterService;
    }

    private void printTextSample(String content, IPrinterCallback callback) {
        IPrinterService service = getPrinterService();
        if (service != null && mPrinterServiceConnected) {
            try {
                service.printText(content, callback);
            } catch (Exception e) {
            }
        }
    }

    public void onPrinterStart() {
        mCallback = new IPrinterCallback.Stub() {
            @Override
            public void onException(int code, final String msg)
                    throws RemoteException {
                Log.e(TAG, "onException code=" + code + " msg=" + msg);
            }

            @Override
            public void onLength(long current, long total)
                    throws RemoteException {
                Log.e(TAG, "onLength");
                sCurrentLength = current;
                sTotalLength = total;
            }

            @Override
            public void onComplete() throws RemoteException {
                Log.e(TAG, "onComplete");
            }
        };

        Intent intent = new Intent();
        intent.setPackage("com.prints.printerservice");
        intent.setAction("com.prints.printerservice.IPrinterService");
        mActivity.startService(intent);
        mActivity.bindService(intent, mConnectionService,
                Context.BIND_AUTO_CREATE);
    }

    public void onPrinterStop() {
        // /*
        try {
            mActivity.unbindService(mConnectionService);
        } catch (Exception e) {

        }
        // */
        // mActivity.finish();
    }

    public void copyFile() {
        String oldPath = "/sdcard/Print/Print_Preview.jpg";
        String fileName = System.currentTimeMillis() + ".jpg";
        String newPath = "/sdcard/wanwang/print/";
        File newFilePath = new File(newPath);
        if (!newFilePath.exists()) {
            newFilePath.mkdirs();
        }
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath + fileName);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();

                oldfile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRAWData(final byte[] data) {
        try {
            Log.e(TAG, "sendRAWData");
            mPrinterService.sendRAWData(data, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printText(final String text) {
        try {
            mPrinterService.printText(text, mCallback);
        } catch (Exception e) {

        }
    }

    public void printTextWithAttributes(final String text, final Map attributes) {
        try {
            mPrinterService
                    .printTextWithAttributes(text, attributes, mCallback);
        } catch (Exception e) {

        }
    }

    public void printColumnsTextWithAttributes(final String[] text,
                                               final List attributes) {
        try {
            mPrinterService.printColumnsTextWithAttributes(text, attributes,
                    mCallback);
        } catch (Exception e) {

        }
    }

    public void printBarCode(final String content, final int align,
                             final int width, final int height, final boolean showContent) {
        try {
            mPrinterService.printBarCode(content, align, width, height,
                    showContent, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printQRCode(final String text, final int align, final int size) {
        try {
            mPrinterService.printerInit(mCallback);
            mPrinterService.printQRCode(text, align, size, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printBitmap(final Bitmap bitmap) {
        try {
            mPrinterService.printBitmap(bitmap, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printBitmap(final Bitmap bitmap, final Map attributes) {
        try {
            mPrinterService.printBitmapWithAttributes(bitmap, attributes,
                    mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printWrapPaper(final int n) {
        try {
            mPrinterService.printWrapPaper(n, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setPrinterSpeed(final int level) {
        try {
            mPrinterService.setPrinterSpeed(level, mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void upgradePrinter() {
        try {
            mPrinterService.upgradePrinter();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getFirmwareVersion() {
        try {
            return mPrinterService.getFirmwareVersion();
        } catch (RemoteException e) {

        }
        return "";
    }

    public String getBootloaderVersion() {
        try {
            return mPrinterService.getBootloaderVersion();
        } catch (RemoteException e) {

        }
        return "";
    }

    public void printerInit() {
        try {
            mPrinterService.printerInit(mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i(TAG, "printerInit error=" + e);
        }
    }

    public void printerReset() {
        try {
            mPrinterService.printerReset(mCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int printerTemperature(final Activity activity) {
        int temperature = -1;
        try {
            temperature = mPrinterService.printerTemperature(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return temperature;
    }

    public static void PrinterStop() {
        sPrinterManager.onPrinterStop();
    }

    private boolean printerPaper() {
        boolean hasPaper = false;
        try {
            hasPaper = mPrinterService.printerPaper(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return hasPaper;
    }

    public static void startMiniBarCode(final byte[] extra_order) {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sPrinterManager.sendRAWData(PrinterData.PRINT_BAR_CODE("Hello World", extra_order));
//					sPrinterManager.sendRAWData(PrinterData.PRINT_BAR_CODE());
//					sPrinterManager.printBarCode("20170301", 1, 300, 100, true);
//					sPrinterManager.printText("\n");
//					sPrinterManager.printWrapPaper(10);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniBarCodeByLib() {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sPrinterManager.printBarCode("20170301", 0, 300, 100, true);
                    sPrinterManager.printText("\n");
                    sPrinterManager.printWrapPaper(10);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniQRCode(final byte[] extra_parameters) {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sPrinterManager.sendRAWData(PrinterData.PRINT_QR_CODE("http://www.apple.com", extra_parameters));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniQRCodeByLib() {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                sPrinterManager.printQRCode("www.baidu.com", 0, 200);
                sPrinterManager.printText("\n");
                sPrinterManager.printWrapPaper(10);
            }
        });
    }

    public static void startMiniBitmap(final Activity activity, final byte[] extra_order) {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.img);
                    sPrinterManager.sendRAWData(PrinterData.PRINT_BITMAP(bitmap, extra_order));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniBitmapByLib(final Activity activity) {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                Map<String, Integer> map = new HashMap<>();
                map.put(KEY_ALIGN, 0);
                Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.drive);
                sPrinterManager.printBitmap(bitmap, map);
            }
        });
    }

    public static void onPrinterTestClicked() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
//					sPrinterManager.sendRAWData(PrinterData.SWIFIN_PRINT_TEST());
////				    sPrinterManager.sendRAWData(PrinterData.setTextSize((byte) 0.5));
//                    sPrinterManager.sendRAWData("Textsize 22&Bold".getBytes());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniTest1(final byte[] order) {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sPrinterManager.sendRAWData(PrinterData.PRINT_TEST1_TEXT(order));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniTest1ByLib() {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                String text = "FORMAT TESTS\n";
                sPrinterManager.printText(text);
                Map<String, Integer> map = new HashMap<>();
                map.put(KEY_ALIGN, 2);
                map.put(KEY_TYPEFACE, 1);
                map.put(KEY_TEXTSIZE, 22);
                String text2 = "Textsize 22&Bold\n";
                sPrinterManager.printTextWithAttributes(text2, map);
                Map<String, Integer> map2 = new HashMap<>();
                map2.put(KEY_ALIGN, 0);
                map2.put(KEY_TEXTSIZE, 24);
                String text3 = "Textsize 24&Normal\n";
                String text4 = "Name   Lables   Counts   Money\n";
                String text5 = "Apple  Iphone     1      6088\n";
                sPrinterManager.printTextWithAttributes(text3, map2);
                map.put(KEY_TEXTSIZE, 22);
                sPrinterManager.printTextWithAttributes(text4, map2);
                sPrinterManager.printTextWithAttributes(text5, map2);
                sPrinterManager.printWrapPaper(10);
            }
        });
    }

    public static void startMiniTest2(final byte[] order) {
//		final Map<String, Integer> map1 = new HashMap<String, Integer>();
//		map1.put(KEY_LINESPACE, 0);
//		map1.put(KEY_TEXTSIZE, 20);
//		map1.put(KEY_TYPEFACE, 0);
//		map1.put(KEY_ALIGN, 1);
//		map1.put(KEY_MARGINLEFT, 15);
//		map1.put(KEY_MARGINRIGHT, 5);
//
//		final Map<String, Integer> map2 = new HashMap<String, Integer>();
//		map2.put(KEY_LINESPACE, 0);
//		map2.put(KEY_TEXTSIZE, 20);
//		map1.put(KEY_TYPEFACE, 0);
//		map2.put(KEY_ALIGN, 1);
//		map2.put(KEY_MARGINLEFT, 20);
//		map2.put(KEY_MARGINRIGHT, 5);
//
//		final Map<String, Integer> map3 = new HashMap<String, Integer>();
//		map3.put(KEY_LINESPACE, 0);
//		map3.put(KEY_TEXTSIZE, 20);
//		map3.put(KEY_TYPEFACE, 0);
//		map3.put(KEY_ALIGN, 1);
//		map3.put(KEY_MARGINLEFT, 5);
//		map3.put(KEY_MARGINRIGHT, 5);

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
//					sPrinterManager.printText("Default left margin 10");
//					// sPrinterManager.printText("\n");
//
//					sPrinterManager.printTextWithAttributes(
//							"Set left margin 15", map1);
//					// sPrinterManager.printText("\n");
//					sPrinterManager.printTextWithAttributes(
//							"Set left margin 20", map2);
//					sPrinterManager.printTextWithAttributes(
//							"Set left margin 5", map3);
//					// sPrinterManager.printText("\n");

//					sPrinterManager.printText("\n\n");
                    sPrinterManager.sendRAWData(PrinterData.PRINT_TEST1_TEXT2(order));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniPaperFeed() {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
//					sPrinterManager.printWrapPaper(10);
                    sPrinterManager.sendRAWData(PrinterData.PAPER_FEED());
                    // sPrinterManager.printText("\n");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniPaperFeedByLib() {
//        sPrinterManager.copyFile();
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                sPrinterManager.printWrapPaper(77);
            }
        });
    }

    public static void startMiniReceipt(Activity activity) {
        final Bitmap cielo_title = BitmapFactory.decodeResource(
                activity.getResources(), R.raw.printer);
        final Map<String, Integer> map_cielo = new HashMap<String, Integer>();
        map_cielo.put(KEY_ALIGN, 0);
        map_cielo.put(KEY_MARGINLEFT, 5);
        map_cielo.put(KEY_MARGINRIGHT, 5);

        final Map<String, Integer> mapCielo = new HashMap<String, Integer>();
        mapCielo.put(KEY_LINESPACE, 10);
        mapCielo.put(KEY_TEXTSIZE, 48);
        mapCielo.put(KEY_TYPEFACE, 1);
        mapCielo.put(KEY_ALIGN, 0);
        mapCielo.put(KEY_MARGINLEFT, 0);
        mapCielo.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> mapTitle = new HashMap<String, Integer>();
        mapTitle.put(KEY_LINESPACE, 10);
        mapTitle.put(KEY_TEXTSIZE, 24);
        mapTitle.put(KEY_TYPEFACE, 0);
        mapTitle.put(KEY_ALIGN, 0);
        mapTitle.put(KEY_MARGINLEFT, 0);
        mapTitle.put(KEY_MARGINRIGHT, 0);
        final String stringtext1 = "VIA - ESTABELECIMENTO / POS=69000004\nCNPJ:00.000.000/000-00\nMENSAGEM TBL F";
        final String stringtext2 = "Alameda Grajau, 219\nBarueri  -  SP\n00000000000003  DOC=305262  AUT=2000053";
        final Map<String, Integer> maptext1 = new HashMap<String, Integer>();
        maptext1.put(KEY_LINESPACE, 10);
        maptext1.put(KEY_TEXTSIZE, 20);
        maptext1.put(KEY_TYPEFACE, 0);
        maptext1.put(KEY_ALIGN, 1);
        maptext1.put(KEY_MARGINLEFT, 0);
        maptext1.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext2 = new HashMap<String, Integer>();
        maptext2.put(KEY_LINESPACE, 10);
        maptext2.put(KEY_TEXTSIZE, 20);
        maptext2.put(KEY_TYPEFACE, 0);
        maptext2.put(KEY_ALIGN, 1);
        maptext2.put(KEY_MARGINLEFT, 10);
        maptext2.put(KEY_MARGINRIGHT, 10);

        final Map<String, Integer> maptext3 = new HashMap<String, Integer>();
        maptext3.put(KEY_LINESPACE, 10);
        maptext3.put(KEY_TEXTSIZE, 20);
        maptext3.put(KEY_TYPEFACE, 0);
        maptext3.put(KEY_ALIGN, 1);
        maptext3.put(KEY_MARGINLEFT, 20);
        maptext3.put(KEY_MARGINRIGHT, 20);

        final Map<String, Integer> maptext4 = new HashMap<String, Integer>();
        maptext4.put(KEY_LINESPACE, 10);
        maptext4.put(KEY_TEXTSIZE, 20);
        maptext4.put(KEY_TYPEFACE, 0);
        maptext4.put(KEY_ALIGN, 1);
        maptext4.put(KEY_MARGINLEFT, 50);
        maptext4.put(KEY_MARGINRIGHT, 50);

        final Map<String, Integer> mapbold = new HashMap<String, Integer>();
        mapbold.put(KEY_LINESPACE, 10);
        mapbold.put(KEY_TEXTSIZE, 20);
        mapbold.put(KEY_TYPEFACE, 0);
        mapbold.put(KEY_ALIGN, 1);
        mapbold.put(KEY_MARGINLEFT, 0);
        mapbold.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> map1 = new HashMap<String, Integer>();
        map1.put(KEY_LINESPACE, 0);
        map1.put(KEY_TEXTSIZE, 20);
        map1.put(KEY_TYPEFACE, 1);
        map1.put(KEY_ALIGN, 2);
        map1.put(KEY_MARGINLEFT, 0);
        map1.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> map2 = new HashMap<String, Integer>();
        map2.put(KEY_LINESPACE, 0);
        map2.put(KEY_TEXTSIZE, 20);
        map2.put(KEY_ALIGN, 0);
        map2.put(KEY_MARGINLEFT, 0);
        map2.put(KEY_MARGINRIGHT, 0);

        final String[] cols1Texts = {"13/03/17", "20:00", "ONL-D"};
        final String[] cols2Texts = {"VALOR:", " ", "66,22"};

        final Map<String, Integer> attrCols1Map1 = new HashMap<String, Integer>();
        attrCols1Map1.put(KEY_LINESPACE, 0);
        attrCols1Map1.put(KEY_TEXTSIZE, 20);
        attrCols1Map1.put(KEY_ALIGN, 1);
        attrCols1Map1.put(KEY_WEIGHT, 1);
        attrCols1Map1.put(KEY_TYPEFACE, 0);
        final Map<String, Integer> attrCols1Map2 = new HashMap<String, Integer>();
        attrCols1Map2.put(KEY_LINESPACE, 0);
        attrCols1Map2.put(KEY_TEXTSIZE, 20);
        attrCols1Map2.put(KEY_ALIGN, 0);
        attrCols1Map2.put(KEY_WEIGHT, 1);
        attrCols1Map1.put(KEY_TYPEFACE, 0);
        final Map<String, Integer> attrCols1Map3 = new HashMap<String, Integer>();
        attrCols1Map3.put(KEY_LINESPACE, 0);
        attrCols1Map3.put(KEY_TEXTSIZE, 20);
        attrCols1Map3.put(KEY_ALIGN, 0);
        attrCols1Map3.put(KEY_WEIGHT, 1);
        attrCols1Map1.put(KEY_TYPEFACE, 0);

        final List attrCols1 = new ArrayList();
        attrCols1.add(attrCols1Map1);
        attrCols1.add(attrCols1Map2);
        attrCols1.add(attrCols1Map3);

        final List attrCols2 = new ArrayList();
        attrCols2.add(attrCols1Map1);
        attrCols2.add(attrCols1Map2);
        attrCols2.add(attrCols1Map3);

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sPrinterManager.printBitmap(cielo_title, map_cielo);
                    // sPrinterManager.printTextWithAttributes("CIELO",
                    // mapCielo);
                    sPrinterManager.printTextWithAttributes("Visa", mapTitle);
                    sPrinterManager.printTextWithAttributes(
                            "CREDITO  A  VISTA  -  I", mapTitle);
                    sPrinterManager.printTextWithAttributes("442780 - 0865",
                            mapTitle);
                    sPrinterManager.printTextWithAttributes(stringtext1,
                            maptext1);
                    sPrinterManager.printTextWithAttributes("POSTO  ABC",
                            mapbold);
                    sPrinterManager.printTextWithAttributes(stringtext2,
                            maptext1);
                    sPrinterManager.printColumnsTextWithAttributes(cols1Texts,
                            attrCols1);
                    sPrinterManager.printTextWithAttributes(
                            "VENDA  A  CERDITO", maptext1);
                    sPrinterManager.printColumnsTextWithAttributes(cols2Texts,
                            attrCols2);
                    sPrinterManager.printTextWithAttributes(
                            "MENSAGEM  TBL  DO", maptext1);

                    sPrinterManager.printTextWithAttributes(
                            "VENDA  A  CERDITO", maptext1);
                    sPrinterManager.printTextWithAttributes(
                            "VENDA  A  CERDITO", maptext2);
                    sPrinterManager.printTextWithAttributes(
                            "VENDA  A  CERDITO", maptext3);
                    sPrinterManager.printTextWithAttributes(
                            "VENDA  A  CERDITO", maptext4);
                    sPrinterManager.printText("\n\n\n");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startMiniDiffFonts() {
        final Map<String, Integer> maptext1 = new HashMap<String, Integer>();
        maptext1.put(KEY_LINESPACE, 10);
        maptext1.put(KEY_TEXTSIZE, 26);
        maptext1.put(KEY_TYPEFACE, 0);
        maptext1.put(KEY_ALIGN, 1);
        maptext1.put(KEY_MARGINLEFT, 0);
        maptext1.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext2 = new HashMap<String, Integer>();
        maptext2.put(KEY_LINESPACE, 10);
        maptext2.put(KEY_TEXTSIZE, 26);
        maptext2.put(KEY_TYPEFACE, 1);
        maptext2.put(KEY_ALIGN, 1);
        maptext2.put(KEY_MARGINLEFT, 0);
        maptext2.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext3 = new HashMap<String, Integer>();
        maptext3.put(KEY_LINESPACE, 10);
        maptext3.put(KEY_TEXTSIZE, 26);
        maptext3.put(KEY_TYPEFACE, 2);
        maptext3.put(KEY_ALIGN, 1);
        maptext3.put(KEY_MARGINLEFT, 0);
        maptext3.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext4 = new HashMap<String, Integer>();
        maptext4.put(KEY_LINESPACE, 10);
        maptext4.put(KEY_TEXTSIZE, 26);
        maptext4.put(KEY_TYPEFACE, 3);
        maptext4.put(KEY_ALIGN, 1);
        maptext4.put(KEY_MARGINLEFT, 0);
        maptext4.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext5 = new HashMap<String, Integer>();
        maptext5.put(KEY_LINESPACE, 10);
        maptext5.put(KEY_TEXTSIZE, 26);
        maptext5.put(KEY_TYPEFACE, 4);
        maptext5.put(KEY_ALIGN, 1);
        maptext5.put(KEY_MARGINLEFT, 0);
        maptext5.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext6 = new HashMap<String, Integer>();
        maptext6.put(KEY_LINESPACE, 10);
        maptext6.put(KEY_TEXTSIZE, 26);
        maptext6.put(KEY_TYPEFACE, 5);
        maptext6.put(KEY_ALIGN, 1);
        maptext6.put(KEY_MARGINLEFT, 0);
        maptext6.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext7 = new HashMap<String, Integer>();
        maptext7.put(KEY_LINESPACE, 10);
        maptext7.put(KEY_TEXTSIZE, 26);
        maptext7.put(KEY_TYPEFACE, 6);
        maptext7.put(KEY_ALIGN, 1);
        maptext7.put(KEY_MARGINLEFT, 0);
        maptext7.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext8 = new HashMap<String, Integer>();
        maptext8.put(KEY_LINESPACE, 10);
        maptext8.put(KEY_TEXTSIZE, 26);
        maptext8.put(KEY_TYPEFACE, 7);
        maptext8.put(KEY_ALIGN, 1);
        maptext8.put(KEY_MARGINLEFT, 0);
        maptext8.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext9 = new HashMap<String, Integer>();
        maptext9.put(KEY_LINESPACE, 10);
        maptext9.put(KEY_TEXTSIZE, 26);
        maptext9.put(KEY_TYPEFACE, 8);
        maptext9.put(KEY_ALIGN, 1);
        maptext9.put(KEY_MARGINLEFT, 0);
        maptext9.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext10 = new HashMap<String, Integer>();
        maptext10.put(KEY_LINESPACE, 10);
        maptext10.put(KEY_TEXTSIZE, 26);
        maptext10.put(KEY_TYPEFACE, 9);
        maptext10.put(KEY_ALIGN, 1);
        maptext10.put(KEY_MARGINLEFT, 0);
        maptext10.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext11 = new HashMap<String, Integer>();
        maptext11.put(KEY_LINESPACE, 10);
        maptext11.put(KEY_TEXTSIZE, 26);
        maptext11.put(KEY_TYPEFACE, 10);
        maptext11.put(KEY_ALIGN, 1);
        maptext11.put(KEY_MARGINLEFT, 0);
        maptext11.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext12 = new HashMap<String, Integer>();
        maptext12.put(KEY_LINESPACE, 10);
        maptext12.put(KEY_TEXTSIZE, 26);
        maptext12.put(KEY_TYPEFACE, 11);
        maptext12.put(KEY_ALIGN, 1);
        maptext12.put(KEY_MARGINLEFT, 0);
        maptext12.put(KEY_MARGINRIGHT, 0);

        final Map<String, Integer> maptext13 = new HashMap<String, Integer>();
        maptext13.put(KEY_LINESPACE, 10);
        maptext13.put(KEY_TEXTSIZE, 26);
        maptext13.put(KEY_TYPEFACE, 12);
        maptext13.put(KEY_ALIGN, 1);
        maptext13.put(KEY_MARGINLEFT, 0);
        maptext13.put(KEY_MARGINRIGHT, 0);

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {

                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test normal fonts,font size is 26\n中文测试打印效果",
                                    maptext1);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test bold fonts,font size is 26\n中文测试打印效果",
                                    maptext2);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test serif fonts,font size is 26\n中文测试打印效果",
                                    maptext3);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test Arial fonts,font size is 26\n中文测试打印效果",
                                    maptext4);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test FangHei fonts,font size is 26\n中文测试打印效果",
                                    maptext5);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test FranklinGothic fonts,font size is 26\n中文测试打印效果",
                                    maptext6);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test Haettenschweiler fonts,font size is 26\n中文测试打印效果",
                                    maptext7);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test HuaSong fonts,font size is 26\n中文测试打印效果",
                                    maptext8);
                    sPrinterManager.printText("\n");
                    sPrinterManager
                            .printTextWithAttributes(
                                    "This used for test Sanmono fonts,font size is 26\n中文测试打印效果",
                                    maptext9);
                    sPrinterManager.printText("\n\n\n");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void showSelectionSpeed(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.text_printer_speed_settings));
        final String[] speeds = {"Level1", "Level2", "Level3", "Level4",
                "Level5", "Level6"};
        builder.setItems(speeds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final int speed = which + 1;
                ThreadPoolManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            sPrinterManager.setPrinterSpeed(speed);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        builder.show();
    }

    public static void showSelectionFonts(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.text_printer_font_settings));
        final String[] speeds = {"Normal", "Bold", "Serif", "Arial",
                "FangHei", "FranklinGothic", "Haettenschweiler", "Huasong",
                "SansMono"};
        builder.setItems(speeds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.util.Log.i("tyj", "which value = " + which);
                onPrinterDifferentFont(which);
            }
        });
        builder.show();
    }

    private static void onPrinterDifferentFont(int fontNum) {
        final String stringtext1 = "VIA - Establecimento / Pos=69000004\ncbpj:00.000.000/000-00\nMENTAGN TBL F";
        final String stringtext2 = "Alameda Grajau, 219\nBarueri  -  SP\n00000000000003  DOC=305262  AUT=2000053";
        final Map<String, Integer> maptext1 = new HashMap<String, Integer>();
        maptext1.put(MiniPrinterManager.KEY_LINESPACE, 10);
        maptext1.put(MiniPrinterManager.KEY_TEXTSIZE, 26);
        maptext1.put(MiniPrinterManager.KEY_TYPEFACE, fontNum);
        maptext1.put(MiniPrinterManager.KEY_ALIGN, 1);
        maptext1.put(MiniPrinterManager.KEY_MARGINLEFT, 0);
        maptext1.put(MiniPrinterManager.KEY_MARGINRIGHT, 0);
        String diffStr = "test";
        switch (fontNum) {
            case 0:
                diffStr = "Normal fonts,size is 26";
                break;
            case 1:
                diffStr = "Bold fonts,size is 26";
                break;
            case 2:
                diffStr = "Serial fonts,size is 26";
                break;
            case 3:
                diffStr = "Arial fonts,size is 26";
                break;
            case 4:
                diffStr = "FangHei fonts,size is 26";
                break;
            case 5:
                diffStr = "FranklinGothic fonts,size is 26";
                break;
            case 6:
                diffStr = "Haettenschweiler fonts,size is 26";
                break;
            case 7:
                diffStr = "HuaSong fonts,size is 26";
                break;
            case 8:
                diffStr = "SansMono fonts,size is 26";
                break;
            default:
                break;

        }
        final String myDiffStr = diffStr;

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {

                    sPrinterManager.printTextWithAttributes(stringtext1,
                            maptext1);
                    sPrinterManager
                            .printTextWithAttributes(myDiffStr, maptext1);
                    sPrinterManager.printTextWithAttributes("China",
                            maptext1);
                    sPrinterManager.printTextWithAttributes("�������������ʽ",
                            maptext1);
                    sPrinterManager.printText("\n\n\n");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void showVersion(final Activity activity) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    String bootloader = sPrinterManager.getBootloaderVersion();
                    String firmware = sPrinterManager.getFirmwareVersion();
                    final String content = "Bootloader:" + bootloader
                            + " Firmware:" + firmware;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, content, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void upgrade() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sPrinterManager.upgradePrinter();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void paperDetect(final Activity activity) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    final boolean hasPaper = sPrinterManager.printerPaper();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = hasPaper ? "HAS PAPER"
                                    : "NO PAPER";
                            Toast.makeText(activity, message, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void temperatureDetect(final Activity activity) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    int temperature = sPrinterManager
                            .printerTemperature(activity);
                    String content = "temperature normal";
                    if (temperature == 1) {
                        content = "temperature normal";
                        android.util.Log.i("tyj", "temperature is normal ");
                    } else {
                        content = "temperature abnormal";
                        android.util.Log.i("tyj", "temperature is abnormal ");
                    }
                    android.util.Log.i("tyj", "temperature value = "
                            + temperature);
                    final String tempContent = content;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, tempContent,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
