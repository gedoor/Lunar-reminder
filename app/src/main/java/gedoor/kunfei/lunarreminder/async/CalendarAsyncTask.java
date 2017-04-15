/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package gedoor.kunfei.lunarreminder.async;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;

public abstract class CalendarAsyncTask extends AsyncTask<Void, Integer, Boolean> {
    BaseActivity activity;
    com.google.api.services.calendar.Calendar client;

    CalendarAsyncTask(BaseActivity activity) {
        this.activity = activity;
        client = activity.client;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.syncStart();
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            availabilityException.printStackTrace();
            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("error", availabilityException.getMessage()));
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            publishProgress(-1);
            activity.startActivityForResult(userRecoverableException.getIntent(), BaseActivity.REQUEST_AUTHORIZATION);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("error", e.getMessage()));
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        if (values[0] == -1) {
            activity.userRecoverable();
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        if (success) {
            activity.syncSuccess();
        } else {
            activity.syncError();
            Toast.makeText(activity, "出现一个错误,已拷贝到剪贴板", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled(Boolean success) {
        super.onCancelled(success);
    }

    abstract protected void doInBackground() throws IOException;

}
