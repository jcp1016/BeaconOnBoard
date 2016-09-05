package com.Sheng.qiansheng.full_bob;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNS;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import android.graphics.Matrix;

/**
 * Created by qiansheng on 7/18/16.
 */
public class RealTime extends Activity {
    TransferUtility transferUtility = null;
    AmazonS3 s3Client = MainActivity.clientManager.s3();
    AmazonSNS snsClient = MainActivity.clientManager.sns();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public static String Device_ID = "first";
    public static long start_time = -1;
    public static long prev_start_time = -100;
    public static float temperature = -1;
    private TextView text_device =null;
    private TextView text_time =null;
    private TextView text_temperature=null;
    private TextView text_elapsed =null;
    private Handler mHandler = new Handler();
    public static String device_string=null;
    private String button_text = "Deactivate";
    private int current_status = 0;
    private int can_downloaded = 0;
    private int upload_called = 0;
    private Thread t = null;
    private Thread check_finished = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.real_time);
        System.out.println("really");
        System.out.println(s3Client);
        text_time = (TextView) findViewById(com.Sheng.qiansheng.full_bob.R.id.textView_Time);
        text_temperature = (TextView) findViewById(com.Sheng.qiansheng.full_bob.R.id.textView_temperature);
        text_elapsed = (TextView) findViewById(R.id.textView_Status);
        text_device = (TextView) findViewById(com.Sheng.qiansheng.full_bob.R.id.textView_device);

        transferUtility = new TransferUtility(s3Client, getApplicationContext());
        t = new Thread() {
            @Override
            public void run() {
                try {
                    new TextTask().execute();
                    while (!isInterrupted()) {
                        Thread.sleep(20000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                System.out.println("========change once=========");
                                new TextTask().execute();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        check_finished = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                if(upload_called==1 && can_downloaded==0 ) {
                                    System.out.println("========change once=========");
                                    new CheckFinishedTask().execute();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        check_finished.start();
        Button button_view = (Button) findViewById(R.id.button_view);
        assert button_view != null;
        button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(can_downloaded==1) {
                    ImageView image = (ImageView) findViewById(com.Sheng.qiansheng.full_bob.R.id.imageView2);
                    new S3Task(image).execute();
                }
            }
        });
        final Button button_change_status = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_download);
        assert button_change_status != null;
        button_change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_text = button_change_status.getText().toString();
                if(!Device_ID.equals("first")) {
                    System.out.println("current_status");
                    System.out.println(current_status);
                    new UploadTask().execute();

                }

            }
        });

        Button button_logout = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_logout);
        final Intent act_main = new Intent(this,MainActivity.class);
        assert button_logout != null;

        View.OnClickListener logout_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_finished.interrupt();
                t.interrupt();
                start_time = -1;
                prev_start_time = -100;
                temperature = -1;
                MainActivity.my_account=null;
                startActivity(act_main);
            }
        };
