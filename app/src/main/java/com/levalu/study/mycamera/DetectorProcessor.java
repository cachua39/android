package com.levalu.study.mycamera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

public class DetectorProcessor extends ActivityCompat implements Detector.Processor<TextBlock> {
    private static final String TAG = "Detector_Processor";
    private Context mContext;
    private ViewGroup mContainer;

    public DetectorProcessor(Context context, ViewGroup container) {
        mContext = context;
        mContainer =  container;
    }

    // Call by the detector to deliver detection results to the processor
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for(int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if(item != null && item.getValue() != null) {
                Log.d(TAG, "Text detected " + item.getValue());
                String checkAlpha = item.getValue();
                if(checkAlpha.matches("[0-9]+")) {
                    String value = "*101*" + checkAlpha + "#";
                    Uri uri = Uri.parse("tel:" + Uri.encode(value));
                    Log.d(TAG, "Text detected " + uri.toString());
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                } else {
                    // Snackbar.make(mContainer, "Scanned text contains alphabet character",Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(mContext,"Scanned text contains alphabet character", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void release() {
        // Shuts down and releases associated processor resources.
    }
}
