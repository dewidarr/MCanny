package com.dewidar.makanny;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class Gui_Manager {
    private FragmentManager fragmentManager;
    private Fragment fragment;
    private Activity context;

    private static final Gui_Manager ourInstance = new Gui_Manager();

    public static Gui_Manager getInstance() {
        return ourInstance;
    }

    private Gui_Manager() {
    }

    private void clearBackStack() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void replaceCurrFragment(Fragment curr_fragment) {
        clearBackStack();
        this.fragment = curr_fragment;
        fragmentManager.beginTransaction()
                .replace(R.id.frame, curr_fragment)
                .commitAllowingStateLoss();
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.fragment = currentFragment;
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .add(R.id.frame, currentFragment)
                .commitAllowingStateLoss();
    }
    public Fragment getCurrentFragment(){
        return fragment;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

}