//        System.out.println(signup_listener);
        button_logout.setOnClickListener(logout_listener);

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                //stuff that updates ui
//                if(start_time>0){
//                    long elapsed_time = (System.currentTimeMillis() - start_time)/1000;
//                    System.out.println("elapsed_time");
//                    System.out.println(elapsed_time);
//                    long second = elapsed_time%60;
//                    elapsed_time = elapsed_time/60;
//                    long minute = elapsed_time%60;
//                    elapsed_time = elapsed_time/60;
//                    long hour = elapsed_time;
//                    String elapsed_string = "Elapsed Time: ";
//                    text_elapsed.setText(elapsed_string.concat(String.format("%1$dh %2$dm %3$ds", hour,minute,second)));
//                }
//
//            }
//        });

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if(start_time>0){
////                    System.out.println("automatic");
//                    long elapsed_time = (System.currentTimeMillis() - start_time)/1000;
////                    System.out.println("elapsed_time");
////                    System.out.println(elapsed_time);
//                    long second = elapsed_time%60;
//                    elapsed_time = elapsed_time/60;
//                    long minute = elapsed_time%60;
//                    elapsed_time = elapsed_time/60;
//                    long hour = elapsed_time;
//                    String elapsed_string = "Elapsed Time: ";
//                    text_elapsed.setText(elapsed_string.concat(String.format("%1$dh %2$dm %3$ds", hour,minute,second)));
//
//                }
//                mHandler.postDelayed(this, 1000);
//                //refresh your textview
//            }
//        };
//        mHandler.post(runnable);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ImagePart Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.Sheng.qiansheng.full_bob/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }
    public void onBackPressed() {
        check_finished.interrupt();
        t.interrupt();
        super.onBackPressed();
    }
    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ImagePart Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.Sheng.qiansheng.full_bob/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    //    private static void FindBindedDevicesWithMultipleThreads(
//            DynamoDBMapper mapper,
//            int numberOfThreads,
//            String email) throws Exception {
//
//        System.out.println("FindBicyclesOfSpecificTypeWithMultipleThreads: Scan ProductCatalog With Multiple Threads.");
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        eav.put(":val1", new AttributeValue().withS("Account_device"));
//        eav.put(":val2", new AttributeValue().withS(email));
//
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("ProductCategory = :val1 and BicycleType = :val2")
//                .withExpressionAttributeValues(eav);
//
//        List<DynamoDB_account_device.Account_Dev> scanResult = mapper.parallelScan(DynamoDB_account_device.Account_Dev.class, scanExpression, numberOfThreads);
//        for (DynamoDB_account_device.Account_Dev account_dev : scanResult) {
//            System.out.println(account_dev);
//        }
//    }
//    private class DynamoDBManagerTask extends
//            AsyncTask<Device_act, Void, String> {
//
//        protected String doInBackground(
//                Device_act... Device_acts) {
//            String DevNum = Device_acts[0].getDevNum();
//            int active = Device_acts[0].getActive();
//            DynamoDB_device_action.insertBind(DevNum,active);
//            current_status = DynamoDB_device_action.getAccount(DevNum).getActive();
//            String result = "change device status";
//            return result;
//        }
//        protected void onPostExecute(String result) {
//            Button button_change_status = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_deactivate);
//            System.out.println(result);
//            if(current_status==0){
//                button_change_status.setText("Activate");
//            }else{
//                button_change_status.setText("Deactivate");
//            }
//        }
//    }
    private class UploadTask extends
            AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(
                Void... Voids) {
            System.out.println("email");
            // Use to mark whether the content is changed
            boolean result=true;
            AmazonDynamoDBClient ddb = MainActivity.clientManager.ddb();
            HashMap filter = new HashMap();
            Condition hashKeyCondition = new Condition().withComparisonOperator(
                    ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(MainActivity.my_account));
            filter.put("account",hashKeyCondition);
            ScanRequest scanRequest = new ScanRequest("Account_device").withScanFilter(filter);
            ScanResult scanResult = ddb.scan(scanRequest);
            System.out.println(scanResult);
            //QueryRequest queryRequest = new QueryRequest().withTableName("Account_device").withKeyConditions(filter);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            System.out.println(items.size());
            DynamoDB_dev_real.Dev_real_Inside correctOne2 = null;
            if(items.size()==0){
                Device_ID = "first";
                result=false;
            }else{
                System.out.println("Device_ID");
                Device_ID = items.get(0).get("deviceID").toString();
                Device_ID = Device_ID.substring(4,Device_ID.length()-2);
                device_string = Device_ID;
                System.out.println(Device_ID);
                correctOne2 = DynamoDB_dev_real.getAccount(Device_ID);
//                if(correctOne2==null){
//                    correctOne2.setDevNum(Device_ID);
//                    correctOne2.setFinished(0);
//                    correctOne2.setUpload(1);
//                }else{
//                    correctOne2.setUpload(1);
//
//                }
                DynamoDB_dev_real.insertBind(correctOne2.getDevNum(),1,0);
                result = true;
            }
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                String elapsed_string = "Status: ";
                System.out.println("========call upload==========");
                text_elapsed.setText(elapsed_string.concat("Loading ..."));
                can_downloaded=0;
                upload_called=1;

            }
        }

    }
    private class CheckFinishedTask extends
            AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(
                Void... Voids) {
            System.out.println("email");
            // Use to mark whether the content is changed
            boolean result=false;
            AmazonDynamoDBClient ddb = MainActivity.clientManager.ddb();
            HashMap filter = new HashMap();
            Condition hashKeyCondition = new Condition().withComparisonOperator(
                    ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(MainActivity.my_account));
            filter.put("account",hashKeyCondition);
            ScanRequest scanRequest = new ScanRequest("Account_device").withScanFilter(filter);
            ScanResult scanResult = ddb.scan(scanRequest);
            System.out.println(scanResult);
            //QueryRequest queryRequest = new QueryRequest().withTableName("Account_device").withKeyConditions(filter);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            System.out.println(items.size());
            DynamoDB_dev_real.Dev_real_Inside correctOne2 = null;
            if(items.size()==0){
                Device_ID = "first";

            }else{
                System.out.println("Device_ID");
                Device_ID = items.get(0).get("deviceID").toString();
                Device_ID = Device_ID.substring(4,Device_ID.length()-2);
                device_string = Device_ID;
                System.out.println(Device_ID);
                correctOne2 = DynamoDB_dev_real.getAccount(Device_ID);
                if(correctOne2==null){

                }else{
                    if(correctOne2.getFinished()==1){
                        result = true;
                        can_downloaded = 1;
                        upload_called = 0;
                    }

                }

            }
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                String elapsed_string = "Status: ";
                System.out.println("========call upload==========");
                text_elapsed.setText(elapsed_string.concat("Ready"));
            }
        }

    }
    private class TextTask extends
            AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(
                Void... Voids) {
            System.out.println("email");
            // Use to mark whether the content is changed
            boolean result=true;
            AmazonDynamoDBClient ddb = MainActivity.clientManager.ddb();
            HashMap filter = new HashMap();
            Condition hashKeyCondition = new Condition().withComparisonOperator(
                    ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(MainActivity.my_account));
            filter.put("account",hashKeyCondition);
            ScanRequest scanRequest = new ScanRequest("Account_device").withScanFilter(filter);
            ScanResult scanResult = ddb.scan(scanRequest);
            System.out.println(scanResult);
            //QueryRequest queryRequest = new QueryRequest().withTableName("Account_device").withKeyConditions(filter);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            System.out.println(items.size());
            DynamoDB_car_real.Car_real_Inside correctOne2 = null;
            if(items.size()==0){
                Device_ID = "first";
                result=false;
            }else{
                System.out.println("Device_ID");
                Device_ID = items.get(0).get("deviceID").toString();
                Device_ID = Device_ID.substring(4,Device_ID.length()-2);
                device_string = Device_ID;
                System.out.println(Device_ID);
                correctOne2 = DynamoDB_car_real.getAccount(Device_ID);
                if(correctOne2==null){
                    return false;
                }
                start_time = correctOne2.getStart_time();
//                System.out.println(prev_start_time);
//                System.out.println("prev_start_time");
                temperature = correctOne2.getTemperature();
//                if(start_time!=prev_start_time){
//                    prev_start_time=start_time;
//                    result=true;
//                }
                System.out.println(start_time);
                System.out.println("start_time");
                System.out.println(temperature);
                System.out.println(correctOne2.getTemperature());
            }
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                String time_string = "Time: ";
                String temperature_string = "Temperature: ";
                String elapsed_string = "Status: ";
                String device_first_part_string= "Device: ";
//                int second = (int) (start_time%100);
//                start_time=start_time/100;
//                int minute = (int) (start_time%100);
//                start_time=start_time/100;
//                int hour = (int) (start_time%100);
//                start_time=start_time/100;
//                int day = (int) (start_time%100);
//                start_time=start_time/100;
//                int month = (int) (start_time%100);
//                start_time=start_time/100;
//                int year = (int) (start_time);
                Date date = new Date(start_time);
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                String reportDate = df.format(date);
                System.out.println(date);
                System.out.println(start_time);
                System.out.println(System.currentTimeMillis());
                long elapsed_time = (System.currentTimeMillis() - start_time)/1000;
                System.out.println("elapsed_time");
                System.out.println(elapsed_time);
                long second = elapsed_time%60;
                elapsed_time = elapsed_time/60;
                long minute = elapsed_time%60;
                elapsed_time = elapsed_time/60;
                long hour = elapsed_time;
                if(text_time!=null) {
                    //text_time.setText(time_string.concat(String.format("%1$d-%2$d-%3$d %4$d:%5$d:%6$d", year, month, day, hour,minute,second)));
                    text_device.setText(device_first_part_string.concat(device_string));
                    text_time.setText(time_string.concat(reportDate));
                    temperature_string = temperature_string.concat(Float.toString((float) (temperature/10*1.8+32)));
                    text_temperature.setText(temperature_string.concat("\u2109"));

                    System.out.println("current time");
                    System.out.println(System.currentTimeMillis());
                }
            }
        }
    }
    private class S3Task extends
            AsyncTask<Void, Void, Boolean> {
        private final WeakReference<ImageView> imageViewReference;

        private S3Task(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        @Override
        protected Boolean doInBackground(
                Void... Voids) {
            System.out.println("email");
            // Use to mark whether the content is changed
            boolean result=false;
            AmazonDynamoDBClient ddb = MainActivity.clientManager.ddb();
            HashMap filter = new HashMap();
            Condition hashKeyCondition = new Condition().withComparisonOperator(
                    ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(MainActivity.my_account));
            filter.put("account",hashKeyCondition);
            ScanRequest scanRequest = new ScanRequest("Account_device").withScanFilter(filter);
            ScanResult scanResult = ddb.scan(scanRequest);
            System.out.println(scanResult);
            //QueryRequest queryRequest = new QueryRequest().withTableName("Account_device").withKeyConditions(filter);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            System.out.println(items.size());
            DynamoDB_car_situation.Car_Inside correctOne2 = null;
            if(items.size()==0){
                Device_ID = "first";
            }else{
                Device_ID = items.get(0).get("deviceID").toString();
                Device_ID = Device_ID.substring(4,Device_ID.length()-2);
                device_string = Device_ID;
                System.out.println(Device_ID);
                correctOne2 = DynamoDB_car_situation.getAccount(Device_ID);
                result = true;
                System.out.println(start_time);
                System.out.println("start_time");
                System.out.println(temperature);
                System.out.println(correctOne2.getTemperature());
                if(device_string!=null){
                    DynamoDB_dev_real.Dev_real_Inside correctOne3 = DynamoDB_dev_real.getAccount(device_string);
                    correctOne3.setFinished(0);
                    DynamoDB_dev_real.updateBind(correctOne3);
                }
            }
            if(result) {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File file = new File(directory, "realtime_downloaded.png");

                System.out.println("file");
                System.out.println(file);
                System.out.println(transferUtility);
                System.out.println("==========Device_ID===========");
                System.out.println(Device_ID);
                S3Object object = s3Client.getObject(new GetObjectRequest("bobotry", Device_ID.concat("/realtime_inside.png")));
                InputStream reader = new BufferedInputStream(
                        object.getObjectContent());
                OutputStream writer = null;
                try {
                    writer = new BufferedOutputStream(new FileOutputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int read = -1;

                try {
                    while ((read = reader.read()) != -1) {
                        writer.write(read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    writer.flush();
                    writer.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }



//            TransferObserver observer = transferUtility.download(
//                    "bobotry",     /* The bucket to download from */
//                    //"first/Baby_Face.JPG",    /* The key for the object to download */
//                    "first/testimage.jpg",
//                    file        /* The file to download the object to */
//            );
                System.out.println(file.length());
            }
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            File file = new File("/data/user/0/com.Sheng.qiansheng.full_bob/app_imageDir/realtime_downloaded.png");
            System.out.println("file size:");
            System.out.println(file.length());
            if (imageViewReference != null && file.length()!=0) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    Bitmap bMap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    int width = 0;
                    width = bMap.getWidth();
                    int height = bMap.getHeight();
                    float ratio = ((float)imageView.getMeasuredWidth())/width;
                    Matrix matrix = new Matrix();
                    matrix.postScale(ratio, ratio);
                    Bitmap scaledBitmap = Bitmap.createBitmap(bMap, 0, 0, width, height, matrix, true);
                    System.out.println(scaledBitmap);
                    imageView.setImageBitmap(scaledBitmap);
                }
            }
            String elapsed_string = "Status: ";
            text_elapsed.setText(elapsed_string);
            can_downloaded = 0;


        }
    }
}
