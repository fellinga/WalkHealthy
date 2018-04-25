package fighting_mongooses.walkhealthy.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fighting_mongooses.walkhealthy.R;

/**
 * Created by mario on 3/27/2018.
 */

public class AutoUpdate {

    private Context context;
    private final String versionNumber  = "0.3.4";
    private final String apkName        = "WalkHealthy.apk";
    private final String serverUrl      = "https://www.fellinga.at/walkhealthy/";
    private final String onlineApkUrl   = serverUrl + apkName;
    private final String versionUrl     = serverUrl + "WalkHealthyVer";

    public AutoUpdate(Context context) {
        this.context = context;
    }

    public void execute() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            if (executor.submit(new CheckForUpdate()).get()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Update available!");
                builder.setCancelable(false);
                builder.setIcon(R.drawable.ic_system_update_black_24dp);
                builder.setMessage("Install now?");
                builder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        });
                builder.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new Downloader().execute();
                            }
                        });
                builder.show();
            }
        } catch (Exception e) {
            // Something went wrong...
        }
    }

    /**
     * Class to check for new updates
     */
    public class CheckForUpdate implements Callable<Boolean> {

        @Override
        public Boolean call() {
            try {
                // Create a URL for the desired page
                URL url = new URL(versionUrl);

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    if (str.startsWith("Version")) {
                        if (!str.endsWith(versionNumber)) {
                            return true;
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                return false;
            }
            return false;
        }
    }

    /**
     * Opens an install intent
     */
    private void installApk() {
        File directory = context.getFilesDir();
        File apkFile = new File(directory, apkName);

        Uri fileLoc;
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String provider = "fighting_mongooses.walkhealthy.fileprovider";
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            fileLoc = android.support.v4.content.FileProvider.getUriForFile(context, provider, apkFile);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            fileLoc = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(fileLoc, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    /**
     * Actual apk download class
     */
    private class Downloader extends AsyncTask<Void,Integer,Boolean> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Downloading...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                final URL url = new URL(onlineApkUrl);
                final URLConnection conn = url.openConnection();
                final int fileSize = conn.getContentLength();
                conn.connect();

                InputStream input = conn.getInputStream();
                FileOutputStream outputStream = context.openFileOutput(apkName, Context.MODE_PRIVATE);

                byte data[] = new byte[1024];
                int count;
                int downloaded=0;

                while ((count = input.read(data)) != -1) {
                    downloaded += count;
                    publishProgress((int)((downloaded*100)/(fileSize)));
                    outputStream.write(data, 0, count);
                }

                outputStream.close();
                input.close();

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            if (success) {
                installApk();
            } else {
                Toast.makeText(context, "Error.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }
    }

}
