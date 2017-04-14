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

package gedoor.kunfei.lunarreminder.sync;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;

public abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

    BaseActivity activity;
    final com.google.api.services.calendar.Calendar client;

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
            showMassage(availabilityException.getMessage());
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            userRecoverable(userRecoverableException);
        } catch (IOException e) {
            e.printStackTrace();
            showMassage(e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        activity.syncFinish();
    }

    @Override
    protected void onCancelled(Boolean success) {
        super.onCancelled(success);

    }

    abstract protected void doInBackground() throws IOException;

    private void showMassage(String massage) {
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(()->{
            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(activity.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("error", massage));
            Toast.makeText(activity, "出现一个错误,已拷贝到剪贴板", Toast.LENGTH_LONG).show();
        });
    }

    private void userRecoverable(UserRecoverableAuthIOException userRecoverableException) {
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(()->{
            activity.userRecoverable(userRecoverableException);
        });
    }

}
