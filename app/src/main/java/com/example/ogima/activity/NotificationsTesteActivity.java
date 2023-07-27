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
import com.example.ogima.model.Notificacao;
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

    //Retrofit
    private Retrofit retrofit;
    private String baseUrl;
    private NotificacaoDados notificacaoDados;
    private Notificacao notificacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_teste);
        btnNotificacaoTeste = findViewById(R.id.btnNotificacaoTeste);

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //API do Firebase:
        baseUrl = "https://fcm.googleapis.com/fcm/";

        //Config retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        btnNotificacaoTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //O to pode ser também para tópicos - /topics/"tópicodesejadosemasaspas";

                FcmUtils.recuperarTokenAtual(new FcmUtils.RecuperarTokenCallback() {
                    @Override
                    public void onRecuperado(String token) {
                        notificacao = new Notificacao("Teste Retrofit", "Conteudo retrofit");
                        notificacaoDados = new NotificacaoDados(token, notificacao, new DataModel("Ana", "22"));

                        NotificationService notificationService = retrofit.create(NotificationService.class);
                        Call<NotificacaoDados> call = notificationService.salvarNotificacao(notificacaoDados);

                        call.enqueue(new Callback<NotificacaoDados>() {
                            @Override
                            public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                                ToastCustomizado.toastCustomizado("Chamado " + response.code(), getApplicationContext());

                                if (response.isSuccessful()) {
                                    ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar notificação", getApplicationContext());
                                }
                            }

                            @Override
                            public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });
    }
}