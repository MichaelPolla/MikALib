package com.michaelpolla.mikalib;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Image Picker, allowing user to select a picture from a gallery app or by taking one with the camera.
 *
 * Based on : http://stackoverflow.com/a/12347567/1975002 | gallery only : http://stackoverflow.com/a/5309217/1975002
 */


@SuppressWarnings({"WeakerAccess", "unused"})
public class ImagePicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final int PICTURE_REQUEST = 1;
    private Uri cameraImageUri;
    private Bitmap rotatedFinalImage;

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
        final PackageManager packageManager = getPackageManager();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Process the result if it's OK (user finished the action)
        if (resultCode == RESULT_OK) {
            if (requestCode == PICTURE_REQUEST) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if(isCamera) {
                    selectedImageUri = cameraImageUri;
                } else {
                    selectedImageUri = data.getData();
                }
                Bitmap resizedImage = BitmapUtils.decodeAndResizeBitmapFromFile(getFilepathFromContentUri(selectedImageUri), 300, 300);
                rotatedFinalImage = BitmapUtils.getRotatedImage(ImageUtils.getImageRotation(getApplicationContext(), selectedImageUri), resizedImage);
            }
        }
    }

    //TODO: move this method elsewhere...
    /**
     * Converts a ContentUri (currently only if pointing to MediaStore) to the corresponding filepath.
     * Example : content://media/external/images/media/66911 --> /storage/A4F8-A472/DCIM/100ANDRO/DSC_0814.JPG
     *
     * If the Uri is already a filepath (like file:///storage/emulated/0/file.ext), returns the corresponding String.
     *
     * @param contentUri A MediaStore ContentUri.
     * @return the corresponding filepath.
     */
    public String getFilepathFromContentUri(Uri contentUri) {
        Cursor cursor = null;
        String path = null;

        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = this.getContentResolver().query(contentUri, projection, null, null, null);

            if(cursor == null) { // provided Uri is probably a filepath already.
                path = contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(column_index);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }
}
