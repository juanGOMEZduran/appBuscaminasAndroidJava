package com.example.buscaminas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private TextView cant, cantp, puntaje, puntMax, puntMin, temporizador;
    private Tablero fondo;
    String impcatidadGanadas;
    int x, y, cantidadGanadas=0, cantidadperidas=0, maxTemp=0;
    Integer maxCant = null;
    Integer minCant = null;
    private Casilla[][] casillas;
    private boolean activo = true;
    private CountDownTimer countDownTimer;
    long segundosTotales ;
    long minutos;
    long segundos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        puntMax=findViewById(R.id.puntMax);
        cant=findViewById(R.id.cant);
        cantp=findViewById(R.id.cantp);
        puntaje=findViewById(R.id.puntaje);
        temporizador=findViewById(R.id.temporizador);
        puntMin=findViewById(R.id.puntMin);
        puntaje.setText("0");

        LinearLayout main = findViewById(R.id.layaut1);
        fondo = new Tablero(this);
        fondo.setOnTouchListener(this);
        main.addView(fondo);
        casillas = new Casilla[8][8];

        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                casillas[f][c] = new Casilla();
            }
        }
        this.disponerBombas();
        this.contarBombasPerimetro();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        iniciarTemporizador();
    }

    private void iniciarTemporizador() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(30000, 1000) { // 30 segundos

            @Override
            public void onTick(long millisUntilFinished) {
                 segundosTotales = millisUntilFinished / 1000;
                 minutos = segundosTotales / 60;
                 segundos = segundosTotales % 60;

                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                temporizador.setText(tiempoFormateado);
            }

            @Override
            public void onFinish() {
                segundosTotales = 0;
                minutos = 0;
                segundos = 0;
                temporizador.setText("00:00");



                Toast.makeText(MainActivity.this, "¡Se acabó el tiempo!", Toast.LENGTH_SHORT).show();
                activo=false;
                fondo.invalidate();
            }
        }.start();
    }

    public void reiniciar(View v) {
        casillas = new Casilla[8][8];
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                casillas[f][c] = new Casilla();
            }
        }
        segundosTotales = 0;
        minutos = 0;
        segundos = 0;
        temporizador.setText("00:00");
        puntaje.setText("0");
        this.disponerBombas();
        this.contarBombasPerimetro();
        activo = true;


        iniciarTemporizador();

        fondo.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (activo) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int f = 0; f < 8; f++) {
                for (int c = 0; c < 8; c++) {
                    if (casillas[f][c].dentro(x, y)) {
                        casillas[f][c].destapado = true;
                        if (casillas[f][c].contenido == 80) {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            puntaje.setText("0");
                            segundosTotales = 0;
                            minutos = 0;
                            segundos = 0;
                            temporizador.setText("00:00");
                            MediaPlayer mp=MediaPlayer.create(this, R.raw.mine);
                            mp.start();
                            Toast.makeText(this, "Booooooooooommmmmmmmmm Perdiste", Toast.LENGTH_LONG).show();
                            activo = false;

                            cantidadperidas++;
                            String max=String.valueOf(maxCant);
                            puntMax.setText(max);
                            puntMin.setText(impcatidadGanadas);

                            impcatidadGanadas="0";
                            String impcatidadPerdida=String.valueOf(cantidadperidas);
                            cantp.setText(impcatidadPerdida);


                        } else if (casillas[f][c].contenido == 0) {
                            recorrer(f, c);
                        }
                        fondo.invalidate();
                        // Salimos de los bucles porque ya encontramos la casilla tocada
                        if (gano() && activo) {
                            MediaPlayer mp=MediaPlayer.create(this, R.raw.victoria);
                            mp.start();
                            Toast.makeText(this, "Ganaste!!!", Toast.LENGTH_LONG).show();
                            activo = false;
                            cantidadGanadas++;
                            puntaje.setText("0");
                            String max=String.valueOf(maxCant);
                            puntMax.setText(max);
                            if(maxCant>maxTemp){
                                maxTemp=maxCant;
                                String min=String.valueOf(maxTemp);
                                puntMin.setText(min);
                            }else {
                                puntMin.setText(impcatidadGanadas);
                            }
                            segundosTotales = 0;
                            minutos = 0;
                            segundos = 0;
                            temporizador.setText("00:00");



                            String impcatidadGanadas=String.valueOf(cantidadGanadas);
                            cant.setText(impcatidadGanadas);
                        }
                        return true;
                    }
                }
            }
        }
        return true;
    }

    class Tablero extends View{

        public Tablero(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            canvas.drawRGB(255, 255, 255);
            int ancho = 0;
            if (canvas.getWidth() < canvas.getHeight())
                ancho = fondo.getWidth();
            else
                ancho = fondo.getHeight();
            int anchocua = ancho / 8;
            Paint paint = new Paint();
            paint.setTextSize(50);
            Paint paint2 = new Paint();
            paint2.setTextSize(50);
            paint2.setTypeface(Typeface.DEFAULT_BOLD);
            paint2.setARGB(255, 0, 0, 255);//numero
            Paint paintlinea1 = new Paint();
            paintlinea1.setARGB(255, 255, 255, 255);
            int filaact = 0;
            for (int f = 0; f < 8; f++) {
                for (int c = 0; c < 8; c++) {
                    casillas[f][c].fijarxy(c * anchocua, filaact, anchocua);
                    if (casillas[f][c].destapado == false)

                        paint.setARGB(153, 204, 204, 204);//color de casilla sin seleccionar
                    else
                        paint.setARGB(255, 153, 153, 153);//color de casilla selecciona
                    canvas.drawRect(c * anchocua, filaact, c * anchocua
                            + anchocua - 2, filaact + anchocua - 2, paint);
                    // linea blanca
                    canvas.drawLine(c * anchocua, filaact, c * anchocua
                            + anchocua, filaact, paintlinea1);
                    canvas.drawLine(c * anchocua + anchocua - 1, filaact, c
                                    * anchocua + anchocua - 1, filaact + anchocua,
                            paintlinea1);

                    if (casillas[f][c].contenido >= 1
                            && casillas[f][c].contenido <= 8
                            && casillas[f][c].destapado)
                        canvas.drawText(
                                String.valueOf(casillas[f][c].contenido), c
                                        * anchocua + (anchocua / 2) - 8,
                                filaact + anchocua / 2, paint2);

                    if (casillas[f][c].contenido == 80
                            && casillas[f][c].destapado) {
                        segundosTotales = 0;
                        minutos = 0;
                        segundos = 0;
                        temporizador.setText("00:00");
                        puntaje.setText("0");
                        Paint bomba = new Paint();

                        bomba.setARGB(255, 255, 0, 0);
                        canvas.drawCircle(c * anchocua + (anchocua / 2),
                                filaact + (anchocua / 2), 8, bomba);
                    }

                }
                filaact = filaact + anchocua;
            }
        }
    }

    private void  disponerBombas(){
        int cantidad=8;
        do {
            int fila=(int) (Math.random() * 8);
            int columna=(int) (Math.random() *8);
            if(casillas[fila][columna].contenido==0){
                casillas[fila][columna].contenido=80;
                cantidad--;
            }
        }while (cantidad!=0);
    }

    private boolean gano(){
        int cant = 0;
        for (int f = 0; f < 8; f++)
            for (int c = 0; c < 8; c++)
                if (casillas[f][c].destapado){
                    cant++;
                    impcatidadGanadas=String.valueOf(cant);
                    puntaje.setText(impcatidadGanadas);
                    if (maxCant == null){
                        maxCant=cant;
                        minCant=cant;

                    }else {
                        maxCant = Math.max(maxCant, cant);
                        minCant = Math.min(minCant, cant);

                    }



                }
        if (cant == 56){

            return true;
        }

        else{

            return false;
        }


    }

    private  void contarBombasPerimetro(){
        for(int f=0; f<8; f++){
            for (int c=0; c<8; c++){
                if(casillas[f][c].contenido ==0){
                    int cant=contarCoodernada(f,c);
                    casillas[f][c].contenido=cant;
                }
            }
        }
    }

    int contarCoodernada(int fila, int columna){
        int total = 0;
        if (fila - 1 >= 0 && columna - 1 >= 0) {
            if (casillas[fila - 1][columna - 1].contenido == 80)
                total++;
        }
        if (fila - 1 >= 0) {
            if (casillas[fila - 1][columna].contenido == 80)
                total++;
        }
        if (fila - 1 >= 0 && columna + 1 < 8) {
            if (casillas[fila - 1][columna + 1].contenido == 80)
                total++;
        }

        if (columna + 1 < 8) {
            if (casillas[fila][columna + 1].contenido == 80)
                total++;
        }
        if (fila + 1 < 8 && columna + 1 < 8) {
            if (casillas[fila + 1][columna + 1].contenido == 80)
                total++;
        }

        if (fila + 1 < 8) {
            if (casillas[fila + 1][columna].contenido == 80)
                total++;
        }
        if (fila + 1 < 8 && columna - 1 >= 0) {
            if (casillas[fila + 1][columna - 1].contenido == 80)
                total++;
        }
        if (columna - 1 >= 0) {
            if (casillas[fila][columna - 1].contenido == 80)
                total++;
        }
        return total;
    }

    private void recorrer(int fil, int col){
        if (fil >= 0 && fil < 8 && col >= 0 && col < 8) {
            if (casillas[fil][col].contenido == 0) {
                casillas[fil][col].destapado = true;
                casillas[fil][col].contenido = 50;
                recorrer(fil, col + 1);
                recorrer(fil, col - 1);
                recorrer(fil + 1, col);
                recorrer(fil - 1, col);
                recorrer(fil - 1, col - 1);
                recorrer(fil - 1, col + 1);
                recorrer(fil + 1, col + 1);
                recorrer(fil + 1, col - 1);
            } else if (casillas[fil][col].contenido >= 1
                    && casillas[fil][col].contenido <= 8) {
                casillas[fil][col].destapado = true;
            }
        }
    }


}