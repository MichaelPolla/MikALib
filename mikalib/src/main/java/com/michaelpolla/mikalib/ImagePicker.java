package com.michaelpolla.mikalib;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Image Picker, allowing user to select a picture from a gallery app or by taking one with the ImagePicker.
 *
 * Based on : http://stackoverflow.com/a/12347567/1975002 | gallery only : http://stackoverflow.com/a/5309217/1975002
 */

//TODO: working as-is if used in an Activity or Fragment, but would be nice to remove errors here (make it "standalone").

@SuppressWarnings({"WeakerAccess", "unused"})
public class ImagePicker {

    private static final int PICTURE_REQUEST = 1;
    private Uri cameraImageUri;

    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "FPConnect" + File.separator);
        root.mkdirs();
        final String filename = StringUtils.getDateTimeFilename(".jpg");
        final File sdImageMainDirectory = new File(root, filename);
        cameraImageUri = Uri.fromFile(sdImageMainDirectory);

        // ImagePicker.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo resolveInfo : listCam) {
            final String packageName = resolveInfo.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.Choose_app_to_use_to_select_image));

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, PICTURE_REQUEST);
    }
}
