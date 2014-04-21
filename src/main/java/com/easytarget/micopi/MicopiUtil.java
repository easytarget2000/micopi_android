/*
 * Copyright (C) 2014 Easy Target
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.easytarget.micopi;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Functional class containing utility methods for dialogs and saving files.
 *
 * Created by Michel on 19.01.14.
 */
public class MicopiUtil implements MediaScannerConnection.MediaScannerConnectionClient {
    private Context mContext;
    private MediaScannerConnection mConn;
    private String mFileName;

    public MicopiUtil( Context context ) {
        mContext = context;
    }

//    /**
//     *
//     * @param iMsgResId    Message that will be displayed in the dialog
//     */
//    public void showErrorDialog( int iMsgResId ) {
//        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick( DialogInterface dialog, int iButton ) {
////                if( iButton == DialogInterface.BUTTON_POSITIVE ) {
////                    showExceptionAlert( cF, strExceptionF );
////                }
//            }
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder( mContext );
//        builder.setMessage( iMsgResId )
//                .setNeutralButton( android.R.string.ok, dialogClickListener )
////                .setPositiveButton( R.string.report_button_label, dialogClickListener )
//                .show();
//    }

    /**
     * Saves the generated image to a file.
     */
    public String saveContactImageFile( Bitmap generatedBitmap,
                                      String strPickedName, char cMd5 ) {
        String strFileName = strPickedName.replace( ' ', '_' ) + "-" + cMd5 + ".png";

        // Files will be stored in the /sdcard/micopi dir.
        File micopiFolder = new File( Environment.getExternalStorageDirectory() + "/micopi/" );
        if( micopiFolder.mkdirs() ) Log.i( "New directory created", micopiFolder.getPath() );
        else Log.i( "New directory created", "false" );

        // The file name is "FirstName_LastName-x.png".
        File file = new File( micopiFolder.getAbsolutePath(), strFileName );
        FileOutputStream fileOutStream;

        try {
            fileOutStream = new FileOutputStream( file );
            generatedBitmap.compress( Bitmap.CompressFormat.PNG, 90, fileOutStream );
            fileOutStream.close();
            performMediaScan( file );
        } catch ( Exception e ) {
            e.printStackTrace();
            return "";
        }
        return strFileName;

    }

    /**
     * Makes the saved picture appear in Android's gallery.
     * @param file  Scan this file for media content
     */
    private void performMediaScan( File file ) {
        this.mFileName = file.getAbsolutePath();
        mConn = new MediaScannerConnection( mContext, this );
        mConn.connect();
    }
    @Override
    public void onMediaScannerConnected() {
        mConn.scanFile(mFileName, null );
    }

    @Override
    public void onScanCompleted( String path, Uri uri ) {
        mConn.disconnect();
    }

}
