package monotalk.db;

import android.app.Application;
import android.util.Log;

/**
 * Created by kensakurai on 2015/01/22.
 */
public class TestApplication1 extends Application {
    @Override
    public void onCreate() {
    }

    @Override
    public void onTerminate() {
        /** This Method Called when this Application finished. */
        Log.v("TestApplication1", "--- onTerminate() in ---");
        MonoTalk.dispose();
    }
}
