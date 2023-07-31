package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.api.NotificationService;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FcmUtils;
import com.example.ogima.helper.NotificationsReceiver;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DataModel;
import com.example.ogima.model.NotificacaoDados;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationsTesteActivity extends AppCompatActivity {

    private Button btnNotificacaoTeste;
    private static final String TAG = "CONFIGFCM";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_teste);
        btnNotificacaoTeste = findViewById(R.id.btnNotificacaoTeste);

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        btnNotificacaoTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                FcmUtils fcmUtils = new FcmUtils();

                fcmUtils.prepararNotificacao(getApplicationContext(), "mensagem",
                        "ZnJhc2ticjFAZ21haWwuY29t", 77777L,"texto",
                        "Ana Fift", "esse anime é muito bom :3", new FcmUtils.NotificacaoCallback() {
                            @Override
                            public void onEnviado() {
                                ToastCustomizado.toastCustomizadoCurto("Notificação enviado com sucesso ^^ ", getApplicationContext());
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao enviar a notificação " + message, getApplicationContext());
                            }
                        });
                             */
            }
        });
    }
}