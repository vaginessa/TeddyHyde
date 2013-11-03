/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.EditorHyde.app;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link CardFlipActivity} and {@link
 * ScreenSlideActivity} samples.</p>
 */
public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    private String theMarkdown;
    private String theFile;
    ViewGroup rootView;
    boolean mDirty;
    public boolean mYfmDisplayed = false;
    EditText editor;
    EditText yfmEditText;
    public String theYfm;
    Button toggleYfm;
    Boolean togglingYfm = false;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber, String markdown, String filename ) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        fragment.setMarkdown( markdown );
        fragment.setFilename( filename );
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    public void setFilename( String filename ) {
        theFile = filename;
    }

    public void setMarkdown( String md) {
        theMarkdown = md;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void makeClean() {
        mDirty = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_editor, container, false);

        editor = ((EditText)rootView.findViewById(R.id.markdownEditor));
        yfmEditText = ((EditText)rootView.findViewById(R.id.yfmEditText));

        String stripped = theMarkdown;

        toggleYfm = (Button)rootView.findViewById(R.id.toggleYFM);
        if( theFile.endsWith( ".md") ) {

            // Strip YFM
            stripped = MarkupUtilities.stripYFM( theMarkdown );

            // Get the YFM
            theYfm = MarkupUtilities.getYFM( theMarkdown );

            toggleYfm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleYamlFrontMatter();
            }
        });
        }
        else {
            toggleYfm.setVisibility(View.GONE);
        }

        editor.setText( stripped );

        editor.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                mDirty = true;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        TextView filenameView = (TextView)rootView.findViewById(R.id.currentFilename);
        filenameView.setText(theFile);

        return rootView;
    }

    private void toggleYamlFrontMatter() {

        Boolean isDirtyNow = mDirty;
        togglingYfm = true;
        // Get the current editor text
        theMarkdown = editor.getText().toString();

        if( mYfmDisplayed ) {
            // save the YFM
            theYfm = MarkupUtilities.getYFM( theMarkdown );
            yfmEditText.setText( theYfm );
            String newText = MarkupUtilities.stripYFM( theMarkdown );
            editor.setText( newText );
            toggleYfm.setText(getString(R.string.show_yfm));
        }
        else {
            editor.setText( theYfm + theMarkdown );
            yfmEditText.setText( "" );
            toggleYfm.setText(getString(R.string.hide_yfm));
        }
        mYfmDisplayed = !mYfmDisplayed;

        if( isDirtyNow != mDirty ) {
            mDirty = isDirtyNow;
        }
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}