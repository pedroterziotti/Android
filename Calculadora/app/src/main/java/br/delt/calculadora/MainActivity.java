package br.delt.calculadora;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    String operationsDisp = "0";
    final String OPERATIONS = "-/%*+";
    final String OPERATIONSREGEX = "-|/|%|\\*|\\+";
    float result =0;
    TextView optText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        optText = findViewById(R.id.opText);
        optText.setText(operationsDisp);

        Button button = findViewById(R.id.buttonDel);
        button.setOnLongClickListener( new View.OnLongClickListener(){
            public boolean onLongClick(View view){
                operationsDisp="0";
                optText.setText(operationsDisp);
                return true;
            }
        });

    }

    public void onNumberOrOperatorClick( View view)
    {
        String value = ((Button) view).getText().toString();
        if(operationsDisp.substring(0) == "0") {
            if(value =="0") return;
            else operationsDisp= "";
        }
        operationsDisp += value;
        optText.setText(operationsDisp);
    }

    public void onBackspace(View view)
    {

        operationsDisp = operationsDisp.length()==1 ? "0": operationsDisp.substring(0,operationsDisp.length() -1);
        optText.setText(operationsDisp);
    }

    public void onEnter(View view){
        Enter();
        operationsDisp = String.valueOf(result);
        optText.setText(operationsDisp);
    }
    public void onCelsius(View view){
        Enter();
        result= (result-32)*5/9;
        operationsDisp = String.valueOf(result);
        optText.setText(operationsDisp);
    }

    public void Enter(){
        List<Float> numbers  = new ArrayList<Float>();
        for (String stringNum: operationsDisp.split(OPERATIONSREGEX)) {
            numbers.add(Float.parseFloat(stringNum));
        }
        List<Character> operations = new ArrayList<Character>();
        for (char chunk:operationsDisp.toCharArray()) {
            if(OPERATIONS.indexOf(chunk) != -1){
                operations.add(chunk);
            }
        }
        result = numbers.get(0);
        Log.d("DEBUG", String.valueOf(operations));
        for(int i =1; i <= operations.size(); i++){
           Log.d("DEBUG", String.valueOf(operations.get(i-1) =='+'));
            /*switch (operations.get(i-1)){
                case '+':
                    result=result+numbers.get(i);
                    Log.d("DEBUG", String.valueOf(result));
                case '-':
                    result-=numbers.get(i);
                case '/':
                    result/=numbers.get(i);
                case '*':
                    result*=numbers.get(i);
            }
            result=result;*/
            if(operations.get(i-1) =='+')
            {
                result+=numbers.get(i);
            } else if (operations.get(i-1) =='-') {
                result-=numbers.get(i);
            } else if (operations.get(i-1) =='/') {
                result/=numbers.get(i);
            } else if (operations.get(i-1) =='*') {
                result*=numbers.get(i);
            } else if (operations.get(i-1) =='%') {
                result=(int)(result/numbers.get(i));
            }
        }

    }



}