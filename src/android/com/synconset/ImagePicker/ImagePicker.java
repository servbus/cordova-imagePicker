/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";

	private CallbackContext callbackContext;
	private JSONObject params;
	private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			if (!hasPermisssion()) {
				requestPermissions(0);
			} else {
				getPictures();
			}
		}
		return true;
	}

	private void getPictures() throws JSONException{
		Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
		int max = 20;
		int desiredWidth = 0;
		int desiredHeight = 0;
		int quality = 100;
		if (this.params.has("maximumImagesCount")) {
			max = this.params.getInt("maximumImagesCount");
		}
		if (this.params.has("width")) {
			desiredWidth = this.params.getInt("width");
		}
		if (this.params.has("height")) {
			desiredHeight = this.params.getInt("height");
		}
		if (this.params.has("quality")) {
			quality = this.params.getInt("quality");
		}
		intent.putExtra("MAX_IMAGES", max);
		intent.putExtra("WIDTH", desiredWidth);
		intent.putExtra("HEIGHT", desiredHeight);
		intent.putExtra("QUALITY", quality);
		if (this.cordova != null) {
			this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			JSONArray res = new JSONArray(fileNames);
			this.callbackContext.success(res);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}
	/**
	 * check application's permissions
	 */
	public boolean hasPermisssion() {
		for (String p : permissions) {
			if (!PermissionHelper.hasPermission(this, p)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * We override this so that we can access the permissions variable, which no longer exists in
	 * the parent class, since we can't initialize it reliably in the constructor!
	 *
	 * @param requestCode The code to get request action
	 */
	public void requestPermissions(int requestCode) {
		PermissionHelper.requestPermissions(this, requestCode, permissions);
	}

	/**
	 * processes the result of permission request
	 *
	 * @param requestCode  The code to get request action
	 * @param permissions  The collection of permissions
	 * @param grantResults The result of grant
	 */
	public void onRequestPermissionResult(int requestCode, String[] permissions,
										  int[] grantResults) throws JSONException {
		PluginResult result;
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				Log.d(TAG, "Permission Denied!");
				this.callbackContext.error("未获得授权使用存储或相机，请在设置中打开");
				return;
			}
		}

		switch (requestCode) {
			case 0:
				getPictures();
				break;
		}
	}
}
