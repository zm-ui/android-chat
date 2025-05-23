/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import cn.wildfire.chat.kit.AppServiceProvider;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.widget.LengthFilter;
import cn.wildfire.chat.kit.widget.SimpleTextWatcher;
import cn.wildfirechat.model.GroupInfo;

public class SetGroupAnnouncementActivity extends WfcBaseActivity {
    EditText announcementEditText;

    private MenuItem confirmMenuItem;
    private GroupInfo groupInfo;
    private GroupAnnouncement currentGroupAnnouncement;

    protected void bindViews() {
        super.bindViews();
        announcementEditText = findViewById(R.id.announcementEditText);
        announcementEditText.setFilters(new InputFilter[]{
            new LengthFilter(2000, maxTextLength -> {
                Toast.makeText(this, getString(R.string.group_announcement_max_length), Toast.LENGTH_SHORT).show();
            })
        });
        announcementEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                SetGroupAnnouncementActivity.this.onTextChanged();
            }
        });
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_set_announcement_activity;
    }

    @Override
    protected void afterViews() {
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        if (groupInfo == null) {
            finish();
            return;
        }

        WfcUIKit.getWfcUIKit().getAppServiceProvider().getGroupAnnouncement(groupInfo.target, new AppServiceProvider.GetGroupAnnouncementCallback() {
            @Override
            public void onUiSuccess(GroupAnnouncement announcement) {
                if (isFinishing()) {
                    return;
                }
                currentGroupAnnouncement = announcement;
                if (TextUtils.isEmpty(announcementEditText.getText())) {
                    announcementEditText.setText(announcement.text);
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(SetGroupAnnouncementActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected int menu() {
        return R.menu.group_set_group_name;
    }

    @Override
    protected void afterMenus(Menu menu) {
        confirmMenuItem = menu.findItem(R.id.confirm);
        confirmMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            setGroupName();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onTextChanged() {
        if (confirmMenuItem != null) {
            String text = announcementEditText.getText().toString().trim();
            if (currentGroupAnnouncement == null || !TextUtils.equals(text, currentGroupAnnouncement.text)) {
                confirmMenuItem.setEnabled(true);
            }
        }
    }

    private void setGroupName() {
        String announcement = announcementEditText.getText().toString().trim();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .content(R.string.processing)
            .progress(true, 100)
            .cancelable(false)
            .build();
        dialog.show();

        WfcUIKit.getWfcUIKit().getAppServiceProvider().updateGroupAnnouncement(groupInfo.target, announcement, new AppServiceProvider.UpdateGroupAnnouncementCallback() {
            @Override
            public void onUiSuccess(GroupAnnouncement announcement) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();
                Toast.makeText(SetGroupAnnouncementActivity.this, getString(R.string.set_group_announcement_success), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();
                Toast.makeText(SetGroupAnnouncementActivity.this, getString(R.string.set_group_announcement_failed, code, msg), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
