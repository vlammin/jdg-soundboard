/*
 * SoundDetailDialog
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl.dialogs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.atmx.android.jdgspl.R;
import com.atmx.android.jdgspl.events.RefreshFavoritesEvent;
import com.atmx.android.jdgspl.models.DbHelper;
import com.atmx.android.jdgspl.models.FavoriteModel;
import com.atmx.android.jdgspl.objects.Favorite;
import com.atmx.android.jdgspl.objects.Sound;
import com.atmx.android.jdgspl.tools.Toaster;
import com.atmx.android.jdgspl.tools.Utils;
import com.commonsware.cwac.provider.StreamProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SoundDetailDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_sound_detail, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setPositiveButton(
            getString(R.string.close),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            }
        );

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            final Sound sound = bundle.getParcelable("sound");

            if (sound != null) {
                TextView mSampleName = view.findViewById(R.id.sound_title);
                mSampleName.setText(sound.getTitle());

                setVideoDetails(view, sound);

                View btnAlarm = view.findViewById(R.id.set_as_alarm);
                btnAlarm.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        addAsRingtoneWithPermission(sound, RingtoneManager.TYPE_ALARM);
                    }
                });

                View btnNotification = view.findViewById(R.id.set_as_notification);
                btnNotification.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        addAsRingtoneWithPermission(sound, RingtoneManager.TYPE_NOTIFICATION);
                    }
                });

                View btnRingtone = view.findViewById(R.id.set_as_ringtone);
                btnRingtone.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        addAsRingtoneWithPermission(sound, RingtoneManager.TYPE_RINGTONE);
                    }
                });

                // Favorite
                DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
                View btnFavorite = view.findViewById(R.id.set_as_favorite);
                if (dbHelper.getSoundModel().existsFavorite(sound)) {
                    if (btnFavorite instanceof Button)
                        ((Button) btnFavorite).setText(R.string.delete_favorite);

                    btnFavorite.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            deleteFavorite(sound);
                            dismiss();
                        }
                    });
                } else {
                    btnFavorite.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            addFavorite(sound);
                            dismiss();
                        }
                    });
                }

                View btnShare = view.findViewById(R.id.share);
                btnShare.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        shareSound(sound);
                    }
                });
            }
        }
        return builder.create();
    }

    private void setVideoDetails(View view, Sound sample) {
        if (sample.getVideo() != null) {
            final String videoYoutubeId = sample.getVideo().getYoutubeId();

            TextView mVideoTitle = view.findViewById(R.id.video_title);
            mVideoTitle.setText(sample.getVideo().getTitle());

            TextView mVideoPublishDate = view.findViewById(R.id.video_publish_date);
            mVideoPublishDate.setText(sample.getVideo().getFormattedPublishDate());

            View btnSeeVideo = view.findViewById(R.id.see_video);
            btnSeeVideo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    seeVideo(videoYoutubeId);
                    dismiss();
                }
            });
        } else {
            View videoDetails = view.findViewById(R.id.video_details);
            videoDetails.setVisibility(View.GONE);
        }
    }

    private void addFavorite(Sound sound) {
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        FavoriteModel favoriteModel = dbHelper.getFavoriteModel();
        Favorite favorite = new Favorite(sound.getTitle(), sound);

        if (favoriteModel.exists(favorite)) {
            Toaster.showShort(getActivity(), R.string.sound_already_favorite);
        } else {
            favoriteModel.add(favorite);
            Toaster.showShort(getActivity(), R.string.sound_add_favorite);
        }

        EventBus.getDefault().post(new RefreshFavoritesEvent());
    }

    private void deleteFavorite(Sound sound) {
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        FavoriteModel favoriteModel = dbHelper.getFavoriteModel();
        Favorite favorite = new Favorite(sound.getTitle(), sound);

        favoriteModel.delete(favorite);
        Toaster.showShort(getActivity(), R.string.sound_delete_favorite);

        EventBus.getDefault().post(new RefreshFavoritesEvent());
    }

    private void seeVideo(String videoYoutubeId) {
        String url = getString(R.string.url_youtube_watch);
        url += videoYoutubeId;

        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri u = Uri.parse(url);
        i.setData(u);
        startActivity(i);
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean addAsRingtone(Sound sound, int ringtoneType) {
        int resourceId = sound.getResourceId();

        boolean isRingtone = false;
        boolean isNotification = false;
        boolean isAlarm = false;

        if (!isExternalStorageAvailable()) {
            Toaster.showLong(getActivity(), R.string.error_external_not_available);
            return false;
        }

        String directory;
        int successString;
        switch (ringtoneType) {
            case RingtoneManager.TYPE_RINGTONE:
                isRingtone = true;
                directory = Environment.DIRECTORY_RINGTONES;
                successString = R.string.modification_done_ringtone;
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                isNotification = true;
                directory = Environment.DIRECTORY_NOTIFICATIONS;
                successString = R.string.modification_done_notification;
                break;
            default:
                isAlarm = true;
                directory = Environment.DIRECTORY_ALARMS;
                successString = R.string.modification_done_alarm;
        }

        File basePath = Environment.getExternalStoragePublicDirectory(directory);
        if (basePath == null) {
            Toaster.showLong(getActivity(), R.string.error_external_not_available);
            return false;
        }

        String path = basePath.getPath();
        String name = "JDG " + Utils.safeFilename(sound.getTitle()) + ".mp3";
        File ringtoneFile = new File(path + "/", name);

        // Copy raw file to external storage if ringtone file not exists
        if (!ringtoneFile.exists()) {
            try {
                InputStream in = getResources().openRawResource(resourceId);
                OutputStream out = new FileOutputStream(ringtoneFile);
                byte[] data = new byte[in.available()];
                int i = in.read(data);
                while (i != -1) {
                    out.write(data, 0, i);
                    i = in.read(data);
                }
                in.close();
                out.close();
            } catch (IOException io) {
                io.printStackTrace();
                Toaster.showLong(getActivity(), R.string.error_write_external);
                return false;
            }
        }

        // Set ringtone settings
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sound.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
        values.put(MediaStore.Audio.Media.TITLE, sound.getTitle());
        values.put(MediaStore.Audio.Media.ARTIST, getString(R.string.jdg));
        values.put(MediaStore.Audio.Media.IS_RINGTONE, isRingtone);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
        values.put(MediaStore.Audio.Media.IS_ALARM, isAlarm);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        try {
            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(
                ringtoneFile.getAbsolutePath()
            );
            ContentResolver resolver = getActivity().getContentResolver();
            RingtoneManager.setActualDefaultRingtoneUri(
                getActivity(),
                ringtoneType,
                resolver.insert(contentUri, values)
            );
        } catch (Exception e) {
            Toaster.showLong(getActivity(), R.string.error_write_settings);
            return false;
        }

        Toaster.showLong(getActivity(), successString);
        dismiss();

        return true;
    }

    private void shareSound(Sound sound) {
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType("audio/mp3");
        share.putExtra(Intent.EXTRA_STREAM, getSoundUri(sound));
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(share, getString(R.string.share_the_sound)));
    }

    private Uri getSoundUri(Sound sound) {
        String authority = getActivity().getPackageName();
        Uri provider = Uri.parse("content://" + authority);

        String path_name = getActivity()
            .getApplicationContext()
            .getResources()
            .getResourceEntryName(sound.getResourceId());

        return provider
            .buildUpon()
            .appendPath(StreamProvider.getUriPrefix(authority))
            .appendPath("jdg_" + path_name + ".mp3")
            .build();
    }

    /**
     * Permission handling
     **/

    private void addAsRingtoneWithPermission(Sound sound, int ringtoneType) {
        SoundDetailDialogPermissionsDispatcher.onAddAsRingtoneWithPermissionCheck(
            this,
            sound,
            ringtoneType
        );
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SoundDetailDialogPermissionsDispatcher.onRequestPermissionsResult(
            this,
            requestCode,
            grantResults
        );
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onPermissionDenied() {
        Toaster.showShort(getActivity(), R.string.perm_denied);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @SuppressWarnings("unused")
    void onShowRationale(PermissionRequest request) {
        Toaster.showLong(getActivity(), R.string.perm_denied_with_never_ask);
        Toaster.showLong(getActivity(), R.string.perm_rationale);
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNeverAskAgain() {
        Toaster.showLong(getActivity(), R.string.perm_denied_with_never_ask);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onAddAsRingtone(Sound sound, int ringtoneType) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.System.canWrite(getActivity())) {
                addAsRingtone(sound, ringtoneType);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(Uri.parse("package:" + getActivity().getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } else {
            addAsRingtone(sound, ringtoneType);
        }
    }
}
