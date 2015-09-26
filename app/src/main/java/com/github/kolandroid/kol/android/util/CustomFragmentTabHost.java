package com.github.kolandroid.kol.android.util;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.ArrayList;

/**
 * Special TabHost that allows the use of {@link Fragment} objects for
 * its tab content.  When placing this in a view hierarchy, after inflating
 * the hierarchy you must call {@link #setup(Context, FragmentManager, int)}
 * to complete the initialization of the tab host.
 * <p/>
 * <p>Here is a simple example of using a FragmentTabHost in an Activity:
 * <p/>
 * {@see development/samples/Support4Demos/src/com/example/android/supportv4/app/FragmentTabs.java
 * complete}
 * <p/>
 * <p>This can also be used inside of a fragment through fragment nesting:
 * <p/>
 * {@see development/samples/Support4Demos/src/com/example/android/supportv4/app/FragmentTabsFragmentSupport.java
 * complete}
 */
public class CustomFragmentTabHost extends TabHost
        implements TabHost.OnTabChangeListener {
    private final ArrayList<TabInfo> mTabs = new ArrayList<>();
    private FrameLayout mRealTabContent;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private TabHost.OnTabChangeListener mOnTabChangeListener;
    private OnCreateFragmentListener mOnCreateFragmentListener;

    private TabInfo mLastTab;
    private boolean mAttached;

    public CustomFragmentTabHost(Context context) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        super(context, null);
        mContainerId = android.R.id.tabcontent;
        super.setOnTabChangedListener(this);
    }

    public CustomFragmentTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContainerId = android.R.id.tabcontent;
        super.setOnTabChangedListener(this);
    }

    public void clearAllTabs() {
        /*
        for(TabInfo info : mTabs) {
            if (info.fragment != null && !info.fragment.isDetached()) {
                Logger.log("CustomFragmentTabHost", "Clearing " + info.tag);
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }
        }
        */

        super.clearAllTabs();
        mLastTab = null;
        mTabs.clear();
    }

    private void initFragmentTabHost(Context context) {
        // If owner hasn't made its own view hierarchy, then as a convenience
        // we will construct a standard one here.
        if (findViewById(android.R.id.tabs) == null) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            addView(ll, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            TabWidget tw = new TabWidget(context);
            tw.setId(android.R.id.tabs);
            tw.setOrientation(TabWidget.HORIZONTAL);
            ll.addView(tw, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0));

            FrameLayout fl = new FrameLayout(context);
            fl.setId(android.R.id.tabcontent);
            ll.addView(fl, new LinearLayout.LayoutParams(0, 0, 0));

            mRealTabContent = fl = new FrameLayout(context);
            mRealTabContent.setId(mContainerId);
            ll.addView(fl, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        }
    }

    /**
     * @deprecated Don't call the original TabHost setup, you must instead
     * call {@link #setup(Context, FragmentManager)} or
     * {@link #setup(Context, FragmentManager, int)}.
     */
    @Override
    @Deprecated
    public void setup() {
        throw new IllegalStateException(
                "Must call setup() that takes a Context and FragmentManager");
    }

    public void setup(Context context, FragmentManager manager) {
        initFragmentTabHost(context);
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        ensureContent();
    }

    public void setup(Context context, FragmentManager manager, int containerId) {
        initFragmentTabHost(context);
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
        ensureContent();
        mRealTabContent.setId(containerId);

        // We must have an ID to be able to save/restore our state.  If
        // the owner hasn't set one at this point, we will set it our self.
        if (getId() == View.NO_ID) {
            setId(android.R.id.tabhost);
        }
    }

    private void ensureContent() {
        if (mRealTabContent == null) {
            mRealTabContent = (FrameLayout) findViewById(mContainerId);
            if (mRealTabContent == null) {
                throw new IllegalStateException(
                        "No tab content FrameLayout found for id " + mContainerId);
            }
        }
    }

    @Override
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    public void setOnCreateFragmentListener(OnCreateFragmentListener f) {
        mOnCreateFragmentListener = f;
    }

    public ArrayList<TabInfo> getTabs() {
        return new ArrayList<>(mTabs);
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> classId, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mContext));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, classId, args);

        if (mAttached) {
            // If we are already attached to the window, then check to make
            // sure this tab's fragment is inactive if it exists.  This shouldn't
            // normally happen.
            info.fragment = mFragmentManager.findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            if (info.fragment != null) {
                info.fragment = null;
            }
        }

        for (TabInfo existingTab : mTabs) {
            if (existingTab.tag.equals(tag)) {
                existingTab.reset(classId, args);
                //If we are replacing the current tab, we have to replace the contents as well
                if (existingTab == mLastTab) {
                    FragmentTransaction res = doTabChanged(tag, null, true);
                    if (res != null)
                        res.commit();
                }
                return;
            }
        }

        //Otherwise, no existing tab found
        mTabs.add(info);
        addTab(tabSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        String currentTab = getCurrentTabTag();

        // Go through all tabs and make sure their fragments match
        // the correct state.
        FragmentTransaction ft = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            tab.fragment = mFragmentManager.findFragmentByTag(tab.tag);
            if (tab.fragment != null && !tab.fragment.isDetached()) {
                if (tab.tag.equals(currentTab)) {
                    // The fragment for this tab is already there and
                    // active, and it is what we really want to have
                    // as the current tab.  Nothing to do.
                    mLastTab = tab;
                } else {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }
                    ft.detach(tab.fragment);
                }
            }
        }

        // We are now ready to go.  Make sure we are switched to the
        // correct tab.
        mAttached = true;
        ft = doTabChanged(currentTab, ft);
        if (ft != null) {
            ft.commit();
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.curTab = getCurrentTabTag();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentTabByTag(ss.curTab);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (mAttached) {
            FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commit();
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    public View getTabByTag(String tag) {
        TabInfo info = getChildByTag(tag);
        if (info == null) return null;

        int index = mTabs.indexOf(info);
        return getTabWidget().getChildTabViewAt(index);
    }

    public TabInfo getChildByTag(String tag) {
        if (tag == null) return null;
        for (TabInfo ti : mTabs) {
            if (ti.tag.equals(tag))
                return ti;
        }

        return null;
    }

    private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft) {
        return doTabChanged(tabId, ft, false);
    }

    private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft, boolean forced) {
        TabInfo newTab = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(tabId)) {
                newTab = tab;
            }
        }
        if (newTab == null) {
            if (tabId == null) return ft;
            throw new IllegalStateException("No tab known for tag " + tabId);
        }
        if (forced || mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager.beginTransaction();
            }
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    ft.detach(mLastTab.fragment);
                }
            }

            if (newTab.fragment == null) {
                newTab.fragment = Fragment.instantiate(mContext,
                        newTab.classId.getName(), newTab.args);

                if (mOnCreateFragmentListener != null)
                    mOnCreateFragmentListener.setup(newTab.fragment, newTab.tag);
                ft.add(mContainerId, newTab.fragment, newTab.tag);
            } else {
                ft.attach(newTab.fragment);
            }

            mLastTab = newTab;
        }
        return ft;
    }

    public interface OnCreateFragmentListener {
        void setup(Fragment f, String tag);
    }

    public static final class TabInfo {
        private final String tag;
        private Class<?> classId;
        private Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            classId = _class;
            args = _args;
        }

        public Bundle getArgs() {
            return args;
        }

        public Fragment getFragment() {
            return fragment;
        }

        private void reset(Class<?> _class, Bundle _args) {
            this.classId = _class;
            this.args = _args;
            this.fragment = null;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String curTab;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            curTab = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(curTab);
        }

        @Override
        public String toString() {
            return "FragmentTabHost.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " curTab=" + curTab + "}";
        }
    }
}

