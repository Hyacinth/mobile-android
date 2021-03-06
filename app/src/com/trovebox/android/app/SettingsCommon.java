
package com.trovebox.android.app;


import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceCategory;

import android.content.DialogInterface;

import com.trovebox.android.app.bitmapfun.util.ImageCacheUtils;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.facebook.FacebookSessionEvents;
import com.trovebox.android.app.facebook.FacebookSessionEvents.LogoutListener;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.provider.UploadsUtils;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;

public class SettingsCommon implements
        OnPreferenceClickListener
{
    static final String TAG = SettingsCommon.class.getSimpleName();
    Activity activity;
    Preference mLoginPreference;
    Preference mFacebookLoginPreference;
    Preference mSyncClearPreference;
    Preference diskCacheClearPreference;
    PreferenceCategory loginCategory;
    Preference mServerUrl;
    Preference autoUploadTagPreference;

    public SettingsCommon(Activity activity)
    {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (activity.getString(R.string.setting_account_loggedin_key)
                .equals(
                        preference.getKey()))
        {
            if (Preferences.isLoggedIn(activity))
            {

                // confirm if user wants to log out
                new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                        .setTitle(R.string.logOut)
                        .setMessage(R.string.areYouSureQuestion)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener()
                                {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int whichButton)
                                    {
                                        TrackerUtils.trackButtonClickEvent("setting_logout",
                                                activity);
                                        Preferences
                                                .logout(activity);
                                        new LogoutTask().execute();
                                    }
                                })
                        .setNegativeButton(R.string.no, null)
                        .show();

            } else
            {
                activity.finish();
            }
        } else if (activity.getString(
                R.string.setting_account_facebook_loggedin_key)
                .equals(preference.getKey()))
        {
            LogoutListener logoutListener = new LogoutListener()
            {

                @Override
                public void onLogoutBegin()
                {
                }

                @Override
                public void onLogoutFinish()
                {
                    FacebookSessionEvents.removeLogoutListener(this);
                    loginCategory
                            .removePreference(mFacebookLoginPreference);
                }

            };
            mFacebookLoginPreference.setEnabled(false);
            FacebookSessionEvents.addLogoutListener(logoutListener);
            FacebookUtils.logoutRequest(activity);
        }
        return false;
    }

    public void refresh()
    {
        mLoginPreference.setTitle(Preferences.isLoggedIn(activity) ?
                R.string.setting_account_loggedin_logout
                : R.string.setting_account_loggedin_login);
    }

    public Preference getLoginPreference()
    {
        return mLoginPreference;
    }

    public void setLoginPreference(Preference mLoginPreference)
    {
        this.mLoginPreference = mLoginPreference;
        this.mLoginPreference.setOnPreferenceClickListener(this);
    }

    public Preference getFacebookLoginPreference()
    {
        return mFacebookLoginPreference;
    }

    public void setFacebookLoginPreference(
            Preference mFacebookLoginPreference)
    {
        this.mFacebookLoginPreference = mFacebookLoginPreference;
        this.mFacebookLoginPreference.setOnPreferenceClickListener(this);
        if (FacebookProvider.getFacebook() == null
                || !FacebookProvider.getFacebook().isSessionValid())
        {
            loginCategory.removePreference(mFacebookLoginPreference);
        }
    }

    public void setAutoUploadTagPreference(
            Preference autoUploadTagPreference)
    {
        this.autoUploadTagPreference = autoUploadTagPreference;
        this.autoUploadTagPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_auto_upload_tag",
                                activity);
                        SettingsCommon.this.autoUploadTagPreference.setSummary(newValue.toString());
                        return true;
                    }
                });
        autoUploadTagPreference.setSummary(Preferences.getAutoUploadTag(activity));
    }

    public void setWiFiOnlyUploadPreference(
            Preference wiFiOnlyUploadPreference)
    {
        wiFiOnlyUploadPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_wifi_only_upload_on",
                                newValue,
                                activity);
                        return true;
                    }
                });
    }

    public void setAutoUploadPreference(
            Preference autoUploadPreference)
    {
        autoUploadPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        TrackerUtils.trackPreferenceChangeEvent("settings_autoupload_on", newValue,
                                activity);
                        return true;
                    }
                });
    }

    public PreferenceCategory getLoginCategory()
    {
        return loginCategory;
    }

    public void setLoginCategory(PreferenceCategory loginCategory)
    {
        this.loginCategory = loginCategory;
    }

    public Preference getServerUrl()
    {
        return mServerUrl;
    }

    public void setServerUrl(Preference mServerUrl)
    {
        this.mServerUrl = mServerUrl;
        mServerUrl.setSummary(Preferences.getServer(activity));
        mServerUrl
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue)
                    {
                        String oldValue = ((EditTextPreference)
                                preference).getText();
                        if (!oldValue.equals(newValue))
                        {
                            Preferences.logout(activity);
                            refresh();
                        }
                        return true;
                    }
                });
    }

    public void setSyncClearPreference(Preference mSyncClearPreference)
    {
        this.mSyncClearPreference = mSyncClearPreference;
        mSyncClearPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener()
                {

                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        // confirm if user wants to clear sync information
                        new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                                .setTitle(R.string.sync_clear)
                                .setMessage(R.string.areYouSureQuestion)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton)
                                            {
                                                TrackerUtils.trackButtonClickEvent(
                                                        "setting_sync_clear", activity);
                                                UploadsUtils.clearUploadsAsync();
                                            }
                                        })
                                .setNegativeButton(R.string.no, null)
                                .show();

                        return true;
                    }
                });
    }

    public void setDiskCachClearPreference(Preference diskCacheClearPreference)
    {
        this.diskCacheClearPreference = diskCacheClearPreference;
        diskCacheClearPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener()
                {

                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        // confirm if user wants to clear sync information
                        new AlertDialog.Builder(activity, R.style.Theme_Trovebox_Dialog_Light)
                                .setTitle(R.string.disk_cache_clear)
                                .setMessage(R.string.areYouSureQuestion)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int whichButton)
                                            {
                                                TrackerUtils.trackButtonClickEvent(
                                                        "setting_disk_cache_clear", activity);
                                                ImageCacheUtils
                                                        .clearDiskCachesAsync(new ProgressDialogLoadingControl(
                                                                activity,
                                                                true,
                                                                false,
                                                                activity.getString(R.string.loading)));
                                            }
                                        })
                                .setNegativeButton(R.string.no, null)
                                .show();

                        return true;
                    }
                });
    }

    public class LogoutTask extends SimpleAsyncTaskEx
    {

        public LogoutTask() {
            super(new ProgressDialogLoadingControl(activity, true, false,
                    activity.getString(R.string.loading)));
        }

        @Override
        protected void onSuccessPostExecute() {
            if (activity != null)
            {
                activity.finish();
            }
        }

        @Override
        protected void onFailedPostExecute() {
            if (activity != null)
            {
                activity.finish();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try
            {
                UploadsUtils.clearUploads();
                FacebookUtils.logoutRequest(activity);
                TwitterUtils.logout(activity);
                ImageCacheUtils.clearDiskCaches();
                return true;
            } catch (Exception ex)
            {
                GuiUtils.noAlertError(TAG, ex);
            }
            return false;
        }

    }
}
