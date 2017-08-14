package part4project.uoa.gather;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * Created by Ida on 14/08/2017.
 * https://stackoverflow.com/questions/14407514/switch-preference-handling-both-onpreferencechange-and-onpreferenceclick
 */

    public class AutoUploadSwitchPreference extends SwitchPreference {
        public AutoUploadSwitchPreference(Context context) {
            super(context);
        }
        public AutoUploadSwitchPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public AutoUploadSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onClick() {
            //super.onClick(); THIS IS THE IMPORTANT PART!
        }
}
