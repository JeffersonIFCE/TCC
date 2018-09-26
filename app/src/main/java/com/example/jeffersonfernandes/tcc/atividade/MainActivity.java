package com.example.jeffersonfernandes.tcc.atividade;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.jeffersonfernandes.tcc.R;
import com.example.jeffersonfernandes.tcc.dao.ValoresDAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    public String valor;
    static TextView textSpace;

    Button btnAquisicao;
    public int tempoAquisicao=0;
    public String[] datas = null;
    public String dataAtual;
    public int contDatas = 0;

    //Poderia ter sido utilizado qualquer Dao
    ValoresDAO valoresDAO;

    Messenger mService = null;
    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAquisicao = (Button) findViewById(R.id.btn_aquisicao);
        textSpace = (TextView) findViewById(R.id.textSpace);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);

        setSupportActionBar(toolbar);

        DrawerLayout mDrawlerLayout = (DrawerLayout) findViewById(R.id.navigation_drawler);
        ActionBarDrawerToggle mToogle = new ActionBarDrawerToggle(this, mDrawlerLayout, toolbar,R.string.open, R.string.close);
        mDrawlerLayout.addDrawerListener(mToogle);
        mToogle.syncState();

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Monitor Fotovoltaico");

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Que pena! Hardware Bluetooth não está funcionando", Toast.LENGTH_SHORT).show();
        } else {
            if(!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth ativado!", Toast.LENGTH_SHORT).show();
            }
        }

        //Limpar os dados quando se inicia o Aplicativo
        limparDadosApp();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.navigation_drawler);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Infla o menu com os botões do actionBar
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Seleção do ActionBar
        switch (item.getItemId()){
            case R.id.action_visibilidade:{
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                return true;
            }
            case R.id.action_dispositivos_pareados: {
                Intent searchPairedDevicesIntent = new Intent(this, PairedDevices.class);
                startActivityForResult(searchPairedDevicesIntent, SELECT_PAIRED_DEVICE);
                return true;
            }
            case R.id.action_dispositivos_proximos: {
                Intent searchPairedDevicesIntent = new Intent(this, DiscoveredDevices.class);
                startActivityForResult(searchPairedDevicesIntent, SELECT_DISCOVERED_DEVICE);
                return true;
            }
            case R.id.action_desconectar: {
                Intent i = new Intent(this, Servico.class);
                stopService(i);
                Toast.makeText(this, "Desconectado!", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.corrente){
            dataAtualString();
            if (contDatas>0){
                Intent i = new Intent(this, CorrenteClass.class);
                i.putExtra("dataAtual", dataAtual);
                startActivity(i);
            } else
                Toast.makeText(this, "Necessária aquisição diária!", Toast.LENGTH_LONG).show();
            contDatas = 0;
        } else if (item.getItemId()==R.id.potencia) {
            dataAtualString();
            if (contDatas > 0) {
                Intent i = new Intent(this, PotenciaClass.class);
                i.putExtra("dataAtual", dataAtual);
                startActivity(i);
            } else
                Toast.makeText(this, "Necessária aquisição diária!", Toast.LENGTH_LONG).show();
            contDatas = 0;
        } else if (item.getItemId()==R.id.temperatura){
            dataAtualString();
            if (contDatas>0){
                Intent i = new Intent(this, TemperaturaClass.class);
                i.putExtra("dataAtual", dataAtual);
                startActivity(i);
            } else
                Toast.makeText(this, "Necessária aquisição diária!", Toast.LENGTH_LONG).show();
            contDatas = 0;
        } else if (item.getItemId()==R.id.tensao){
            dataAtualString();
            if (contDatas>0){
                Intent i = new Intent(this, TensaoClass.class);
                i.putExtra("dataAtual", dataAtual);
                startActivity(i);
            } else
                Toast.makeText(this, "Necessária aquisição diária!", Toast.LENGTH_LONG).show();
            contDatas = 0;
        } else if (item.getItemId()==R.id.ajuda){
            Intent i = new Intent(this, Ajuda.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.navigation_drawler);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Verifica se foi ativado o bluetooth
        if(requestCode == ENABLE_BLUETOOTH) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth ativado!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Bluetooth não ativado!", Toast.LENGTH_LONG).show();
            }
        }

        //Retorna o dispositivo que foi selecionado para conexão
        else if(requestCode == SELECT_PAIRED_DEVICE || requestCode == SELECT_DISCOVERED_DEVICE) {
            if(resultCode == RESULT_OK) {

                Toast.makeText(getApplicationContext(), "Você selecionou " + data.getStringExtra("btDevName")
                        + "\n" + data.getStringExtra("btDevAddress"), Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this, Servico.class);
                i.putExtra("btDevAddress", data.getStringExtra("btDevAddress"));
                startService(i);

            }
            else {
                Toast.makeText(getApplicationContext(), "Nenhum dispositivo selecionado!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, Servico.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void DialogAquisicao (View view){

        Message msg = null;
        if (btnAquisicao.getText().toString().equals("Parar Aquisição")) {
            msg = Message.obtain(null, Servico.PARAR_AQUISICAO, 0, 0);
            btnAquisicao.setText("Aquisição");
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            ViewGroup mView = (ViewGroup) inflater.inflate(R.layout.dialog_aquisicao, null);

            final Spinner spinnerValor = (Spinner) mView.findViewById(R.id.spinnerValor);
            final Spinner spinnerTempo = (Spinner) mView.findViewById(R.id.spinnerTempoSalvar);

            ArrayAdapter adapterValor = ArrayAdapter.createFromResource(this, R.array.spinner_valor, android.R.layout.simple_spinner_item);
            spinnerValor.setAdapter(adapterValor);
            ArrayAdapter adapterTempo = ArrayAdapter.createFromResource(this, R.array.spinner_tempo_salvar, android.R.layout.simple_spinner_item);
            spinnerTempo.setAdapter(adapterTempo);

            mBuilder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            mBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String valor = spinnerValor.getSelectedItem().toString();
                    String tempo = spinnerTempo.getSelectedItem().toString();

                    //Defini a faixa de valores que a placa vai utilizar
                    String faixa;

                    if (valor.equals("25V / 2A"))
                        faixa = "1";
                    else if (valor.equals("50V / 4A"))
                        faixa = "2";
                    else
                        faixa = "3";

                    if (tempo.equals("1 minuto"))
                        tempoAquisicao = 60;
                    else if (tempo.equals("2 minutos"))
                        tempoAquisicao = 120;
                    else if (tempo.equals("5 minutos"))
                        tempoAquisicao = 300;
                    else if (tempo.equals("30 minutos"))
                        tempoAquisicao = 1800;
                    else if (tempo.equals("1 hora"))
                        tempoAquisicao = 3600;
                    else if (tempo.equals("2 horas"))
                        tempoAquisicao = 7200;
                    else if (tempo.equals("5 horas"))
                        tempoAquisicao = 18000;
                    else if (tempo.equals("7 horas"))
                        tempoAquisicao = 25200;
                    else if (tempo.equals("8 horas"))
                        tempoAquisicao = 28800;
                    else
                        tempoAquisicao = 43200;

                    Message msg = null;
                    //Enviar faixa de aquisição para o Handler da classe Serviço
                    if (!mBound) return;
                    if (faixa == "1") {
                        msg = Message.obtain(null, Servico.AQUISITAR1, 0, 0);
                        btnAquisicao.setText("Parar Aquisição");
                    } else if (faixa == "2") {
                        msg = Message.obtain(null, Servico.AQUISITAR2, 0, 0);
                        btnAquisicao.setText("Parar Aquisição");
                    } else if (faixa == "3") {
                        msg = Message.obtain(null, Servico.AQUISITAR3, 0, 0);
                        btnAquisicao.setText("Parar Aquisição");
                    }
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    //Enviar tempo de aquisição para o Handler da classe Serviço
                    if (tempoAquisicao == 60)
                        msg = Message.obtain(null, Servico.TEMPO1, 0, 0);
                    else if (tempoAquisicao == 120)
                        msg = Message.obtain(null, Servico.TEMPO2, 0, 0);
                    else if (tempoAquisicao == 300)
                        msg = Message.obtain(null, Servico.TEMPO3, 0, 0);
                    else if (tempoAquisicao == 1800)
                        msg = Message.obtain(null, Servico.TEMPO4, 0, 0);
                    else if (tempoAquisicao == 3600)
                        msg = Message.obtain(null, Servico.TEMPO5, 0, 0);
                    else if (tempoAquisicao == 7200)
                        msg = Message.obtain(null, Servico.TEMPO6, 0, 0);
                    else if (tempoAquisicao == 18000)
                        msg = Message.obtain(null, Servico.TEMPO7, 0, 0);
                    else if (tempoAquisicao == 25200)
                        msg = Message.obtain(null, Servico.TEMPO8, 0, 0);
                    else if (tempoAquisicao == 28800)
                        msg = Message.obtain(null, Servico.TEMPO9, 0, 0);
                    else if (tempoAquisicao == 43200)
                        msg = Message.obtain(null, Servico.TEMPO10, 0, 0);
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    public void DialogHistorico (View view) {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup mView = (ViewGroup) inflater.inflate(R.layout.dialog_historico, null);

        Button btn_exportar = (Button) mView.findViewById(R.id.btn_exportar);
        Button btn_gerarGrafico = (Button) mView.findViewById(R.id.btn_gerar_grafico);

        mBuilder.setPositiveButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        btn_exportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File arq, diretorio;
                byte[] dados;
                String texto = "Corrente      Temperatura      Tensão      Potência                 Data             Horário\n", nomeArquivo="valores", nomePasta;

                //Obter valores Corrente
                ValoresDAO valoresDAO = new ValoresDAO(MainActivity.this);
                String dataCorrente = valoresDAO.consultarCorrente();
                valoresDAO.close();
                String[] separarCorrentes = null;
                separarCorrentes = dataCorrente.split("\n");

                //Criar pasta e arquivo apenas se existir valores
               if (separarCorrentes.length > 1) {

                    //Obter valores Temperatura
                    ValoresDAO valoresDAO1 = new ValoresDAO(MainActivity.this);
                    String dataTemperatura = valoresDAO1.consultarTemperatura();
                    String[] separarTemperaturas = dataTemperatura.split("\n");

                    //Obter valores Tensão
                    String dataTensão = valoresDAO1.consultarTensao();
                    String[] separarTensões = dataTensão.split("\n");

                    //Obter valores Potência
                    String dataPotencia = valoresDAO1.consultarPotencia();
                    String[] separarPotencia = dataPotencia.split("\n");

                    //Obter Datas
                    String data = valoresDAO1.consultarData();
                    String[] datas = data.split("\n");

                    //Obter Horários
                    String h = valoresDAO1.consultarHora();
                    String[] horarios = h.split("\n");

                    valoresDAO1.close();

                    //O nome do arquivo possui data e hora atuais que está sendo gerado
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    Date data_atual = cal.getTime();

                    String data_completa = dateFormat.format(data_atual);
                    String hora_atual = dateFormat_hora.format(data_atual);
                    data_completa = data_completa.replace("/", "-");

                    nomeArquivo += data_completa + "-" + hora_atual + ".txt";

                    //Inseridos valores no arquivo de texto
                    for (int i = 0; i < separarCorrentes.length; i++) {
                        texto += separarCorrentes[i] + ",              " +
                                separarTemperaturas[i] +",              " +
                                separarTensões[i] + ",              " +
                                separarPotencia[i] +",              " +
                                datas[i] + "       " +
                                horarios[i] + "\n";
                    }

                   //Criando Pasta
                   nomePasta = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MonitorFotovoltaico/";

                   diretorio = new File(nomePasta);
                   if(!diretorio.exists())
                       diretorio.mkdirs();

                    //Criando o arquivo
                    arq = new File(nomePasta, nomeArquivo);
                    arq.getParentFile().mkdirs();

                    FileOutputStream fos = null;
                    dados = texto.getBytes();
                    try {
                        fos = new FileOutputStream(arq);
                        fos.write(dados);
                        fos.flush();
                        fos.close();
                        Toast.makeText(getApplicationContext(), "Salvo em: " + nomePasta + nomeArquivo, Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Erro", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Erro", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Sem valores para exportar!", Toast.LENGTH_SHORT).show();
            }
        });

        btn_gerarGrafico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHistoricoGrafico();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

    }

    public void DialogHistoricoGrafico () {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup mView = (ViewGroup) inflater.inflate(R.layout.dialog_historico_grafico, null);

        final Calendar calendario;
        final int dia, mes, ano, hora, minuto;

        calendario = Calendar.getInstance();
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        mes = calendario.get(Calendar.MONTH);
        ano = calendario.get(Calendar.YEAR);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minuto = calendario.get(Calendar.MINUTE);

        final RadioGroup radioGroup = (RadioGroup) mView.findViewById(R.id.radioGroup);
        final TextView dataInicio = (TextView) mView.findViewById(R.id.text_data_inicio_completa);
        final TextView dataFim = (TextView) mView.findViewById(R.id.text_data_fim_completa);
        final TextView horaInicio = (TextView) mView.findViewById(R.id.text_hora_inicio_completa);
        final TextView horaFim = (TextView) mView.findViewById(R.id.text_hora_fim_completa);


        dataInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month+=1;
                        dataInicio.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                }, ano, mes, dia);
                datePickerDialog.show();
            }
        });

        dataFim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month+=1;
                        dataFim.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                }, ano, mes, dia);
                datePickerDialog.show();
            }
        });

        horaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        horaInicio.setText(hourOfDay+":"+minute);
                    }
                }, hora, minuto, false);
                timePickerDialog.show();
            }
        });

        horaFim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        horaFim.setText(hourOfDay+":"+minute);
                    }
                }, hora, minuto, false);
                timePickerDialog.show();
            }
        });

        mBuilder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int item = radioGroup.getCheckedRadioButtonId();
                int retorno, retHora;

                if (horaInicio.getText().toString().equals("__:__:__") && !horaFim.getText().toString().equals("__:__:__")
                        || !horaInicio.getText().toString().equals("__:__:__") && horaFim.getText().toString().equals("__:__:__")){
                    Toast.makeText(getApplication(), "Selecione os horários!", Toast.LENGTH_SHORT).show();
                } else {
                    if (item==R.id.radioCorrente){
                        if (!dataInicio.getText().toString().equals("__/__/____") && !dataFim.getText().toString().equals("__/__/____")){
                            retorno = dataHistorico(dataInicio.getText().toString(), dataFim.getText().toString());
                            if (retorno==1){
                                if (!horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")
                                        && !dataInicio.getText().toString().equals(dataFim.getText().toString())){
                                    Toast.makeText(getApplication(), "Datas não podem ser iguais!", Toast.LENGTH_SHORT).show();
                                } else if (dataInicio.getText().toString().equals(dataFim.getText().toString())
                                        && !horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")){
                                    retHora = horaHistorico(horaInicio.getText().toString(), horaFim.getText().toString(), dataInicio.getText().toString());
                                    if (retHora==0)
                                        Toast.makeText(getApplication(), "Horários incorretos!", Toast.LENGTH_SHORT).show();
                                    else {
                                        if (contDatas>0){
                                            Intent i = new Intent(MainActivity.this, CorrenteClass.class);
                                            i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                            i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                            i.putExtra("horaMininaHistorico", horaInicio.getText().toString());
                                            i.putExtra("horaMaximaHistorico", horaFim.getText().toString());
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    if (contDatas>0){
                                        Intent i = new Intent(MainActivity.this, CorrenteClass.class);
                                        i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                        i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                contDatas=0;
                            } else
                                Toast.makeText(getApplication(), "Datas incorretas!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplication(), "Data não selecionada!", Toast.LENGTH_SHORT).show();
                    }
                    else if (item==R.id.radioPotencia) {
                        if (!dataInicio.getText().toString().equals("__/__/____") && !dataFim.getText().toString().equals("__/__/____")){
                            retorno = dataHistorico(dataInicio.getText().toString(), dataFim.getText().toString());
                            if (retorno==1){
                                if (!horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")
                                        && !dataInicio.getText().toString().equals(dataFim.getText().toString())){
                                    Toast.makeText(getApplication(), "Datas não podem ser iguais!", Toast.LENGTH_SHORT).show();
                                } else if (dataInicio.getText().toString().equals(dataFim.getText().toString())
                                        && !horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")){
                                    retHora = horaHistorico(horaInicio.getText().toString(), horaFim.getText().toString(), dataInicio.getText().toString());
                                    if (retHora==0)
                                        Toast.makeText(getApplication(), "Horários incorretos!", Toast.LENGTH_SHORT).show();
                                    else {
                                        if (contDatas>0){
                                            Intent i = new Intent(MainActivity.this, PotenciaClass.class);
                                            i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                            i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                            i.putExtra("horaMininaHistorico", horaInicio.getText().toString());
                                            i.putExtra("horaMaximaHistorico", horaFim.getText().toString());
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    if (contDatas>0){
                                        Intent i = new Intent(MainActivity.this, PotenciaClass.class);
                                        i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                        i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                contDatas=0;
                            } else
                                Toast.makeText(getApplication(), "Datas incorretas!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplication(), "Data não selecionada!", Toast.LENGTH_SHORT).show();
                    }
                    else if (item==R.id.radioTemperatura) {
                        if (!dataInicio.getText().toString().equals("__/__/____") && !dataFim.getText().toString().equals("__/__/____")){
                            retorno = dataHistorico(dataInicio.getText().toString(), dataFim.getText().toString());
                            if (retorno==1){
                                if (!horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")
                                        && !dataInicio.getText().toString().equals(dataFim.getText().toString())){
                                    Toast.makeText(getApplication(), "Datas não podem ser iguais!", Toast.LENGTH_SHORT).show();
                                } else if (dataInicio.getText().toString().equals(dataFim.getText().toString())
                                        && !horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")){
                                    retHora = horaHistorico(horaInicio.getText().toString(), horaFim.getText().toString(), dataInicio.getText().toString());
                                    if (retHora==0)
                                        Toast.makeText(getApplication(), "Horários incorretos!", Toast.LENGTH_SHORT).show();
                                    else {
                                        if (contDatas>0){
                                            Intent i = new Intent(MainActivity.this, TemperaturaClass.class);
                                            i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                            i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                            i.putExtra("horaMininaHistorico", horaInicio.getText().toString());
                                            i.putExtra("horaMaximaHistorico", horaFim.getText().toString());
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    if (contDatas>0){
                                        Intent i = new Intent(MainActivity.this, TemperaturaClass.class);
                                        i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                        i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                contDatas=0;
                            } else
                                Toast.makeText(getApplication(), "Datas incorretas!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplication(), "Data não selecionada!", Toast.LENGTH_SHORT).show();
                    }
                    else if (item==R.id.radioTensao) {
                        if (!dataInicio.getText().toString().equals("__/__/____") && !dataFim.getText().toString().equals("__/__/____")) {
                            retorno = dataHistorico(dataInicio.getText().toString(), dataFim.getText().toString());
                            if (retorno == 1) {
                                if (!horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")
                                        && !dataInicio.getText().toString().equals(dataFim.getText().toString())) {
                                    Toast.makeText(getApplication(), "Datas não podem ser iguais!", Toast.LENGTH_SHORT).show();
                                } else if (dataInicio.getText().toString().equals(dataFim.getText().toString())
                                        && !horaInicio.getText().toString().equals("__:__:__")
                                        && !horaFim.getText().toString().equals("__:__:__")) {
                                    retHora = horaHistorico(horaInicio.getText().toString(), horaFim.getText().toString(), dataInicio.getText().toString());
                                    if (retHora == 0)
                                        Toast.makeText(getApplication(), "Horários incorretos!", Toast.LENGTH_SHORT).show();
                                    else {
                                        if (contDatas > 0) {
                                            Intent i = new Intent(MainActivity.this, TensaoClass.class);
                                            i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                            i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                            i.putExtra("horaMininaHistorico", horaInicio.getText().toString());
                                            i.putExtra("horaMaximaHistorico", horaFim.getText().toString());
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    if (contDatas > 0) {
                                        Intent i = new Intent(MainActivity.this, TensaoClass.class);
                                        i.putExtra("dataMininaHistorico", dataInicio.getText().toString());
                                        i.putExtra("dataMaximaHistorico", dataFim.getText().toString());
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getApplication(), "Sem valores nesse período!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                contDatas = 0;
                            } else
                                Toast.makeText(getApplication(), "Datas incorretas!", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplication(), "Data não selecionada!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplication(), "Gráfico não selecionado!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

    }



    //Saber se existe valores diários para os gráficos
    public void dataAtualString(){

        //Pode ser utilizado qualquer tabela
        valoresDAO = new ValoresDAO(MainActivity.this);
        String data = valoresDAO.consultarData();
        valoresDAO.close();

        datas = data.split("\n");

        dataAtual = dataAtualFuncao();

        for (int i=0; i<datas.length; i++){
            if (datas[i].toString().equals(dataAtual))
                contDatas++;
        }

    }

    public int dataHistorico(String dataInicioCompleta, String dataFimCompleta){

        contDatas=0;
        //Pode ser utilizado qualquer tabela
        valoresDAO = new ValoresDAO(MainActivity.this);
        String data = valoresDAO.consultarData();
        valoresDAO.close();

        String[] dataHist = data.split("\n"), var;
        String[] dataMinima = dataInicioCompleta.split("/"), dataMaxima = dataFimCompleta.split("/");
        int[] dias = new int[dataHist.length], meses = new int[dataHist.length], anos = new int[dataHist.length],
                dataMin = new int[dataMinima.length], dataMax = new int[dataMaxima.length];

        for (int i=0; i<dataMinima.length; i++){
            dataMin[i] = Integer.parseInt(dataMinima[i]);
            dataMax[i] = Integer.parseInt(dataMaxima[i]);
        }

        if (dataHist.length < 2)
            dataHist=null;

        if (dataHist != null)
            for (int i = 0; i<dataHist.length; i++){
                var = dataHist[i].split("/");
                dias[i] = Integer.parseInt(var[0]);
                meses[i] = Integer.parseInt(var[1]);
                anos[i] = Integer.parseInt(var[2]);
                var = null;
            }

        if ((dataMin[1]-dataMax[1])>1)
            return 0;

        if (dataMin[2]>dataMax[2] || (dataMax[2]-dataMin[2])>1 ||
                dataMin[0]>dataMax[0] && dataMin[1]>=dataMax[1] && dataMin[2]>=dataMax[2] ||
                dataMin[0]>=dataMax[0] && dataMin[1]>dataMax[1] && dataMin[2]>=dataMax[2]
                || dataMin[2]>dataMax[2])
            return 0;

        if (dataHist != null)
            for (int i=0; i<dataHist.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    contDatas++;
                    break;
                }
            }
        return 1;
    }

    public int horaHistorico (String horaInicio, String horaFim, String date){

        contDatas = 0;

        valoresDAO = new ValoresDAO(MainActivity.this);
        String datas = valoresDAO.consultarData();
        String horarios = valoresDAO.consultarHora();
        valoresDAO.close();

        String[] data = datas.split("\n"), horas = horarios.split("\n"),
                horaMin = horaInicio.split(":"), horaMax = horaFim.split(":"), varIni, dataEscolhida, var;

        dataEscolhida = date.split("/");

        int horaIni = Integer.parseInt(horaMin[0]), minIni = Integer.parseInt(horaMin[1]),
                horaF = Integer.parseInt(horaMax[0]), minF = Integer.parseInt(horaMax[1]),
                hora = 0, minuto = 0, dateH, dateM;

        dateH = Integer.parseInt(dataEscolhida[0]);
        dateM = Integer.parseInt(dataEscolhida[1]);

        if (horaIni>horaF || horaIni==horaF && minIni>minF)
            return 0;

        if (data.length < 2)
            data = null;

        if (data != null)
            for (int i=0; i<data.length; i++) {
                var = data[i].split("/");
                if (dateH==Integer.parseInt(var[0]) && dateM==Integer.parseInt(var[1])){
                    varIni = horas[i].split(":");
                    hora = Integer.parseInt(varIni[0]);
                    minuto = Integer.parseInt(varIni[1]);

                    if (hora>=horaIni && hora<=horaF && minuto>=minIni && minuto<=minF){
                        contDatas++;
                        break;
                    }

                    else if (hora>=horaIni && hora<=horaF)
                        if (hora>horaIni && hora<horaF){
                            contDatas++;
                            break;
                        }
                        else if (hora==horaIni && minuto>=minIni){
                            if (hora<horaF){
                                contDatas++;
                                break;
                            }
                            else if (hora==horaF && minuto<=minF){
                                contDatas++;
                                break;
                            }
                        }
                }
                var = null;
            }

        return 1;

    }

    public void limparDadosApp (){

        //Obter id dos Valores
        valoresDAO = new ValoresDAO(MainActivity.this);
        String idValores = valoresDAO.consultarId();
        valoresDAO.close();
        String[] idC = null;
        idC = idValores.split("\n");

        if (idC.length > 1) {

            //Obter Datas
            ValoresDAO valoresDAO1 = new ValoresDAO(MainActivity.this);
            String data = valoresDAO1.consultarData();
            String[] var = null, datas = data.split("\n");

            int[] dias, meses, anos;
            int tamanho = datas.length;
            dias = new int[tamanho];
            meses = new int[tamanho];
            anos = new int[tamanho];

            for (int i = 0; i<datas.length; i++){
                var = datas[i].split("/");
                dias[i] = Integer.parseInt(var[0]);
                meses[i] = Integer.parseInt(var[1]);
                anos[i] = Integer.parseInt(var[2]);
                var = null;
            }

            //Data atual
            dataAtual = dataAtualFuncao();
            String[] dateAtual = dataAtual.split("/");
            int dia = Integer.parseInt(dateAtual[0]), mes = Integer.parseInt(dateAtual[1]),
                    ano = Integer.parseInt(dateAtual[2]);

            for (int i=0; i<datas.length; i++){
                if (ano == anos[i]){
                    if (meses[i] < mes && dias[i] <= dia || (mes - meses[i]) > 1){
                        valoresDAO1.excluirValores(idC[i]);
                    }
                } else if (ano > anos[i]){
                    if ((meses[i] - mes) < 11 || (meses[i] - mes) == 11 && dias[i] < dia){
                        valoresDAO1.excluirValores(idC[i]);
                    }
                }
            }
            valoresDAO1.close();

        }

    }

    public String dataAtualFuncao (){
        //Data atual
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Date data_atual = cal.getTime();

        return dateFormat.format(data_atual);
    }

}
