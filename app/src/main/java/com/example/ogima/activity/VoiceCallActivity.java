package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.URL;

public class VoiceCallActivity extends AppCompatActivity {

    private Toolbar toolbarVoiceCall;
    private TextView txtViewNameVoiceCall, txtViewVoiceDuration;
    private ImageButton imgBtnBackVoiceCall, imgBtnFinishVoice,
            imgBtnMuteVoiceSound, imgBtnMuteMicVoice;
    private ImageView imgViewVoiceCall;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Usuario usuarioDestinatario;
    private DatabaseReference usuarioRef;
    private Mensagem talkKey;
    private String tipoChamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        inicializandoComponentes();
        toolbarVoiceCall.setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");
            talkKey = (Mensagem) dados.getSerializable("talkKeyMensagem");
            tipoChamada = dados.getString("tipoChamada");
            verificaUsuarioAtual();
        }

        imgBtnBackVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgBtnFinishVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

    }

    private void verificaUsuarioAtual() {
        usuarioRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        txtViewNameVoiceCall.setText(usuarioDestinatario.getNomeUsuario());

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);

                    if (usuarioAtual.getEpilepsia().equals("Sim")) {
                        GlideCustomizado.montarGlideEpilepsia(getApplicationContext(),
                                usuarioDestinatario.getMinhaFoto(),
                                imgViewVoiceCall,
                                android.R.color.transparent);
                    } else if (usuarioAtual.getEpilepsia().equals("Não")) {
                        GlideCustomizado.montarGlide(getApplicationContext(),
                                usuarioDestinatario.getMinhaFoto(),
                                imgViewVoiceCall,
                                android.R.color.transparent);
                    }

                    exibirChamada(usuarioAtual);
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void inicializandoComponentes() {
        toolbarVoiceCall = findViewById(R.id.toolbarVoiceCall);
        imgBtnBackVoiceCall = findViewById(R.id.imgBtnBackVoiceCall);
        imgViewVoiceCall = findViewById(R.id.imgViewVoiceCall);
        txtViewNameVoiceCall = findViewById(R.id.txtViewNameVoiceCall);
        imgBtnFinishVoice = findViewById(R.id.imgBtnFinishVoice);
        imgBtnMuteVoiceSound = findViewById(R.id.imgBtnMuteVoiceSound);
        imgBtnMuteMicVoice = findViewById(R.id.imgBtnMuteMicVoice);
        txtViewVoiceDuration = findViewById(R.id.txtViewVoiceDuration);
    }

    private void exibirChamada(Usuario usuarioLogado) {
        try {

            JitsiMeetUserInfo infosUser = new JitsiMeetUserInfo();

            infosUser.setDisplayName(usuarioLogado.getNomeUsuario());

            if (usuarioLogado.getEpilepsia().equals("Não")) {
                infosUser.setAvatar(new URL(usuarioLogado.getMinhaFoto()));
            }

            if (tipoChamada != null) {
                if (tipoChamada.equals("video")) {
                    JitsiMeetConferenceOptions options
                            = new JitsiMeetConferenceOptions.Builder()
                            .setUserInfo(infosUser)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("recording.enabled", false)
                            .setRoom("Room " + talkKey.getTalkKey())
                            .setVideoMuted(false)
                            .build();
                    JitsiMeetActivity.launch(this, options);
                } else if (tipoChamada.equals("voz")) {
                    JitsiMeetConferenceOptions options
                            = new JitsiMeetConferenceOptions.Builder()
                            .setUserInfo(infosUser)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("recording.enabled", false)
                            .setFeatureFlag("video-mute.enabled", false)
                            .setRoom("Room " + talkKey.getTalkKey())
                            .setVideoMuted(true)
                            .build();
                    JitsiMeetActivity.launch(this, options);
                }
            } else {
                ToastCustomizado.toastCustomizadoCurto("Nuloo chamada", getApplicationContext());
            }
        } catch (Exception ex) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro " + ex.getMessage(), getApplicationContext());
        }

    }
}