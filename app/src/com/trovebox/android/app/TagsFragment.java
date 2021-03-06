
package com.trovebox.android.app;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.model.Tag;
import com.trovebox.android.app.ui.adapter.MultiSelectTagsAdapter;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GalleryOpenControl;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

public class TagsFragment extends CommonFragment
{
    public static final String TAG = TagsFragment.class.getSimpleName();

    private LoadingControl loadingControl;
    private GalleryOpenControl galleryOpenControl;

    private TagsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tags, container, false);

        init(v);

        return v;
    }

    public void init(View v) {
        mAdapter = new TagsAdapter();
        ListView list = (ListView) v.findViewById(R.id.list_tags);
        list.setAdapter(mAdapter);

        final EditText search = (EditText) v.findViewById(R.id.edit_search);
        search.setOnEditorActionListener(new OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event)
            {
                if (event != null)
                {
                    switch (event.getKeyCode())
                    {
                        case KeyEvent.KEYCODE_ENTER:
                            CommonUtils.debug(TAG, "Key code enter");
                            if (KeyEvent.ACTION_DOWN == event.getAction())
                            {
                                CommonUtils.debug(TAG, "Opening gallery");
                                search.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        galleryOpenControl.openGallery(search.getText()
                                                .toString().trim(), null);
                                    }
                                });
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });
        Button filterBtn = (Button) v.findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TrackerUtils.trackButtonClickEvent("filterBtn", TagsFragment.this);
                galleryOpenControl.openGallery(mAdapter.getSelectedTags(), null);
            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        galleryOpenControl = ((GalleryOpenControl) activity);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    private class TagsAdapter extends MultiSelectTagsAdapter implements
            OnClickListener
    {
        public TagsAdapter()
        {
            super(loadingControl);
        }

        @Override
        public View getView(Tag tag, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_tag,
                        null);
            }
            CheckBox checkBox = (CheckBox) convertView
                    .findViewById(R.id.tag_checkbox);
            initTagCheckbox(tag, checkBox);

            View clickView = convertView.findViewById(R.id.tag_click_view);
            clickView.setTag(tag);
            clickView.setOnClickListener(this);

            ((TextView) convertView.findViewById(R.id.text_count))
                    .setText(Integer.toString(tag
                            .getCount()));
            return convertView;
        }

        @Override
        public void onClick(View v) {
            TrackerUtils.trackButtonClickEvent("tag_click_view", TagsFragment.this);
            Tag tag = (Tag) v.getTag();
            galleryOpenControl.openGallery(tag.getTag(), null);
        }

    }

}
