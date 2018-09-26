package com.example.jeffersonfernandes.tcc.atividade;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.jeffersonfernandes.tcc.R;
import com.example.jeffersonfernandes.tcc.dao.ValoresDAO;
import com.example.jeffersonfernandes.tcc.modelo.Valores;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class TensaoClass extends AppCompatActivity implements
        OnChartGestureListener, OnChartValueSelectedListener {

    ValoresDAO tensaoDao;
    private LineChart mChart;
    private LineDataSet setTensao;
    float maiorValor;
    String[] separar=null, datas, horarios, dataMinima = new String[3], dataMaxima = new String[3],
            var, horaMini = new String[2], horaMaxi = new String[2];
    int[] dataMin = new int[3], dataMax = new int[3], horaMin = new int[2], horaMax = new int[2],
            dias, meses, anos, horas, minutos;
    String dataAtual = "", dataMininaHistorico = "", dataMaximaHistorico = "",
            horaMinima = "", horaMaxima = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tensao_class);

        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("dataAtual") != null)
            dataAtual = bundle.getString("dataAtual");
        else if (bundle.getString("dataMininaHistorico") != null){
            dataMininaHistorico = bundle.getString("dataMininaHistorico");
            dataMinima = dataMininaHistorico.split("/");
            for (int i=0; i<dataMinima.length; i++)
                dataMin[i] = Integer.parseInt(dataMinima[i]);
        }
        if (bundle.getString("dataMaximaHistorico") != null){
            dataMaximaHistorico = bundle.getString("dataMaximaHistorico");
            dataMaxima = dataMaximaHistorico.split("/");
            for (int i=0; i<dataMaxima.length; i++)
                dataMax[i] = Integer.parseInt(dataMaxima[i]);
        }
        if (bundle.getString("horaMininaHistorico") != null){
            horaMinima = bundle.getString("horaMininaHistorico");
            horaMini = horaMinima.split(":");
            horaMin[0] = Integer.parseInt(horaMini[0]);
            horaMin[1] = Integer.parseInt(horaMini[1]);
        }
        if (bundle.getString("horaMaximaHistorico") != null){
            horaMaxima = bundle.getString("horaMaximaHistorico");
            horaMaxi = horaMaxima.split(":");
            horaMax[0] = Integer.parseInt(horaMaxi[0]);
            horaMax[1] = Integer.parseInt(horaMaxi[1]);
        }

        mChart = (LineChart) findViewById(R.id.lineChart);

        mChart.setOnChartGestureListener(TensaoClass.this);
        mChart.setOnChartValueSelectedListener(TensaoClass.this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        horariosString();
        valoresString();
        datasString();
        gerarGráfico();

    }

    public void datasString(){
        tensaoDao = new ValoresDAO(TensaoClass.this);
        String data = tensaoDao.consultarData();
        tensaoDao.close();

        datas = data.split("\n");

        if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")){
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
        }

        int tam = horarios.length;
        horas = new int[tam];
        minutos = new int[tam];

        for (int i = 0; i<horarios.length; i++){
            var = horarios[i].split(":");
            horas[i] = Integer.parseInt(var[0]);
            minutos[i] = Integer.parseInt(var[1]);
            var = null;
        }

    }

    public void horariosString (){
        tensaoDao = new ValoresDAO(TensaoClass.this);
        String h = tensaoDao.consultarHora();
        tensaoDao.close();

        horarios = h.split("\n");
    }

    public void valoresString (){
        tensaoDao = new ValoresDAO(TensaoClass.this);
        String data = tensaoDao.consultarTensao();
        tensaoDao.close();

        separar = data.split("\n");
    }

    public void gerarGráfico () {
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        String primeiroValor = null;

        //Verificar o horário do primeiro valor no período
        if (dataMininaHistorico.equals(""))
            for (int i=0; i<separar.length; i++){
                if (datas[i].toString().equals(dataAtual)){
                    primeiroValor = String.valueOf(horarios[i]);
                    break;
                }
            }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")
                && !horaMinima.equals("") && !horaMaxima.equals("")) {
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0] && minutos[i]>=horaMin[1] && minutos[i]<=horaMax[1]){
                        primeiroValor = String.valueOf(horarios[i]);
                        break;
                    } else if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0]){
                        if (horas[i]>horaMin[0] && horas[i]<horaMax[0]){
                            primeiroValor = String.valueOf(horarios[i]);
                            break;
                        } else if (horas[i]==horaMin[0] && minutos[i]>=horaMin[1]){
                            if (horas[i]<horaMax[0]){
                                primeiroValor = String.valueOf(horarios[i]);
                                break;
                            } else if (horas[i]==horaMax[0] && minutos[i]<=horaMax[1]){
                                primeiroValor = String.valueOf(horarios[i]);
                                break;
                            }
                        }
                    }
                }
            }
        }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")){
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    primeiroValor = String.valueOf(horarios[i]);
                    break;
                }
            }
        }

        //Verificar qual o maior valor no período escolhido, para adicionar no maximo do gráfico
        if (dataMininaHistorico.equals(""))
            for (int i=0; i<separar.length; i++){
                if (datas[i].toString().equals(dataAtual)){
                    Float valor = Float.parseFloat(separar[i]);
                    if(valor>maiorValor)
                        maiorValor=valor;
                }
            }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")
                && !horaMinima.equals("") && !horaMaxima.equals("")) {
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0] && minutos[i]>=horaMin[1] && minutos[i]<=horaMax[1]){
                        Float valor = Float.parseFloat(separar[i]);
                        if(valor>maiorValor)
                            maiorValor=valor;
                    } else if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0]){
                        if (horas[i]>horaMin[0] && horas[i]<horaMax[0]){
                            Float valor = Float.parseFloat(separar[i]);
                            if(valor>maiorValor)
                                maiorValor=valor;
                        } else if (horas[i]==horaMin[0] && minutos[i]>=horaMin[1]){
                            if (horas[i]<horaMax[0]){
                                Float valor = Float.parseFloat(separar[i]);
                                if(valor>maiorValor)
                                    maiorValor=valor;
                            } else if (horas[i]==horaMax[0] && minutos[i]<=horaMax[1]){
                                Float valor = Float.parseFloat(separar[i]);
                                if(valor>maiorValor)
                                    maiorValor=valor;
                            }
                        }
                    }
                }
            }
        }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")){
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    Float valor = Float.parseFloat(separar[i]);
                    if(valor>maiorValor)
                        maiorValor=valor;
                }
            }
        }

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMaximum(maiorValor);
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableAxisLineDashedLine(10f, 10f, 0f);
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getDescription().setText("");
        mChart.getXAxis().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);

        ArrayList<Entry> yValues = new ArrayList<>();

        //Valores da data atual
        if (dataMininaHistorico.equals(""))
            for (int i=0; i<separar.length; i++){
                if (datas[i].toString().equals(dataAtual)){
                    Float valor = Float.parseFloat(separar[i]);
                    yValues.add(new Entry(i,valor));
                }
            }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")
                && !horaMinima.equals("") && !horaMaxima.equals("")) {
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0] && minutos[i]>=horaMin[1] && minutos[i]<=horaMax[1]){
                        Float valor = Float.parseFloat(separar[i]);
                        yValues.add(new Entry(i,valor));
                    } else if (horas[i]>=horaMin[0] && horas[i]<=horaMax[0]){
                        if (horas[i]>horaMin[0] && horas[i]<horaMax[0]){
                            Float valor = Float.parseFloat(separar[i]);
                            yValues.add(new Entry(i,valor));
                        } else if (horas[i]==horaMin[0] && minutos[i]>=horaMin[1]){
                            if (horas[i]<horaMax[0]){
                                Float valor = Float.parseFloat(separar[i]);
                                yValues.add(new Entry(i,valor));
                            } else if (horas[i]==horaMax[0] && minutos[i]<=horaMax[1]){
                                Float valor = Float.parseFloat(separar[i]);
                                yValues.add(new Entry(i,valor));
                            }
                        }
                    }
                }
            }
        }
        else if (!dataMininaHistorico.equals("") && !dataMaximaHistorico.equals("")){
            for (int i=0; i<separar.length; i++){
                if (dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]>=dataMin[0] && meses[i]>=dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]>=dataMax[0] && meses[i]<dataMax[1] && anos[i]<=dataMax[2]
                        || dias[i]<=dataMin[0] && meses[i]>dataMin[1] && anos[i]>=dataMin[2]
                        && dias[i]<=dataMax[0] && meses[i]<=dataMax[1] && anos[i]<=dataMax[2]){
                    Float valor = Float.parseFloat(separar[i]);
                    yValues.add(new Entry(i,valor));
                }
            }
        }

        setTensao = new LineDataSet(yValues, "Início aquisição " + primeiroValor);

        setTensao.setFillAlpha(100);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setTensao);

        setTensao.setColor(Color.GRAY);
        setTensao.setCircleColor(Color.GRAY);
        setTensao.setLineWidth(2f);
        setTensao.setValueTextSize(0f);
        //setTensao.setValueTextColor(Color.BLACK);

        LineData data = new LineData(dataSets);

        mChart.setData(data);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}

    @Override
    public void onChartLongPressed(MotionEvent me) {}

    @Override
    public void onChartDoubleTapped(MotionEvent me) {}

    @Override
    public void onChartSingleTapped(MotionEvent me) {}

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        for (int i=0; i<separar.length; i++){
            //Mostrar apenas os dados da informação que foi selecionada
            if (i==e.getX()){
                Toast.makeText(getApplicationContext(), "Valor: " + separar[i]
                        + "\nHorário: " + horarios[i]
                        + "\nData: " + datas[i], Toast.LENGTH_LONG).show();
                break;
            }
        }

    }

    @Override
    public void onNothingSelected() {}
}
