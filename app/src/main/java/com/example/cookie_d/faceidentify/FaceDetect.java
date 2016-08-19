package com.example.cookie_d.faceidentify;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Cookie_D on 2016/3/18.
 */
public class FaceDetect {
    public interface CallBack {//用于返回数据的处理
        void success(JSONObject result);
        void error(FaceppParseException exception);
    }

    //方法的返回值不一，正确的时候返回JSON字符串，错误时返回错误信息
    public static void detect(final Bitmap bm, final CallBack callBack) {//使用匿名内部类，参数设为final
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建请求，request
                    HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET, true, true);//isCN,isDebug
                    Bitmap bmSmall = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight());//图片转化为字节数组
                    //创建ByteArray输出流将Bitmap输出其中
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG,80, stream);//将bitmap压缩到stream中
                    byte[] arrays = stream.toByteArray();
                    PostParameters params = new PostParameters();
                    params.setImg(arrays);//将字节数组封装到Parameters中
                    JSONObject jsonObject = requests.detectionDetect(params);//至此已返回了JSON数据
                    //返回之前先打一个log
                    Log.e("TAG", jsonObject.toString());
                    if (callBack != null) {
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callBack != null) {
                        callBack.error(e);
                    }
                }

            }
        }).start();
    }
    public static void vertify(final String name,final String faceid,final CallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET, true, true);
                PostParameters params = new PostParameters();
                params.setPersonName(name);
                params.setFaceId(faceid);
                try {
                    JSONObject jsonObject = requests.recognitionVerify(params);
                    Log.e("TAG", jsonObject.toString());
                    if (callBack != null) {
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callBack != null) {
                        callBack.error(e);
                    }
                }
            }
        }).start();
    }
    public static void trainvertify(final String name,final String faceid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET, true, true);
                PostParameters params = new PostParameters();
                params.setPersonName(name);
                params.setFaceId(faceid);
                try {
                    JSONObject jsonObject=requests.personAddFace(params);
                    Log.e("TAG", jsonObject.toString());
                    JSONObject jsonObject1=requests.trainVerify(params);
                    Log.e("TAG", jsonObject1.toString());
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    public static void createperson(final String _name, final String faceid,final CallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET, true, true);
                PostParameters params = new PostParameters();
                params.setPersonName(_name);
                params.setFaceId(faceid);
                try {
                    JSONObject jsonObject = requests.personCreate(params);
                    Log.e("TAG", jsonObject.toString());
                    if (callBack != null) {
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callBack != null) {
                        callBack.error(e);
                    }
                }
            }
        }).start();

    }
}
