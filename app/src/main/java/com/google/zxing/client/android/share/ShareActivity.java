/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.share;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.ContactsContract;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.shu.messystem.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.clipboard.ClipboardInterface;

/**
 * Barcode Scanner can share data like contacts and bookmarks by displaying a QR Code on screen,
 * such that another user can scan the barcode with their phone.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ShareActivity extends Activity {

  private static final String TAG = ShareActivity.class.getSimpleName();

  private static final int PICK_BOOKMARK = 0;
  private static final int PICK_CONTACT = 1;
  private static final int PICK_APP = 2;

  private View clipboardButton;

  private final View.OnClickListener contactListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Log.e("ACCESS","CONTACT");
      requestPower();
    }
  };
  //申请访问联系人权限
  public void requestPower() {
    String grant[]= {Manifest.permission.READ_CONTACTS};

    if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) { //判断是否已经赋予权限

      if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)) { //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
        Toast.makeText(this, "您需要允许才能分享联系人", Toast.LENGTH_LONG).show();
      }

      ActivityCompat.requestPermissions(this,grant, 1);//申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果

    }else{
      Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
      intent.addFlags(Intents.FLAG_NEW_DOC);
      startActivityForResult(intent, PICK_CONTACT);
    }

  }
  //权限申请结果
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //同意权限申请
          Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
          intent.addFlags(Intents.FLAG_NEW_DOC);
          startActivityForResult(intent, PICK_CONTACT);
        }else { //拒绝权限申请
          Toast.makeText(this, "你拒绝后将不能分享联系人。", Toast.LENGTH_LONG).show();
        }
        break;
      default:
        break;
    }
  }

  private final View.OnClickListener bookmarkListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.addFlags(Intents.FLAG_NEW_DOC);
      intent.setClassName(ShareActivity.this, BookmarkPickerActivity.class.getName());
      startActivityForResult(intent, PICK_BOOKMARK);
    }
  };

  private final View.OnClickListener appListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.addFlags(Intents.FLAG_NEW_DOC);
      intent.setClassName(ShareActivity.this, AppPickerActivity.class.getName());
      startActivityForResult(intent, PICK_APP);
    }
  };

  private final View.OnClickListener clipboardListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      // Should always be true, because we grey out the clipboard button in onResume() if it's empty
      CharSequence text = ClipboardInterface.getText(ShareActivity.this);
      if (text != null) {
        launchSearch(text.toString());
      }
    }
  };

  private final View.OnKeyListener textListener = new View.OnKeyListener() {
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
        String text = ((TextView) view).getText().toString();
        if (text != null && !text.isEmpty()) {
          launchSearch(text);
        }
        return true;
      }
      return false;
    }
  };

  private void launchSearch(String text) {
    Intent intent = new Intent(Intents.Encode.ACTION);
    intent.addFlags(Intents.FLAG_NEW_DOC);
    intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
    intent.putExtra(Intents.Encode.DATA, text);
    intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
    startActivity(intent);
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.share);

    findViewById(R.id.share_contact_button).setOnClickListener(contactListener);
    if (Build.VERSION.SDK_INT >= 23) { // Marshmallow / 6.0
      // Can't access bookmarks in 6.0+
      findViewById(R.id.share_bookmark_button).setEnabled(false);
    } else {
      findViewById(R.id.share_bookmark_button).setOnClickListener(bookmarkListener);
    }
    findViewById(R.id.share_app_button).setOnClickListener(appListener);
    clipboardButton = findViewById(R.id.share_clipboard_button);
    clipboardButton.setOnClickListener(clipboardListener);
    findViewById(R.id.share_text_view).setOnKeyListener(textListener);
  }

  @Override
  protected void onResume() {
    super.onResume();
    clipboardButton.setEnabled(ClipboardInterface.hasText(this));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case PICK_BOOKMARK:
        case PICK_APP:
          showTextAsBarcode(intent.getStringExtra("url")); // Browser.BookmarkColumns.URL
          break;
        case PICK_CONTACT:
          // Data field is content://contacts/people/984
          showContactAsBarcode(intent.getData());
          break;
      }
    }
  }

  private void showTextAsBarcode(String text) {
    Log.i(TAG, "Showing text as barcode: " + text);
    if (text == null) {
      return; // Show error?
    }
    Intent intent = new Intent(Intents.Encode.ACTION);
    intent.addFlags(Intents.FLAG_NEW_DOC);
    intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
    intent.putExtra(Intents.Encode.DATA, text);
    intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
    startActivity(intent);
  }

  /**
   * Takes a contact Uri and does the necessary database lookups to retrieve that person's info,
   * then sends an Encode intent to render it as a QR Code.
   *
   * @param contactUri A Uri of the form content://contacts/people/17
   */
  private void showContactAsBarcode(Uri contactUri) {
    Log.i(TAG, "Showing contact URI as barcode: " + contactUri);
    if (contactUri == null) {
      return; // Show error?
    }
    ContentResolver resolver = getContentResolver();

    String id;
    String name;
    boolean hasPhone;
    try (Cursor cursor = resolver.query(contactUri, null, null, null, null)) {
      if (cursor == null || !cursor.moveToFirst()) {
        return;
      }
      id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
      name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
      hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0;
    }

    // Don't require a name to be present, this contact might be just a phone number.
    Bundle bundle = new Bundle();
    if (name != null && !name.isEmpty()) {
      bundle.putString(ContactsContract.Intents.Insert.NAME, massageContactData(name));
    }

    if (hasPhone) {
      try (Cursor phonesCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                null,
                                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + '=' + id,
                                                null,
                                                null)) {
        if (phonesCursor != null) {
          int foundPhone = 0;
          int phonesNumberColumn = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
          int phoneTypeColumn = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
          while (phonesCursor.moveToNext() && foundPhone < Contents.PHONE_KEYS.length) {
            String number = phonesCursor.getString(phonesNumberColumn);
            if (number != null && !number.isEmpty()) {
              bundle.putString(Contents.PHONE_KEYS[foundPhone], massageContactData(number));
            }
            int type = phonesCursor.getInt(phoneTypeColumn);
            bundle.putInt(Contents.PHONE_TYPE_KEYS[foundPhone], type);
            foundPhone++;
          }
        }
      }
    }

    try (Cursor methodsCursor = resolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                               null,
                                               ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + '=' + id,
                                               null,
                                               null)) {
      if (methodsCursor != null && methodsCursor.moveToNext()) {
        String data = methodsCursor.getString(
            methodsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
        if (data != null && !data.isEmpty()) {
          bundle.putString(ContactsContract.Intents.Insert.POSTAL, massageContactData(data));
        }
      }
    }

    try (Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                             null,
                                             ContactsContract.CommonDataKinds.Email.CONTACT_ID + '=' + id,
                                             null,
                                             null)) {
      if (emailCursor != null) {
        int foundEmail = 0;
        int emailColumn = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
        while (emailCursor.moveToNext() && foundEmail < Contents.EMAIL_KEYS.length) {
          String email = emailCursor.getString(emailColumn);
          if (email != null && !email.isEmpty()) {
            bundle.putString(Contents.EMAIL_KEYS[foundEmail], massageContactData(email));
          }
          foundEmail++;
        }
      }
    }

    Intent intent = new Intent(Intents.Encode.ACTION);
    intent.addFlags(Intents.FLAG_NEW_DOC);
    intent.putExtra(Intents.Encode.TYPE, Contents.Type.CONTACT);
    intent.putExtra(Intents.Encode.DATA, bundle);
    intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());

    Log.i(TAG, "Sending bundle for encoding: " + bundle);
    startActivity(intent);
  }

  private static String massageContactData(String data) {
    // For now -- make sure we don't put newlines in shared contact data. It messes up
    // any known encoding of contact data. Replace with space.
    if (data.indexOf('\n') >= 0) {
      data = data.replace("\n", " ");
    }
    if (data.indexOf('\r') >= 0) {
      data = data.replace("\r", " ");
    }
    return data;
  }
}
