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

package gedoor.kunfei.lunarreminder.Async;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

import gedoor.kunfei.lunarreminder.UI.MainActivity;


public abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

  MainActivity activity;
  final com.google.api.services.calendar.Calendar client;
//  private final View progressBar;

  CalendarAsyncTask(MainActivity activity) {
    this.activity = activity;
    client = activity.client;
//    progressBar = fragment.getListView().findViewById(R.id.title_refresh_progress);
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
//    activity.numAsyncTasks++;
//    activity.mAyncTaskList.add(this);
////    progressBar.setVisibility(View.VISIBLE);
//    activity.setProgressBarIndeterminateVisibility(true);
  }

  @Override
  protected final Boolean doInBackground(Void... ignored) {
    try {
      doInBackground();
      return true;
    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
      availabilityException.printStackTrace();
    } catch (UserRecoverableAuthIOException userRecoverableException) {
      activity.swNoRefresh();
      activity.startActivityForResult(userRecoverableException.getIntent(), activity.REQUEST_AUTHORIZATION);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  protected void onPostExecute(Boolean success) {
    super.onPostExecute(success);
    if (success) {
      activity.refreshView();
    }
  }
  
  @Override
  protected void onCancelled(Boolean success) {
	  super.onCancelled(success);

  }

  abstract protected void doInBackground() throws IOException;
}
