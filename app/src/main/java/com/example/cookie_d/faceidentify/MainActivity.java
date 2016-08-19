package com.example.cookie_d.faceidentify;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    private Paint mPaint;
    private ImageView mPhoto;
    private Button mStartPhoto;
    private Bitmap mPhotoImg;
    private String firstfaceid;
    private String faceid_1;
    private static final int CATCH_PHOTO_1 = 0x001;
    private static final int CATCH_PHOTO_2 = 0x002;
    private static final int MSG_SUCCESS_FIRST = 0x111;
    private static final int MSG_ERROR_FIRST = 0x112;
    private static final int MSG_SUCCESS_LATER=0x113;
    private static final int MSG_ERROR_LATER=0x114;
    private static final int MSG_SUCCESS_VERTIFY = 0x115;
    private static final int MSG_ERROR_VERTIFY= 0x116;
    private static final int MSG_SUCCESS_CREATE= 0x117;
    private static final int MSG_ERROR_CREATE= 0x118;
    private String imageFilePath;
    private View mWaiting;
    private File checksetting;
    private String name;
    private Button pick_suc_button;
    private ImageView pick_suc_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaint=new Paint();
        checksetting =new File("/sdcard/FaceIdentify/setting.txt");
        if(!checksetting.exists()){
            new  AlertDialog.Builder(this)
                    .setTitle("使用说明" )
                    .setMessage("●本软件使用人脸识别技术来完成身份验证功能\r\n●首次使用将进行初次采相并进行保存，采相成功后软件将退出\r\n●下次开启软件时将直接进入人脸验证阶段\r\n●重置该软件请删除SD根目录下的“FaceIdentify”文件夹\r\n\r\n\r\n\r\n\r\nPowered By Cookie_D and Freegle@SEU\r\nCloud Computing by Face++" )
                    .setPositiveButton("确定" ,  null )
                    .show();
            firstopen();
        }
        else{
            try {
                BufferedReader br=new BufferedReader(new FileReader(checksetting));
                name=br.readLine();
                br.close();
                vertifyphoto();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    private void vertifyphoto() {

        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Log.e("TAG",
                    "SD card is not avaiable/writeable right now.");
            return;
        }
        imageFilePath = "/sdcard/FaceIdentify/"+name+ ".jpg";
        File imageFile = new File(imageFilePath);
        Uri imageFileUri = Uri.fromFile(imageFile);
        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
        intent.putExtra("camerasensortype", 2); // 调用前置摄像头,必须是安卓原生系统
        intent.putExtra("fullScreen", false); // 全屏
        intent.putExtra("showActionIcons", false);
        startActivityForResult(intent, CATCH_PHOTO_2);
    }

    private void waitting(){
        setContentView(R.layout.waitting);
        mWaiting=(View)findViewById(R.id.id_waiting);
        mWaiting.setVisibility(View.VISIBLE);
    }
    private void firstopen(){
        setContentView(R.layout.firstopen);
        mStartPhoto= (Button) findViewById(R.id.id_StartPhoto);
        mPhoto= (ImageView) findViewById(R.id.id_Photo);
        mStartPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catchPhoto();
            }
        });

    }
    private void catchPhoto() {

        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Log.e("TAG",
                    "SD card is not avaiable/writeable right now.");
            return;
        }
        name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+"";
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();//显示照片名字
        File file = new File("/sdcard/FaceIdentify/");
        if(!file.exists())
            file.mkdirs();// 不存在则创建文件夹
        imageFilePath = "/sdcard/FaceIdentify/"+name+ ".jpg";//图片地址
        File imageFile = new File(imageFilePath);
        Uri imageFileUri = Uri.fromFile(imageFile);

        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
        intent.putExtra("camerasensortype", 2); // 调用前置摄像头,必须是安卓原生系统
        intent.putExtra("fullScreen", false); // 全屏
        intent.putExtra("showActionIcons", false);
        startActivityForResult(intent, CATCH_PHOTO_1);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATCH_PHOTO_1) {
            waitting();
            if (imageFilePath != null && !imageFilePath.trim().equals("")) {//当前图片路径不为空，即点击了选择图片
                resizePhoto();
                FaceDetect.detect(mPhotoImg, new FaceDetect.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCCESS_FIRST;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR_FIRST;
                        msg.obj = exception.getErrorMessage();
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
        if(requestCode==CATCH_PHOTO_2){

            waitting();
            if (imageFilePath != null && !imageFilePath.trim().equals("")) {//当前图片路径不为空，即点击了选择图片
                resizePhoto();
                FaceDetect.detect(mPhotoImg, new FaceDetect.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCCESS_LATER;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR_LATER;
                        msg.obj = exception.getErrorMessage();
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }
    }
    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();//内含图片高宽等信息
        options.inJustDecodeBounds = true;//不加载图片只获取图片尺寸,用options存下来
        BitmapFactory.decodeFile(imageFilePath, options);
        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);//缩放比例的获取
        //可通过更改1024f来改变压缩比例值
        options.inSampleSize = (int) Math.ceil(ratio);//进行压缩
        options.inJustDecodeBounds = false;//加载图片
        mPhotoImg = BitmapFactory.decodeFile(imageFilePath, options);
    }
    private void picksuccess(){
        setContentView(R.layout.successpage);
        pick_suc_button=(Button)findViewById(R.id.id_FinishCapture);
        pick_suc_view=(ImageView)findViewById(R.id.id_SuccessPic);
        pick_suc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    private void prepareRsBitmap(JSONObject rs) {
        Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(), mPhotoImg.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mPhotoImg, 0, 0, null);//重新把原图画上canvas,原图画在bitmap上
        try {
            JSONArray faces = rs.getJSONArray("face");//获取的JSON文件中可能根据图片识别出多个face
            int faceCount = faces.length();//计算几张脸
            for (int i = 0; i < faceCount; i++) {
                //获取单独face对象
                JSONObject face = faces.getJSONObject(i);
                //解析属性
                //取center x和y 以及height和width，这些都在Position中
                JSONObject posObj = face.getJSONObject("position");
                //x,y均为相对图片高宽的百分比
                float x = (float) posObj.getJSONObject("center").getDouble("x");
                float y = (float) posObj.getJSONObject("center").getDouble("y");
                float w = (float) posObj.getDouble("width");
                float h = (float) posObj.getDouble("height");

                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();

                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();


                mPaint.setColor(0xffffffff);
                mPaint.setStrokeWidth(3);

                //画出脸部框
                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x + w / 2, y - h / 2, x + w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2, mPaint);

                mPhotoImg = bitmap;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void nofaceorfail(){
        setContentView(R.layout.varifiedpage);

    }



    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS_FIRST:
                    mWaiting.setVisibility(View.GONE);
                    JSONObject rs = (JSONObject) msg.obj;
                    JSONArray faces = null;//获取的JSON文件中可能根据图片识别出多个face
                    firstfaceid=null;
                    try {
                        faces = rs.getJSONArray("face");
                        firstfaceid=faces.getJSONObject(0).getString("face_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int faceCount = faces.length();
                    if(faceCount!=1){
                        mWaiting.setVisibility(View.GONE);
                        firstopen();
                        TextView tv=(TextView)findViewById(R.id.textView);
                        tv.setText("未检测到脸部\n请重新拍摄");
                        mPhoto.setImageResource(R.drawable.fail);
                        mStartPhoto.setText("重 新 拍 摄");
                    }
                    else if(faceCount==1){
                        try {
                            checksetting.createNewFile();
                            BufferedWriter br=new BufferedWriter(new FileWriter(checksetting));
                            br.write(name);
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        picksuccess();
                        prepareRsBitmap(rs);
                        pick_suc_view.setImageBitmap(mPhotoImg);
                        FaceDetect.createperson(name, firstfaceid, new FaceDetect.CallBack() {
                            @Override
                            public void success(JSONObject result) {
                                Message msg = Message.obtain();
                                msg.what = MSG_SUCCESS_CREATE;
                                msg.obj = result;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void error(FaceppParseException exception) {

                                Message msg = Message.obtain();
                                msg.what = MSG_ERROR_CREATE;
                                msg.obj = exception;
                                mHandler.sendMessage(msg);
                            }
                        });
                        FaceDetect.trainvertify(name,firstfaceid);
                    }
                    break;
                case MSG_ERROR_FIRST:
                    break;
                case MSG_SUCCESS_LATER:
                    mWaiting.setVisibility(View.GONE);
                    JSONObject rs_1 = (JSONObject) msg.obj;
                    JSONArray faces_1 = null;//获取的JSON文件中可能根据图片识别出多个face
                    try {
                        faces_1 = rs_1.getJSONArray("face");
                        faceid_1=faces_1.getJSONObject(0).getString("face_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int faceCount_1 = faces_1.length();//计算几张脸
                    if(faceCount_1!=1){
                        nofaceorfail();
                        ImageView iv=(ImageView)findViewById(R.id.id_result);
                        iv.setImageResource(R.drawable.fail);
                        TextView tv_1=(TextView)findViewById(R.id.vertify_textView);
                        tv_1.setText("未检测到脸部\n请重新验证");
                        Button bt=(Button)findViewById(R.id.id_varifiedButton);
                        bt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                vertifyphoto();
                            }
                        });
                    }
                    else if(faceCount_1==1){
                        FaceDetect.vertify(name, faceid_1, new FaceDetect.CallBack() {
                            @Override
                            public void success(JSONObject result) {
                                Message msg = Message.obtain();
                                msg.what = MSG_SUCCESS_VERTIFY;
                                msg.obj = result;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void error(FaceppParseException exception) {
                                Message msg = Message.obtain();
                                msg.what = MSG_ERROR_VERTIFY;
                                msg.obj = exception;
                                mHandler.sendMessage(msg);

                            }
                        });
                    }
                    break;
                case MSG_ERROR_LATER:
                    break;
                case MSG_SUCCESS_VERTIFY:
                    JSONObject rs_2 = (JSONObject) msg.obj;
                    try {
                        String temp=rs_2.getString("is_same_person");
                        if(temp.equals("true")){
                            nofaceorfail();
                            ImageView iv=(ImageView)findViewById(R.id.id_result);
                            iv.setImageResource(R.drawable.success);
                            TextView tv_1=(TextView)findViewById(R.id.vertify_textView);
                            tv_1.setText("验证成功");
                            Button bt=(Button)findViewById(R.id.id_varifiedButton);
                            bt.setText("完 成");
                            bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    System.exit(0);
                                }
                            });
                            FaceDetect.trainvertify(name, faceid_1);
                        }
                        else{
                            nofaceorfail();
                            ImageView iv=(ImageView)findViewById(R.id.id_result);
                            iv.setImageResource(R.drawable.fail);
                            TextView tv_1=(TextView)findViewById(R.id.vertify_textView);
                            tv_1.setText("验证失败\n请重新验证");
                            Button bt1=(Button)findViewById(R.id.id_varifiedButton);
                            bt1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vertifyphoto();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case MSG_ERROR_VERTIFY:
                    break;
                case MSG_SUCCESS_CREATE:
                    FaceDetect.trainvertify(name,firstfaceid);
                    break;
                case MSG_ERROR_CREATE:
                    break;
            }

            super.handleMessage(msg);
        }
    };
}
