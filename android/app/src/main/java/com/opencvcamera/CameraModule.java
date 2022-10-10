package com.opencvcamera;

// Facebook (React Native)
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
// OpenCV
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
// Android
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
// Java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraModule extends ReactContextBaseJavaModule {
	// React Native
	public static final String REACT_CLASS = "CameraModule";
	// Folders
	private String mImageFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/JavaCameraView/";
	// Files
	private String mFileType = ".jpeg";
	// Frame Cycle Counter
	private long mFrameCycle;
	
	CameraModule(ReactApplicationContext context) {
		super(context);
	}

	public String getName() {
		return REACT_CLASS;
	}

	@ReactMethod
	private void takePicture(Boolean firstCall, Callback callback) {
		// firstCall is defined in a recursion loop in react native to prevent Java code from stalling
		if (firstCall == true) {
			CameraViewManager.frameCycle = 0; // Catch for rare case this value reaches it's maximum
			mFrameCycle = CameraViewManager.frameCycle;
		}
		// Checks # of framecycles passed to ensure capture with no processing artifacts
		if ((CameraViewManager.frameCycle - mFrameCycle < 2) || CameraViewManager.captureMat.empty()) {
			callback.invoke(false, "");
		}
		else {
			Mat rgba = CameraViewManager.captureMat.clone();
			
			if (rgba.empty()) {
				callback.invoke(true, ""); // Catch for empty matrix error (will require user retake picture)
			}

			Mat outputMat = new Mat();
			
			// Rotate image to portrait
			Core.flip(rgba.t(), outputMat, 1);

			// Patch to correct RGB channels for JavaCamera2View (If using JavaCameraView, remove this line)
			Imgproc.cvtColor(outputMat, outputMat, Imgproc.COLOR_RGBA2BGRA);

			String filePath = "";

			if (CameraViewManager.saveAsBase64) {
				MatOfByte outputMatOfByte = new MatOfByte();
				Imgcodecs.imencode(mFileType, outputMat, outputMatOfByte);
				byte[] outputByte = outputMatOfByte.toArray();
				filePath = Base64.encodeToString(outputByte, Base64.DEFAULT);
			}
			else {
				// Create Folder to Store Images
				createFolder();
				// Create Unique Image Filename
				filePath = uniqueName();
				// Write Image to Path
				Imgcodecs.imwrite(filePath, outputMat);
			}

			rgba.release();
			outputMat.release();
			callback.invoke(true, filePath);
		}
    }

	private String uniqueName() {
		String currentDateAndTime = (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date());
		String filePath = mImageFolderPath + currentDateAndTime + mFileType;
		
		// Accounts for duplicate file names
		File tempFile = new File(filePath);
		int tempIndex = 1;
		while(tempFile.exists()) {
			filePath = mImageFolderPath + currentDateAndTime + "("+ tempIndex++ +")" + mFileType;
			tempFile = new File(filePath);
		}

		return filePath;
	}

	private void createFolder() {
		File imageFolder = new File(mImageFolderPath);
		if (!imageFolder.exists()) {
			if (imageFolder.mkdirs()) {
				Log.d(REACT_CLASS, "Image folder created");
			}
			Log.d(REACT_CLASS, "Failed to create image folder");
		}
		Log.d(REACT_CLASS, "Image folder exists");
	}

	// Getter Example (Send native props to React Native)
	// @ReactMethod
	// private void getExample(Callback callback) {
	// 	callback.invoke(CameraViewManager.example);
	// }
}
