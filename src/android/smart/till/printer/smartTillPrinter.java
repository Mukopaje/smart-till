package smart.till.printer;

import android.content.Context;
import android.content.Intent;
   
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
   
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.printApis.Mini.MiniPrinterManager;
import com.android.printApis.Utils;

/**
 * This class echoes a string called from JavaScript.
 */
public class smartTillPrinter extends CordovaPlugin {

    CordovaInterface cordovaInt;
    public String ReceiptContent;
    private Context context = null;
    private Context tContext = null;
    
    public static final String TAG = "SmartTill";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("printString")) {
            ReceiptContent = data.getString(0);
			 new PrintReceipt().execute();
            return true;
        }
        // } else if (action.equals("printBarCode")) {
        // 	BarcodeData = data.getString(0);
		// 	 new PrintBarcode().execute();
        // return true;
        // } else if (action.equals("printQRCode")) {
        // 	QRCodeData = data.getString(0);
		// 	 new PrintQRCode().execute();
        // return true;
        // }
        return false;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        tContext = webView.getContext(); 
        context = this.cordova.getActivity().getApplicationContext();    
        MiniPrinterManager.init(context);
        // Toast.makeText(webView.getContext(), usbPrManger, Toast.LENGTH_LONG).show();
        // Log.d(TAG, "Initialization - "+ usbPrManger);
        	
    }

    public class PrintReceipt extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute(){			
				Toast.makeText(webView.getContext(), "Printing Please Wait... ", Toast.LENGTH_LONG).show();
		};
		
		@Override
		protected String doInBackground(Void... params){
			byte[] printContent1 = null;
			String s1 = null;
			try {
				printContent1 = strToByteArray(ReceiptContent,"UTF-8");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}
			
			try {			
                System.out.println("content print started");
                MiniPrinterManager.sendRAWData(printContent1);
				System.out.println("content print finished");						
			}catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "Exception error ="+e);
				s1 = e.toString();
				}
			
			return s1;	
		}
    }
    
    public static byte[] strToByteArray(String str){
        if (str == null) {
            return null;
        }       
        byte[] byteArray = str.getBytes();       
        return byteArray;
    }
    
    public static byte[] strToByteArray(String str,String encodeStr) throws UnsupportedEncodingException  {
        if (str == null) {
            return null;
        }
        byte[] byteArray = null;
        if(encodeStr.equals("IBM852")){
        	byteArray = str.getBytes("IBM852");
        }else if (encodeStr.equals("GB2312")) {
        	byteArray = str.getBytes("GB2312");
		}else if (encodeStr.equals("ISO-8859-1")) {
        	byteArray = str.getBytes("ISO-8859-1");
		}else if (encodeStr.equals("UTF-8")) {
        	byteArray = str.getBytes("UTF-8");
		}else {
			byteArray = str.getBytes();
		}        
        return byteArray;
    }
    
    public static String checkEncoding(String str){
        if (str == null) {
            return null;
        }
        String encodestr =null;
        if(str.equals("1B7430")){
        	encodestr = "UTF-8";
        }else if(str.equals("1B7431")) {
        	encodestr = "GB2312";
		}else if(str.equals("1B7412")) {
        	encodestr = "IBM852";
		}
		else if(str.equals("1B7417")) {
        	encodestr = "ISO-8859-1";
		}else {
			encodestr = "UTF-8";
		}
        return encodestr;
    }

    public static byte[] hexToBytes(String bcdStr) {
        byte[] retValue = new byte[(int) (bcdStr.length() / 2)];
        int k = 0;
        for (int i = 0; i < bcdStr.length(); i += 2) {



            retValue[(int) (i / 2)] = (byte) Integer.parseInt(bcdStr.substring(k, i + 2), 16);
            k += 2;
        }
        return retValue;
    }

    @Override
    protected void onStop() {
        super.onStop();
        MiniPrinterManager.PrinterStop();
        Log.d(TAG, "On Stop has fired: " + 'Service has stoped');
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MiniPrinterManager.PrinterStop();
        Log.d(TAG, "On Destroy has fired: " + 'Service was destroyed');
    }
}
