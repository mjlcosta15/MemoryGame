package pt.isec.pem.memory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayerActivity extends AppCompatActivity {

    private static final int PORT = 6000;
    Message msg = new Message();

    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;

    private Timer myTimer;
    Handler h = null;

    private TextView tvScore1,tvScore2;
    private GridView gridView;
    private ProgressDialog pd = null;
    private ServerSocket serverSocket = null;

    private boolean canGo;
    private boolean firstFlip;
    private ImageView imgFirstFlipped;
    private String firstCardFlipped;
    private int firstCardFlippedPosition;
    private int pairsDiscovered;
    private int score;
    private int pos = -1;
    private boolean doubleClick;
    private boolean doubleClickGo;
    private boolean server;
    private Level lvl = null;
    private String levelName;
    private boolean myTurn;
    private MyAdapter myAdapter;
    private int oppponentScore;
    private int clickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        h = new Handler();

        gridView = (GridView) findViewById(R.id.gvMultiplayer);
        tvScore1 = (TextView) findViewById(R.id.tvScore1);
        tvScore2 = (TextView) findViewById(R.id.tvScore2);

        tvScore1.setText(getString(R.string.player1) + ": 0");
        tvScore2.setText(getString(R.string.player2) + ": 0");

        Intent intent = getIntent();
        server = intent.getBooleanExtra("server",false);

        canGo = false;
        firstFlip = true;
        doubleClick = false;
        oppponentScore = 0;

        if(server) {
            tvScore1.setTypeface(null, Typeface.BOLD_ITALIC);
            levelName = intent.getStringExtra("level_name");
            lvl = getLevelFromFile(levelName);
            receiveClient();
        }else{
            tvScore2.setTypeface(null, Typeface.BOLD_ITALIC);
            new Thread(new ClientThread(socket,intent.getStringExtra("ip"))).start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        closeSocket();
    }

    public void onBackPressed(){

        //criar dialog para abandonar nivel e enviar msg ao sv ou cli de que vai terminar

        Message msg = new Message();
        msg.setAborted(true);
        sendM(msg);

        closeSocket();
        finish();
    }

    private void receiveClient() {
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.dialogServerMsg) + " " + levelName + "\n(IP: " + ip
                + ")");
        pd.setTitle(R.string.dialogServerTitle);
        pd.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeSocket();
                finish();
            }
        });
        pd.show();

        new Thread(new ServerThread(socket)).start();

    }

    class ClientThread implements Runnable {

        String ip;

        public ClientThread(Socket socket, String ip) {
            //this.serverSocket = socket;
            this.ip = ip;
            myTurn = false;
        }

        public void run() {

            //final Message msg = new Message();
            Message msgReceived;

            try {

                //serverSocket = new Socket(ip,PORT);
                socket = new Socket(ip,PORT);

                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                //msgReceived = (Message) receiveMessage();
                msgReceived = (Message) receiveM();

                levelName = msgReceived.getLevelName();
                lvl = msgReceived.getLvl();
                myAdapter = new MyAdapter(MultiplayerActivity.this,lvl);
                myAdapter.setmImagePaths(msgReceived.getAdapterArray());

                h.post(new Runnable() { //corre na UIPrincipal
                    @Override
                    public void run() {
                        createLevel();
                    }
                });

                while(msgReceived.isRunning()){

                    //msgReceived = (Message) receiveMessage();
                    msgReceived = (Message) receiveM();

                    setMyTurn(msgReceived.isYourTurn());

                    h.post(new updateUIThread(msgReceived));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class ServerThread implements Runnable {

        public ServerThread(Socket socket){
            myTurn = true;
        }

        public void run() {

            final Message msg = new Message();
            Message msgReceived = new Message();

            try {
                serverSocket = new ServerSocket(PORT);
                serverSocket.setSoTimeout(60000);

                //clientSocket = serverSocket.accept();
                socket = serverSocket.accept();
                pd.dismiss();

                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                myAdapter = new MyAdapter(MultiplayerActivity.this, lvl);

                //msg.setMyAdapter(myAdapter);
                msg.setAdapterArray(myAdapter.getmImagePaths());
                msg.setLevelName(levelName);
                msg.setLvl(lvl);
                sendM(msg); //envia o nivel ao cliente para ele o construir do outro lado

                h.post(new Runnable() { //corer na UIPrincipal
                    @Override
                    public void run() {
                        createLevel();
                    }
                });

                while(msgReceived.isRunning()){

                    msgReceived = (Message) receiveM();

                    setMyTurn(msgReceived.isYourTurn());

                    h.post(new updateUIThread(msgReceived));

                }

            } catch (SocketTimeoutException e) {

                h.post(new Runnable() { //corer na UIPrincipal
                    @Override
                    public void run() {
                        Toast.makeText(MultiplayerActivity.this, getString(R.string.toastClientNotFound), Toast.LENGTH_SHORT).show();
                    }
                });

                closeSocket();
                finish();

            } catch (IOException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class updateUIThread implements Runnable {

        private Message msg;
        ImageView v = null;

        public updateUIThread(Message message) {
            this.msg = message;
        }

        @Override
        public void run() {

            int pos1 = msg.getFirstImagePosition();
            int pos2 = msg.getSecondImagePosition();


            if(msg.isAborted()){
                closeSocket();
                dialogCreator(getString(R.string.abortedMsg),"").show();
            }

            if(msg.isRunning()){

                if(msg.isFaceUp()){

                    if(pos1 != -1) {
                        v = (ImageView) myAdapter.getItem(pos1);
                        if ( v != null) {
                            v.setImageBitmap(DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(pos1), 100, 100));
                            if(msg.isRightPair()){
                                v.setOnClickListener(null);
                            }
                        }
                    }

                    if(pos2 != -1) {
                        v = (ImageView) myAdapter.getItem(pos2);
                        if ( v != null) {
                            v.setImageBitmap(DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(pos2), 100, 100));
                            if(msg.isRightPair()){
                                v.setOnClickListener(null);
                            }
                        }
                    }
                } else{
                    if(pos1 != -1) {
                        v = (ImageView) myAdapter.getItem(pos1);
                        if ( v != null) {
                            v.setImageResource(R.drawable.back);
                        }
                    }

                    if(pos2 != -1) {
                        v = (ImageView) myAdapter.getItem(pos2);
                        if ( v != null) {
                            v.setImageResource(R.drawable.back);
                        }
                    }
                }

                if(server){
                    tvScore2.setText(getString(R.string.player2) + ": " + msg.getTheirScore());
                }else{
                    tvScore1.setText(getString(R.string.player1) + ": " + msg.getTheirScore());
                }

                oppponentScore = msg.getTheirScore();
                pairsDiscovered = msg.getnPairs();
            }else { // jogo acabou

                h.post(new Runnable() { //corer na UIPrincipal
                    @Override
                    public void run() {

                        if(score > msg.getTheirScore() )
                            dialogCreator(getString(R.string.youWin), getString(R.string.gameOverDialogHeader)).show();
                        else if( score < msg.getTheirScore())
                            dialogCreator(getString(R.string.youLost), getString(R.string.gameOverDialogHeader)).show();
                        else
                            dialogCreator(getString(R.string.draw), getString(R.string.gameOverDialogHeader)).show();
                    }
                });
            }
        }
    }

    private synchronized boolean getMyTurn(){
        return myTurn;
    }

    private synchronized void setMyTurn(boolean turn){
        myTurn = turn;
    }

    private void createLevel(){

        if(lvl == null) {
            Toast.makeText(this, "Error Loading Level", Toast.LENGTH_SHORT).show();
            return;
        }

        // - fill gridView with imageView
        gridView.setAdapter(myAdapter);
        gridView.setNumColumns(lvl.getColums());

        //Configura o jogo com as opcoes atuais
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean intruders = prefs.getBoolean("pref_intruders", true);

        if(intruders)
            pairsDiscovered = lvl.getnPairs();
        else{
            pairsDiscovered = lvl.getnPairs() - lvl.getIntruders().size();
        }

        int clickMode = Integer.parseInt(prefs.getString("click_mode", "1"));
        clickTime = Integer.parseInt(prefs.getString("click_time", "0"));

        defineClickMode(clickMode);

        boolean showTable = prefs.getBoolean("pref_show", true);

        if(showTable){

            myAdapter.setFaceUp(true);
            myAdapter.notifyDataSetChanged();

            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter.setFaceUp(false);
                            myAdapter.notifyDataSetChanged();
                        }
                    });

                    canGo = true;

                }
            }, 2000);
        }else
            canGo = true;
    }

    private boolean isIntruder(String pair, Level lvl){
        for ( String str : lvl.getIntruders()){
            if(str.compareToIgnoreCase(pair)==0)
                return true;
        }
        return false;
    }

    private Level getLevelFromFile(String levelName){

        ArrayList<Level> array;

        try{

            FileInputStream fin = new FileInputStream(getFilesDir() + "/array_levels");
            ObjectInputStream ois = new ObjectInputStream(fin);
            array = (ArrayList<Level>) ois.readObject();
            ois.close();

            for(Level lvl : array)
                if(lvl.getLevelName().compareToIgnoreCase(levelName) == 0)
                    return lvl;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }

        return null;
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void defineClickMode(int clickMode){

        switch (clickMode){
            case 1:
                doubleClick = false;
                defineOnClickListener();
                break;
            case 2:
                defineOnLongClickListener();
                break;
            case 3:
                doubleClick = true;
                defineOnClickListener();
                break;
        }
    }

    private void defineOnClickListener(){

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(!getMyTurn())
                    return;

                if(doubleClick) {
                    if (!doubleClickGo) {
                        pos = position;
                        doubleClickGo = true;

                        if(clickTime > 0) {
                            myTimer = new Timer();
                            myTimer.schedule(new TimerTask() {
                                @Override
                                public void run() { //caso passe o tempo definido para o doubleclick
                                    doubleClickGo = false;
                                }
                            }, clickTime);
                        }

                        return;
                    } else {
                        doubleClickGo = false;
                        if (pos != position)
                            return;
                    }
                }

                msg = new Message();

                if (firstFlip && canGo) {
                    imgFirstFlipped = (ImageView) view;
                    firstCardFlippedPosition = position;
                    firstCardFlipped = myAdapter.getSelectedImagePath(position);

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(firstCardFlipped, 100, 100));
                    firstFlip = false;

                    msg.setFirstImagePosition(position);
                    //msg.setSecondImagePosition(-1);
                    msg.setTheirScore(score);
                    msg.setFaceUp(true);
                    sendM(msg);

                }else{
                    if (!canGo)
                        return;
                    else
                        canGo = false;

                    if (firstCardFlippedPosition == position) {
                        canGo = true;
                        return;
                    }

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));

                    firstFlip = true;

                    if (myAdapter.getSelectedImagePath(position).compareToIgnoreCase(firstCardFlipped) == 0 &&
                            firstCardFlippedPosition != position) {

                        if (!isIntruder(firstCardFlipped, lvl)) {
                            if (server) {
                                score++;
                                tvScore1.setText(getString(R.string.player1) + ": " + score);
                            } else {
                                score++;
                                tvScore2.setText(getString(R.string.player2) + ": " + score);
                            }
                        }

                        canGo = true;
                        view.setOnClickListener(null); //dsabilita o click daquela posicao
                        imgFirstFlipped.setOnClickListener(null);

                        msg.setFaceUp(true);
                        msg.setTheirScore(score);
                        msg.setFirstImagePosition(firstCardFlippedPosition);
                        msg.setSecondImagePosition(position);
                        msg.setRightPair(true);
                        sendM(msg);

                        pairsDiscovered--;

                        if (pairsDiscovered == 0) { //FIM DE JOGO

                            h.post(new Runnable() { //corer na UIPrincipal
                                @Override
                                public void run() {

                                    if(score > oppponentScore )
                                        dialogCreator(getString(R.string.youWin), getString(R.string.gameOverDialogHeader)).show();
                                    else if( score < oppponentScore)
                                        dialogCreator(getString(R.string.youLost), getString(R.string.gameOverDialogHeader)).show();
                                    else
                                        dialogCreator(getString(R.string.draw), getString(R.string.gameOverDialogHeader)).show();
                                }
                            });

                            msg.setRunning(false);
                            msg.setTheirScore(score);
                            sendM(msg);
                        }
                    } else {

                        msg.setFaceUp(true);
                        msg.setTheirScore(score);
                        msg.setSecondImagePosition(position);
                        sendM(msg);

                        final View v2 = view;
                        final int positionThread = position;

                        myTimer = new Timer();
                        myTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgFirstFlipped.setImageResource(R.drawable.back);
                                        ((ImageView) v2).setImageResource(R.drawable.back);
                                    }
                                });

                                msg.setFirstImagePosition(firstCardFlippedPosition);
                                msg.setSecondImagePosition(positionThread);
                                msg.setnPairs(pairsDiscovered);
                                msg.setFaceUp(false);
                                msg.setYourTurn(true);
                                msg.setTheirScore(score);
                                sendM(msg);

                                setMyTurn(false);
                                canGo = true;

                            }
                        }, 1000);

                        if (server) {
                            Toast.makeText(MultiplayerActivity.this, getString(R.string.player2Turns), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MultiplayerActivity.this, getString(R.string.player1Turns), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void defineOnLongClickListener(){

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(!getMyTurn())
                    return false;

                msg = new Message();

                if (firstFlip && canGo) {
                    imgFirstFlipped = (ImageView) view;
                    firstCardFlippedPosition = position;
                    firstCardFlipped = myAdapter.getSelectedImagePath(position);

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(firstCardFlipped, 100, 100));
                    firstFlip = false;

                    msg.setFirstImagePosition(position);
                    msg.setTheirScore(score);
                    msg.setFaceUp(true);
                    sendM(msg);

                }else{
                    if (!canGo)
                        return false;
                    else
                        canGo = false;

                    if (firstCardFlippedPosition == position) {
                        canGo = true;
                        return false;
                    }

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));

                    firstFlip = true;

                    if (myAdapter.getSelectedImagePath(position).compareToIgnoreCase(firstCardFlipped) == 0 &&
                            firstCardFlippedPosition != position) {

                        if (!isIntruder(firstCardFlipped, lvl)) {
                            if (server) {
                                score++;
                                tvScore1.setText(getString(R.string.player1) + ": " + score);
                            } else {
                                score++;
                                tvScore2.setText(getString(R.string.player2) + ": " + score);
                            }
                        }

                        canGo = true;
                        view.setOnClickListener(null); //dsabilita o click daquela posicao
                        imgFirstFlipped.setOnClickListener(null);

                        msg.setFaceUp(true);
                        msg.setTheirScore(score);
                        msg.setFirstImagePosition(firstCardFlippedPosition);
                        msg.setSecondImagePosition(position);
                        msg.setRightPair(true);
                        sendM(msg);

                        pairsDiscovered--;

                        if (pairsDiscovered == 0) { //FIM DE JOGO

                            h.post(new Runnable() { //corer na UIPrincipal
                                @Override
                                public void run() {

                                    if(score > oppponentScore )
                                        dialogCreator(getString(R.string.youWin), getString(R.string.gameOverDialogHeader)).show();
                                    else if( score < oppponentScore)
                                        dialogCreator(getString(R.string.youLost), getString(R.string.gameOverDialogHeader)).show();
                                    else
                                        dialogCreator(getString(R.string.draw), getString(R.string.gameOverDialogHeader)).show();
                                }
                            });

                            msg.setRunning(false);
                            msg.setTheirScore(score);
                            sendM(msg);
                        }
                    } else {

                        msg.setFaceUp(true);
                        msg.setTheirScore(score);
                        msg.setSecondImagePosition(position);
                        sendM(msg);

                        final View v2 = view;
                        final int positionThread = position;

                        myTimer = new Timer();
                        myTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgFirstFlipped.setImageResource(R.drawable.back);
                                        ((ImageView) v2).setImageResource(R.drawable.back);
                                    }
                                });

                                msg.setFirstImagePosition(firstCardFlippedPosition);
                                msg.setSecondImagePosition(positionThread);
                                msg.setnPairs(pairsDiscovered);
                                msg.setFaceUp(false);
                                msg.setYourTurn(true);
                                msg.setTheirScore(score);
                                sendM(msg);

                                setMyTurn(false);
                                canGo = true;

                            }
                        }, 1000);

                        if (server) {
                            Toast.makeText(MultiplayerActivity.this, getString(R.string.player2Turns), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MultiplayerActivity.this, getString(R.string.player1Turns), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            }
        });

    }

    public void sendM(Message message){
        try {
            out.reset();
            //out.writeObject(message);
            out.writeUnshared(message);
            out.flush();
        }catch(IOException e) {
            e.printStackTrace();
            System.out.println("Erro na comunicacao como o socket " +
                    socket.getInetAddress().getHostAddress() + ":" +
                    socket.getPort() + "\n\t" + e);
        }
    }

    public Object receiveM(){

        try {
            return in.readObject();
        }catch(IOException e) {
            //e.printStackTrace();
            System.out.println("Erro na comunicacao como o socket " +
                    socket.getInetAddress().getHostAddress() + ":" +
                    socket.getPort()+"\n\t" + e);
            return null;

        }catch (ClassNotFoundException e){
            System.out.println("Pedido recebido de tipo inesperado:\n\t" + e);
            return null;
        }
    }

    private AlertDialog dialogCreator(String msg, String title){

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiplayerActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setNeutralButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MultiplayerActivity.this.finish();
            }
        });

        return builder.create();
    }

    private void closeSocket(){

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();

            }
            serverSocket = null;
        }
    }

}
