package com.example.chocoasus.dial;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteTextView  extends AutoCompleteTextView{

        private int myThreshold;

        public CustomAutoCompleteTextView(Context context) {
            super(context);
        }

        public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        //set threshold 0.
        public void setThreshold(int threshold) {
            if (threshold < 0) {
                threshold = 0;
            }
            myThreshold = threshold;
        }

        //if threshold   is 0 than return true
        public boolean enoughToFilter() {
            return true;
        }

        //invoke on focus
        protected void onFocusChanged(boolean focused, int direction,
                                      Rect previouslyFocusedRect) {
            //skip space and backspace
            super.performFiltering("", 67);
            // TODO Auto-generated method stub
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }

        public int getThreshold() {
            return myThreshold;
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                // Contact has pressed Back key. So hide the keyboard & clear focus
                InputMethodManager mgr = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
                this.clearFocus();
                // So event is propagated.
                return false;} return super.dispatchKeyEvent(event);
        }
    }