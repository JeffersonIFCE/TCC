package com.example.jeffersonfernandes.tcc.atividade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.example.jeffersonfernandes.tcc.R;
import com.example.jeffersonfernandes.tcc.dao.ValoresDAO;
import com.example.jeffersonfernandes.tcc.modelo.Valores;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Servico extends Service {

    String enderecoDispositivo="";
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    ThreadTempoAquisitar myThreadTempoAquisitar;

    Valores valoresModelo;
    ValoresDAO valoresDAO;

    long retornoDB;

    public String readMessage = "";
    public String valor;

    static final int PARAR_AQUISICAO = 0, AQUISITAR1 = 1, AQUISITAR2 = 2, AQUISITAR3 = 3;
    static final int TEMPO1 = 60, TEMPO2 = 120, TEMPO3 = 300, TEMPO4 = 1800, TEMPO5 = 3600, TEMPO6 = 7200, TEMPO7 = 18000, TEMPO8 = 25200, TEMPO9 = 28800, TEMPO10 = 43200;

    int faixaAquisicao = 0, tempoAquisicao = 0;
    boolean flagAquisitar = false;

    class FaixaHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARAR_AQUISICAO:
                    faixaAquisicao=0;
                    flagAquisitar = false;
                    Log.d("****Enviar", String.valueOf(faixaAquisicao));
                    EnviarFaixa();
                    break;
                case AQUISITAR1:
                    faixaAquisicao=1;
                    Log.d("****Enviar", String.valueOf(faixaAquisicao));
                    EnviarFaixa();
                    break;
                case AQUISITAR2:
                    faixaAquisicao=2;
                    Log.d("****Enviar", String.valueOf(faixaAquisicao));
                    EnviarFaixa();
                    break;
                case AQUISITAR3:
                    faixaAquisicao=3;
                    Log.d("****Enviar", String.valueOf(faixaAquisicao));
                    EnviarFaixa();
                    break;
                case TEMPO1:
                    tempoAquisicao = 60;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO2:
                    tempoAquisicao = 120;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO3:
                    tempoAquisicao = 300;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO4:
                    tempoAquisicao = 1800;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO5:
                    tempoAquisicao = 3600;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO6:
                    tempoAquisicao = 7200;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO7:
                    tempoAquisicao = 18000;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO8:
                    tempoAquisicao = 25200;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO9:
                    tempoAquisicao = 28800;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                case TEMPO10:
                    tempoAquisicao = 43200;
                    Log.d("****Tempo", String.valueOf(tempoAquisicao));
                    IniciarThreadTempoAquisitar(tempoAquisicao);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new FaixaHandler());

    public Servico() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        enderecoDispositivo = intent.getStringExtra("btDevAddress");
        Log.d("****", enderecoDispositivo);
        BluetoothDevice btDevice = btAdapter.getRemoteDevice(enderecoDispositivo);

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        myThreadConnectBTdevice = new ThreadConnectBTdevice(btDevice);
        myThreadConnectBTdevice.start();

        Log.d("****", "OnStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if(myThreadConnected!=null){
            myThreadConnected.cancel();
        }
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
        Log.d("****", "OnDestroy");
        super.onDestroy();
    }

    private void startThreadConnected(BluetoothSocket socket){
        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                //textSpace.setText("Conectando...");
                Log.d("****", "Conectando...");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                Log.d("****", "Conexão falhou");
                e.printStackTrace();

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            if(success){
                Log.d("****", "Conectado!");
                startThreadConnected(bluetoothSocket);
            }
        }
        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;


            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);

                    Log.d("****", strReceived);
                    if (readMessage.length()<21)
                        readMessage+=strReceived;
                    Log.d("****", readMessage);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Conexão perdida!";
                }

            }

        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
                Log.d("****Enviou", buffer.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ThreadTempoAquisitar extends Thread {

        private int tempoThread = 0;
        private int tempoTotalThread = 0;
        private int contador=0;

        private ThreadTempoAquisitar (int tempo){
            tempoTotalThread = tempo;
        }

        public void run (){
            flagAquisitar=true;
            if (flagAquisitar){

                //Serão gerados 15 pontos no gráfico
                if (tempoTotalThread == 60)
                    tempoThread = 4;
                    //Serão gerados 20 pontos no gráfico
                else if (tempoTotalThread == 120)
                    tempoThread = 6;
                else if (tempoTotalThread == 300)
                    tempoThread = 15;
                else if (tempoTotalThread == 1800)
                    tempoThread = 90;
                else if (tempoTotalThread == 3600)
                    tempoThread = 180;
                else if (tempoTotalThread == 7200)
                    tempoThread = 360;
                    //Serão gerados 25 pontos no gráfico
                else if (tempoTotalThread == 18000)
                    tempoThread = 720;
                else if (tempoTotalThread == 25200)
                    tempoThread = 1008;
                    //Serão gerados 30 pontos no gráfico
                else if (tempoTotalThread == 28800)
                    tempoThread = 960;
                else if (tempoTotalThread == 43200)
                    tempoThread = 1440;

                while (flagAquisitar){
                    try {
                        Log.d("salvar","chamou");
                        SalvarDados();
                        sleep(tempoThread*1000);
                        contador+=tempoThread;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (contador>=tempoTotalThread){
                        faixaAquisicao=0;
                        Log.d("****Enviar", String.valueOf(faixaAquisicao));
                        EnviarFaixa();
                        flagAquisitar = false;
                    }
                    Log.d("*****Contador", String.valueOf(contador));
                }

            } else {
                Log.d("*****", "Flag false");
            }
            NotificacaoUsuario("Aquisição finalizada");
            Log.d("*****", "Parou Aquisicao");
        }

    }

    public void SalvarDados (){
        readMessage = "06.12V 1.321A 27.13T";
        flagAquisitar = true;
        if (readMessage!="")
            if (readMessage.length()>18){
                if (flagAquisitar){
                    String[] valores = readMessage.split(" ");
                    String valorTensao = "", valorCorrente = "", valorTemperatura = "", valor;
                    int contTensao = 0, contCorrente = 0, contTemperatura = 0;

                    for (int i = 0; i < valores.length; i++){
                        if (valores[i].length()>4){
                            valor = valores[i].substring(0, valores[i].length()-1);
                            String letra = valores[i].substring(valores[i].length()-1, valores[i].length());
                            if (letra.equals("V")){
                                valorTensao = valor;
                                contTensao++;
                            } else if (letra.equals("A")){
                                valorCorrente = valor;
                                contCorrente++;
                            } else if (letra.equals("T")){
                                valorTemperatura = valor;
                                contTemperatura++;
                            }
                        }
                    }

                    Log.d("Dado","chamou");
                    if (contTensao !=0 && contCorrente !=0 && contTemperatura !=0){
                        Log.d("Dado","chamou2");
                        //Capturar hora e data atuais
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");

                        Date data = new Date();

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(data);
                        Date data_atual = cal.getTime();

                        String data_completa = dateFormat.format(data_atual);
                        String hora_atual = dateFormat_hora.format(data_atual);

                        /*ContentValues contentValues = new ContentValues();
                        contentValues.put(MyContentProvider.CORRENTE,Float.parseFloat(valorCorrente));
                        contentValues.put(MyContentProvider.POTENCIA,Float.parseFloat(valorTensao)*Float.parseFloat(valorCorrente));
                        contentValues.put(MyContentProvider.TEMPERATURA,Float.parseFloat(valorTemperatura));
                        contentValues.put(MyContentProvider.TENSAO,Float.parseFloat(valorTensao));
                        contentValues.put(MyContentProvider.DATA,data_completa);
                        contentValues.put(MyContentProvider.HORA,hora_atual);
                        Uri uri = getContentResolver().insert(MyContentProvider.CONTENT_URI,contentValues);

                        Log.d("Dado",uri.toString());*/

                        valoresModelo = new Valores();
                        valoresDAO = new ValoresDAO(this);
                        valoresModelo.setCorrente(Float.parseFloat(valorCorrente));
                        valoresModelo.setPotencia(Float.parseFloat(valorTensao)*Float.parseFloat(valorCorrente));
                        valoresModelo.setTemperatura(Float.parseFloat(valorTemperatura));
                        valoresModelo.setTensao(Float.parseFloat(valorTensao));
                        valoresModelo.setData(data_completa);
                        valoresModelo.setHora(hora_atual);
                        retornoDB = valoresDAO.salvarValores(valoresModelo);
                        valoresDAO.close();

                        if (retornoDB == -1)
                            Log.d("*****", "Erro ao armazenar!");
                        else
                            Log.d("*****", "Salvo com sucesso!");
                    }
                }
            }
        readMessage = "";
    }

    //Enviar a faixa que vai ser trabalhada para a plaquinha
    public void EnviarFaixa (){
        /*if (myThreadConnected==null)
            NotificacaoUsuario("Sem conexão para aquisição");
        else {
            if (flagAquisitar){
                flagAquisitar = false;
                NotificacaoUsuario("Aquisição finalizada");
            } else {
                flagAquisitar = true;
                if(myThreadConnected!=null){
                    byte[] enviar = String.valueOf(faixaAquisicao).getBytes();
                    myThreadConnected.write(enviar);
                    Log.d("****", "Enviou");
                    byte[] NewLine = "\n".getBytes();
                    myThreadConnected.write(NewLine);
                    if (faixaAquisicao!=0){
                        flagAquisitar = true;
                        NotificacaoUsuario("Aquisitando valores");
                    } else
                        NotificacaoUsuario("Aquisição finalizada");
                }
            }
        }*/
    }

    //Iniciar o tempo para aquisição de valores
    public void IniciarThreadTempoAquisitar (int temp){
        myThreadTempoAquisitar = new ThreadTempoAquisitar(temp);
        myThreadTempoAquisitar.start();
        Log.d("****", "Iniciou Tempo Aquisitar");
    }

    //Feedback para usuário
    public void NotificacaoUsuario (String mensagem){
        Intent i = new Intent(this, MainActivity.class);
        int id = 1;
        PendingIntent pi = PendingIntent.getActivity(getBaseContext(), id, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getBaseContext())
                .setContentTitle("Monitor Fotovoltaico")
                .setContentText(mensagem)
                .setSmallIcon(R.drawable.icone_potencia)
                .setContentIntent(pi).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

}
