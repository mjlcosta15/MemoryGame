package pt.isec.pem.memory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MultiplayerChooserActivity extends AppCompatActivity {

    boolean isConnected;
    ConnectivityManager cm;
    NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_chooser);
    }

    public void onClientClick(View v) {

         cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Toast.makeText(this, getString(R.string.toastNoWiFi), Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText editTextIP = new EditText(this);
        final AlertDialog.Builder rolerChooser = new AlertDialog.Builder(this);
        rolerChooser.setTitle(getString(R.string.dialogClientTitle));
        rolerChooser.setMessage(getString(R.string.dialogClientMessage));
        rolerChooser.setView(editTextIP);

        rolerChooser.setPositiveButton(getString(R.string.dialogClientConfirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(editTextIP.getText().toString().compareToIgnoreCase("") == 0){
                    Toast.makeText(MultiplayerChooserActivity.this, getString(R.string.toastEmptyIP), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent;
                intent = new Intent(MultiplayerChooserActivity.this, MultiplayerActivity.class);
                intent.putExtra("server", getIntent().getBooleanExtra("server",false));
                intent.putExtra("ip",editTextIP.getText().toString());
                startActivity(intent);
            }
        });
        rolerChooser.setNegativeButton(getString(R.string.dialogClientCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MultiplayerChooserActivity.this.finish();
            }
        });

        rolerChooser.show();
    }

    public void onServerClick(View v) {

        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Toast.makeText(this, getString(R.string.toastNoWiFi), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        intent = new Intent(this,SelectLevelActivity.class);
        intent.putExtra("gameMode", "Multiplayer");
        intent.putExtra("server", true);
        startActivity(intent);
    }
}
