
package com.shu.messystem;
import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class QRCodeScanFragement extends Fragment implements OnClickListener{
	private Activity parentActivity;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.qrcodescan_fragement, container, false);
		parentActivity = getActivity();// 获取Acticity
		ImageView scan2DCode=(ImageView) rootView.findViewById(R.id.scan_2dcode);
		scan2DCode.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		startActivity(new Intent(parentActivity,CaptureActivity.class));
	}
}