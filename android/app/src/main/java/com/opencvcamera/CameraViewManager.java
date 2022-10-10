package com.opencvcamera;

// Facebook (React Native)
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
// Android
import android.util.Log;
// AndroidX
import androidx.annotation.Nullable;
// Java
import java.util.ArrayList;
import java.util.List;
// OpenCV
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;
import org.opencv.core.Scalar;

public class CameraViewManager extends SimpleViewManager<JavaCamera2View> implements CameraBridgeViewBase.CvCameraViewListener2 {
    // React Native
    public static final String REACT_CLASS = "JavaCameraView";
    ReactApplicationContext mCallerContext;
    // Camera Preview
    private JavaCamera2View mCamera;
    private int mCameraID = -1;  // Front: 98, Back: 99, Any: -1
    public static long frameCycle = 0;
    // Mat Images
    private Mat mRgba;
    private Mat mGray;
    private Mat mHierarchy;
    public static Mat captureMat;
    // Image Capture
    public static boolean saveAsBase64 = true;
    // Image Proccessing
    private static boolean mEnableProcessing = false;

    // React Native: Set caller context
    public CameraViewManager(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }
    
    @Override // React Native: Component Name
    public String getName() {
        return REACT_CLASS;
    }

    @Override // Initializes camera preview for react native bridge
    public JavaCamera2View createViewInstance(ThemedReactContext context) { 
        mCamera = new JavaCamera2View(context, mCameraID);
        mCamera.setCvCameraViewListener(this);
        // mCamera.setCameraPermissionGranted(); // Required for OpenCV 4.x.x
        mCamera.enableView();
        return mCamera;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mHierarchy = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mHierarchy.release();
        captureMat.release();
    }

    @Override // Real-time camera frame processing handler
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frameCycle++;
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        if (mEnableProcessing) {
            mRgba.convertTo(mRgba, -1, 5, 0); // Apply a simple filter to the camera preview frame
        }

        captureMat = mRgba.clone(); // Clean slate to be accessed in CameraModule

        return mRgba;
    }

    @ReactProp(name="cameraID")
    public void setCameraID(JavaCamera2View view, @Nullable Integer cameraID) {
        if (cameraID != null && (cameraID == -1 || cameraID == 98 || cameraID == 99)) {
            mCamera.disableView();
            // mCameraID = cameraID;
            mCamera.setCameraIndex(cameraID);
            mCamera.enableView();
        }
    }

    @ReactProp(name="enableProcessing")
    public void setCameraID(JavaCamera2View view, @Nullable Boolean enableProcessing) {
        if (enableProcessing != null) {
            mEnableProcessing = enableProcessing;
        }
    }

    @ReactProp(name="saveAsBase64")
    public void setSaveAsBase64(JavaCamera2View view, @Nullable Boolean saveAsBase64) {
        if (saveAsBase64 != null) {
            this.saveAsBase64 = saveAsBase64;
        }
    }
}
