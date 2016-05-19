
package vis.util.SerialHelper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerialConsole {

    private final String TAG = SerialConsole.class.getSimpleName();

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialDriver)}.
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private UsbSerialDriver sDriver = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    private final SerialInputOutputManager.Listener mListener;

    public SerialConsole(UsbSerialDriver driver, SerialInputOutputManager.Listener mListener) {
        this.sDriver = driver;
        this.mListener = mListener;
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sDriver != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sDriver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    public boolean open() {
        Log.d(TAG, "Open, sDriver=" + sDriver);
        if (sDriver == null) {
            return false;
        } else {
            try {
                sDriver.open();
                sDriver.setParameters(9600, 8, UsbSerialDriver.STOPBITS_1,
                        UsbSerialDriver.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                e.printStackTrace();
                try {
                    sDriver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sDriver = null;
                return false;
            }
        }
        onDeviceStateChange();
        return true;
    }

    public void close() {
        stopIoManager();
        if (sDriver != null) {
            try {
                sDriver.close();
            } catch (IOException e) {
                // Ignore.
            }
            sDriver = null;
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    
    public void wirte(byte[] b) {
        // byte[] data = null;
        // data = HexDump.hexStringToByteArray(str);

        mSerialIoManager.writeAsync(b);

    }

}
