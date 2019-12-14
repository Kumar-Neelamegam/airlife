package at.jku.mobilecomputing.airlife.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class AsynkTaskCustom extends AsyncTask<onWriteCode, Void, Void> {
    private onWriteCode object;
    private Object returnobject;
    private final Context context;
    private final ProgressDialog p;

    public AsynkTaskCustom(Context context, String Message) {
        this.context = context;
        this.p = new ProgressDialog(context);
        this.p.setMessage(Message);
        this.p.setIndeterminate(false);
        this.p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.p.setCancelable(false);
        this.p.show();

    }


    @Override
    protected final Void doInBackground(onWriteCode... params) {

        try {
            this.object = params[0];
            this.returnobject = this.object.onExecuteCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected final void onPostExecute(Void result) {
        super.onPostExecute(result);

        this.p.dismiss();

        try {
            this.object.onSuccess(this.returnobject);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
