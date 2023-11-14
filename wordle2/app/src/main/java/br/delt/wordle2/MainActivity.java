package br.delt.wordle2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public TableLayout table;
    static String palavra= "karma".toUpperCase();
    static String palavra_original= "karma".toUpperCase();
    JSONArray palavras;
    public static int[] cursor = {0,0}; // 6 linhas e 5 colunas

    final int limite_linhas =5;
    final int limite_colunas =4;
    PopupWindow popupWindow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Random rand = new Random();
        table = findViewById(R.id.table);
        try {

            JSONObject obj = new JSONObject(loadJSONFromAsset());
            palavras = obj.getJSONArray("palavras");
            palavra_original = palavras.get(rand.nextInt(palavras.length())).toString().toUpperCase();
            palavra = removeAccentsBeforeJava7(palavra_original);
        }
        catch (Exception e){}


    }
    public void cursorMover(View view)
    {
       cursor[1] = ((TableRow) view.getParent()).indexOfChild(view);
    }

    public void keyboardHandler(View view)
    {
        TextView textView = (TextView) view;
        TextView element = (TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(cursor[1]);
        element.setText(textView.getText().toString().toUpperCase());
        cursor[1] = cursor[1] >= limite_colunas ? cursor[1] : cursor[1]+1;
    }
    public void enterHandler(View view)
    {
        String tentativa="";
        for(int i=0; i <= limite_colunas;i++) //builds the word
        {
            TextView element = (TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(i);
            tentativa+=element.getText();
        }
        if(tentativa.length() < palavra.length() || !removeAccentsBeforeJava7(palavras.toString().toUpperCase()).contains(tentativa)) return; //checks if valid

        String editPalavra = palavra;
        for(int i=0; i <= limite_colunas;i++)
        {
            TextView element = (TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(i);
            try {

                if(element.getText().charAt(0) == palavra.toCharArray()[i] && editPalavra.contains(element.getText()))
                {
                    element.setBackground(new ColorDrawable(getResources().getColor(R.color.green,null)));
                    editPalavra=editPalavra.replaceFirst(element.getText().toString(),"");

                }
                else if (palavra.contains(element.getText()) && editPalavra.contains(element.getText()))
                {
                    element.setBackground(new ColorDrawable(getResources().getColor(R.color.yellow,null)));
                    editPalavra=editPalavra.replaceFirst(element.getText().toString(),"");
                }
                else {
                    element.setBackground(new ColorDrawable(getResources().getColor(R.color.black, null)));
                    //disables the button

                    TextView key =(TextView) findViewById(getResources().getIdentifier(element.getText().toString().toLowerCase(),"id",getPackageName()));
                    //key.setClickable(false);
                    key.setBackground(new ColorDrawable(getResources().getColor(R.color.keyboard_bg_off, null)));
                    key.setTextColor(getResources().getColor(R.color.black, null));
                }
            }
            catch (Exception e) {return;}
        }

        // Checks for win or loss
        if(palavra.toString().toUpperCase().trim().equals(tentativa.toString().toUpperCase().trim()))
        {

            LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.popup,null);

            ( (TextView) customView.findViewById(R.id.end_message)).setText("Parabéns! Você ganhou!");

            //instantiate popup window
            popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            //display the popup window
            popupWindow.showAtLocation(findViewById(R.id.constraintLayout), Gravity.CENTER, 0, 0);
            findViewById(R.id.keyboard).setClickable(false);
            return;
        }
        else if( cursor[0] >= limite_linhas)
        {

            LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.popup,null);

            ( (TextView) customView.findViewById(R.id.end_message)).setText("A palavra certa era:\n " + palavra);

            //instantiate popup window
            popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            //display the popup window
            popupWindow.showAtLocation(findViewById(R.id.constraintLayout), Gravity.CENTER, 0, 0);
            findViewById(R.id.keyboard).setClickable(false);
            return;
        }

        cursor[0] = cursor[0] > limite_linhas ? cursor[0] : cursor[0]+1;
        cursor[1]=0;
        if(cursor[0] > limite_linhas ) return;

       /// Disables current row and enables the next
        for(int i=0; i <= limite_colunas;i++)
        {
            ((TextView) ((TableRow) table.getChildAt(cursor[0]-1)).getChildAt(i)).setClickable(false);
            ((TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(i)).setClickable(true);
        }

    }

    public void backspaceHandler(View view)
    {
        TextView element = (TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(cursor[1]);
        if((element.getText()== "" ) && cursor[1] !=0)
        {
            cursor[1]--;
            element = (TextView) ((TableRow) table.getChildAt(cursor[0])).getChildAt(cursor[1]);
        }
        element.setText("");

    }

    public void reset(View view)
    {
        cursor = new int[]{0,0};
        finish();
        startActivity(getIntent());
    }

    public static String removeAccentsBeforeJava7(String value) {
        String normalizer = Normalizer.normalize(value, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
        return pattern.matcher(normalizer).replaceAll("");
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("palavras.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}