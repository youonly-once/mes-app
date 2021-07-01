package com.shu.messystem.component;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by 01405645 on 2018-05-11.
 */

public class ChoosePicture {
    //返回码，相机
    private static final int RESULT_CAMERA=200;
    //返回码，相册
    private static final int RESULT_ALBUM=100;
    //拍照后照片的Uri
    private Uri imageUri;
    private static final int CROP_PICTURE = 2;//裁剪后图片返回码
    //裁剪图片存放地址的Uri
    private Uri cropImageUri;
    private Activity mActivity;
    public ChoosePicture(Activity mActivity){
        this.mActivity=mActivity;
    }
    //相机拍照后存放的文件
    private File outputImage;
    public void choosePicture(){
        AlertDialog.Builder builder=new AlertDialog.Builder(mActivity);
        builder.setItems(new String[]{"本地相册","相机拍照"},new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //检测 是否有打开相机权限 有则打开 没有则申请后打开
                        checkAlbumGranted();
                        break;
                    case 1:
                        //打开相机
                        openCamera();
                        break;
                }
            }
        });
        builder.create().show();

    }
    private void checkAlbumGranted() {
        //无权限申请
        if(ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            openAlbum();
        }
    }
    public void openAlbum() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        mActivity.startActivityForResult(intent,RESULT_ALBUM);

    }
    private void openCamera() {
//先验证手机是否有sdcard
        String status= Environment.getExternalStorageState();
        if(status.equals(Environment.MEDIA_MOUNTED))
        {
            //创建File对象，用于存储拍照后的照片
             outputImage=new File(mActivity.getExternalCacheDir(),"out_image.jpg");//SD卡的应用关联缓存目录
            try {
                if(outputImage.exists()){
                    outputImage.delete();
                }
                outputImage.createNewFile();
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//android 7.0以后
                    imageUri= FileProvider.getUriForFile(mActivity,
                            "com.shu.messystem.fileprovider",outputImage);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                }else{
                    imageUri= Uri.fromFile(outputImage);
                }
                //启动相机程序
                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                mActivity.startActivityForResult(intent,RESULT_CAMERA);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Toast.makeText(mActivity, "没有找到储存目录",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(mActivity, "没有储存卡",Toast.LENGTH_LONG).show();
        }
    }
    @SuppressLint("NewApi")
    public String handlerImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(mActivity,uri)){
            //如果是document类型的Uri,则通过document id处理
            String docId= DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];//解析出数字格式的id
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的URI，则使用普通方式处理
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath=uri.getPath();
        }
        Log.e("imagePath",imagePath);
        return imagePath;
      //  startPhotoZoom();
    }
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = mActivity.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    public void handlerImageBeforeKitKat(Intent data){
        Uri cropUri=data.getData();
        startPhotoZoom();
    }


    public void startPhotoZoom() {
        File CropPhoto=new File(mActivity.getExternalCacheDir(),"crop_image.jpg");
        try{
            if(CropPhoto.exists()){
                if(!CropPhoto.delete()){//文件删除失败
                    Toast.makeText(mActivity,"图片剪辑失败",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if(!CropPhoto.createNewFile()){
                Toast.makeText(mActivity,"图片剪辑失败",Toast.LENGTH_SHORT).show();
                return;
            }
        }catch(IOException e){
            Toast.makeText(mActivity,"图片剪辑失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        cropImageUri=Uri.fromFile(CropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        mActivity.startActivityForResult(intent, CROP_PICTURE);
    }
    public Uri getImageUri(){
        return cropImageUri;
    }
    public File getOutputImage(){
        return outputImage;
    }
}
