package com.example.languagetranslatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micTV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    String[] fromLanguages={"From","Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan",
            "Czech", "Welsh", "Danish", "German", "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian",
            "Finnish", "French", "Irish", "Galician", "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian",
            "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Georgian", "Kannada", "Korean",
            "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay", "Maltese", "Dutch", "Norwegian", "Polish",
            "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Tamil",
            "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu", "Vietnamese"};

    String[] toLanguages={"To","Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan",
            "Czech", "Welsh", "Danish", "German", "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian",
            "Finnish", "French", "Irish", "Galician", "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian",
            "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Georgian", "Kannada", "Korean",
            "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay", "Maltese", "Dutch", "Norwegian", "Polish",
            "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Tamil",
            "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu", "Vietnamese"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode,fromlanguageCode,tolanguageCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner=findViewById(R.id.idFromSpinner);
        toSpinner=findViewById(R.id.idToSpinner);
        sourceEdt=findViewById(R.id.idEdtSource);
        micTV=findViewById(R.id.idTVMic);
        translateBtn=findViewById(R.id.idBtnTranslate);
        translatedTV=findViewById(R.id.idTVTranslatedTV);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlanguageCode=getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter=new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tolanguageCode=getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter=new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if(sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your text to translate...", Toast.LENGTH_SHORT).show();
                }else if(fromlanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                }else if(tolanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select target language", Toast.LENGTH_SHORT).show();
                }else{
                    translateText(fromlanguageCode,tolanguageCode,sourceEdt.getText().toString());
                }
            }
        });
        micTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                 i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                 i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                 i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                 try{
                     startActivityForResult(i,REQUEST_PERMISSION_CODE);
                 }catch(Exception e){
                     e.printStackTrace();
                     Toast.makeText(MainActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
                 }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }

    private void translateText(int fromlanguageCode, int tolanguageCode, String source){
        translatedTV.setText("Downloading Modal...");
        FirebaseTranslatorOptions options=new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromlanguageCode).setTargetLanguage(tolanguageCode).build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to translate: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to download Language Modal: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private int getLanguageCode(String language){
        int languageCode=0;
        switch(language){
            case "African":
                languageCode= FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case "Danish":
                languageCode= FirebaseTranslateLanguage.DA;
                break;
            case "German":
                languageCode= FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                languageCode= FirebaseTranslateLanguage.EL;
                break;
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Esperanto":
                languageCode= FirebaseTranslateLanguage.EO;
                break;
            case "Spanish":
                languageCode= FirebaseTranslateLanguage.ES;
                break;
            case "Estonian":
                languageCode= FirebaseTranslateLanguage.ET;
                break;
            case "Persian":
                languageCode= FirebaseTranslateLanguage.FA;
                break;
            case "Finnish":
                languageCode= FirebaseTranslateLanguage.FI;
                break;
            case "French":
                languageCode= FirebaseTranslateLanguage.FR;
                break;
            case "Irish":
                languageCode= FirebaseTranslateLanguage.GA;
                break;
            case "Galician":
                languageCode= FirebaseTranslateLanguage.GL;
                break;
            case "Gujarati":
                languageCode= FirebaseTranslateLanguage.GU;
                break;
            case "Hebrew":
                languageCode= FirebaseTranslateLanguage.HE;
                break;
            case "Haitian":
                languageCode= FirebaseTranslateLanguage.HT;
                break;
            case "Croatian":
                languageCode= FirebaseTranslateLanguage.HR;
                break;
            case "Hungarian":
                languageCode= FirebaseTranslateLanguage.HU;
                break;
            case "Indonesian":
                languageCode= FirebaseTranslateLanguage.ID;
                break;
            case "Icelandic":
                languageCode= FirebaseTranslateLanguage.IS;
                break;
            case "Italian":
                languageCode= FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode= FirebaseTranslateLanguage.JA;
                break;
            case "Georgian":
                languageCode= FirebaseTranslateLanguage.KA;
                break;
            case "Kannada":
                languageCode= FirebaseTranslateLanguage.KN;
                break;
            case "Korean":
                languageCode= FirebaseTranslateLanguage.KO;
                break;
            case "Lithuanian":
                languageCode= FirebaseTranslateLanguage.LT;
                break;
            case "Latvian":
                languageCode= FirebaseTranslateLanguage.LV;
                break;
            case "Macedonian":
                languageCode= FirebaseTranslateLanguage.MK;
                break;
            case "Marathi":
                languageCode= FirebaseTranslateLanguage.MR;
                break;
            case "Malay":
                languageCode= FirebaseTranslateLanguage.MS;
                break;
            case "Maltese":
                languageCode= FirebaseTranslateLanguage.MT;
                break;
            case "Dutch":
                languageCode= FirebaseTranslateLanguage.NL;
                break;
            case "Norwegian":
                languageCode= FirebaseTranslateLanguage.NO;
                break;
            case "Polish":
                languageCode= FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languageCode= FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languageCode= FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languageCode= FirebaseTranslateLanguage.RU;
                break;
            case "Slovak":
                languageCode= FirebaseTranslateLanguage.SK;
                break;
            case "Slovenian":
                languageCode= FirebaseTranslateLanguage.SL;
                break;
            case "Albanian":
                languageCode= FirebaseTranslateLanguage.SQ;
                break;
            case "Swedish":
                languageCode= FirebaseTranslateLanguage.SV;
                break;
            case "Swahili":
                languageCode= FirebaseTranslateLanguage.SW;
                break;
            case "Tamil":
                languageCode= FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languageCode= FirebaseTranslateLanguage.TE;
                break;
            case "Thai":
                languageCode= FirebaseTranslateLanguage.TH;
                break;
            case "Tagalog":
                languageCode= FirebaseTranslateLanguage.TL;
                break;
            case "Turkish":
                languageCode= FirebaseTranslateLanguage.TR;
                break;
            case "Ukranian":
                languageCode= FirebaseTranslateLanguage.UK;
                break;
            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
            case "Vietnamese":
                languageCode= FirebaseTranslateLanguage.VI;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }
}