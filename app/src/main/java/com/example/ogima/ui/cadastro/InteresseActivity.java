package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class InteresseActivity extends AppCompatActivity {

    private Button btnContinuarInteresse;
    private ArrayList<String> arrayLista = new ArrayList<>();

    public String[] guardaInteresse = new String[29];
    private String guardaInteresses = "";

    public TextView textSubTitulo;

    private CheckBox btnAnimais, btnAnimes, btnAstrologia, btnAventuras, btnBebidas, btnBotanica, btnCantar,
            btnCarroMoto, btnCiclismo, btnCompras, btnCozinhar, btnDancar, btnDecoracao, btnDesfilar, btnEsportes,
            btnFestas, btnFilantropia, btnFilme, btnFotografia, btnLer, btnMalhacao, btnMeditar, btnModa, btnNatacao,
            btnNovela, btnSerie, btnTecnologia, btnViajar, btnVideogame;

    private int contadorAnimais = 0, contadorAnimes = 0, contadorAstrologia = 0, contadorAventuras = 0, contadorBebidas = 0,
            contadorBotanica = 0, contadorCantar = 0, contadorCarroMoto = 0, contadorCiclismo = 0, contadorCompras = 0, contadorCozinhar = 0, contador = 0,
            contadorDancar = 0, contadorDecoracao = 0, contadorDesfilar = 0, contadorEsportes = 0, contadorFestas = 0, contadorFilantropia = 0, contadorFilme = 0,
            contadorFotografia = 0, contadorLer = 0, contadorMalhacao = 0, contadorMeditar = 0, contadorModa = 0, contadorNatacao = 0, contadorNovela = 0,
            contadorSerie = 0, contadorTecnologia = 0, contadorViajar = 0, contadorVideogame = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_interesse);

        btnContinuarInteresse = findViewById(R.id.btnContinuarInteresse);
        btnAnimais = findViewById(R.id.btnAnimais);
        btnAnimes = findViewById(R.id.btnAnimes);
        btnAstrologia = findViewById(R.id.btnAstrologia);
        btnAventuras = findViewById(R.id.btnAventuras);
        btnBebidas = findViewById(R.id.btnBebidas);
        btnBotanica = findViewById(R.id.btnBotanica);
        btnCantar = findViewById(R.id.btnCantar);
        btnCarroMoto = findViewById(R.id.btnCarroMoto);
        btnCiclismo = findViewById(R.id.btnCiclismo);
        btnCompras = findViewById(R.id.btnCompras);
        btnCozinhar = findViewById(R.id.btnCozinhar);
        btnDancar = findViewById(R.id.btnDancar);
        btnDecoracao = findViewById(R.id.btnDecoracao);
        btnDesfilar = findViewById(R.id.btnDesfilar);
        btnEsportes = findViewById(R.id.btnEsportes);
        btnFestas = findViewById(R.id.btnFestas);
        btnFilantropia = findViewById(R.id.btnFilantropia);
        btnFilme = findViewById(R.id.btnFilme);
        btnFotografia = findViewById(R.id.btnFotografia);
        btnLer = findViewById(R.id.btnLer);
        btnMalhacao = findViewById(R.id.btnMalhacao);
        btnMeditar = findViewById(R.id.btnMeditar);
        btnModa = findViewById(R.id.btnModa);
        btnNatacao = findViewById(R.id.btnNatacao);
        btnNovela = findViewById(R.id.btnNovela);
        btnSerie = findViewById(R.id.btnSerie);
        btnTecnologia = findViewById(R.id.btnTecnologia);
        btnViajar = findViewById(R.id.btnViajar);
        btnVideogame = findViewById(R.id.btnVideogame);


        textSubTitulo = findViewById(R.id.textSubTitulo);



        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

        btnContinuarInteresse.setEnabled(false);


        if (btnContinuarInteresse != null) {
            btnContinuarInteresse.setEnabled(true);
        }


    }

    public void validarContador(View view) {

        //Método usado pelo botão de continuação.

        if (contador <= 0) {
            Toast.makeText(InteresseActivity.this, "Escolha pelo menos um interesse", Toast.LENGTH_SHORT).show();
        } else if (contador >= 1 && contador <= 5) {

            armazenarInteresse();
            Toast.makeText(InteresseActivity.this, "Uau você é incrível" + arrayLista, Toast.LENGTH_SHORT).show();

            //Recebendo dados Email/Senha/Nome/Apelido/Idade

            Bundle dados = getIntent().getExtras();
            Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

            Toast.makeText(InteresseActivity.this, "Email "
                    + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                    + " Nome " + usuario.getNomeUsuario() + " Apelido "
                    + usuario.getApelidoUsuario() + " Idade " + usuario.getIdade() +
                    " Nascimento " + usuario.getDataNascimento() + " Genêro " + usuario.getGeneroUsuario(), Toast.LENGTH_LONG).show();

            usuario.setInteresses(arrayLista);

            Intent intent = new Intent(getApplicationContext(), FotoPerfilActivity.class);
            intent.putExtra("dadosUsuario", usuario);
            startActivity(intent);

            //intent.putStringArrayListExtra("listaInteresse",arrayLista);


        }

        //Toast.makeText(InteresseActivity.this, "" + contador, Toast.LENGTH_SHORT).show();
       // Toast.makeText(InteresseActivity.this, "" + guardaInteresse[0], Toast.LENGTH_SHORT).show();

    }

    public void verificarMarca(View view) {



        if (btnAnimais.isChecked()) {
            contadorAnimais = 1;
            //letra = "A";
            calculo();
        } else if (!btnAnimais.isChecked()) {
            contadorAnimais = 0;
            calculo();
        }
        if (btnAnimes.isChecked()) {
            contadorAnimes = 1;
            //letra = letra + "B";
            //guardaInteresse [1] = "Animes";
            calculo();
        } else if (!btnAnimes.isChecked()) {
            contadorAnimes = 0;
            calculo();
        }
        if (btnAstrologia.isChecked()) {
            contadorAstrologia = 1;
            //letra = letra + "C";
            //guardaInteresse [2] = "Astrologia";
            calculo();
        } else if (!btnAstrologia.isChecked()) {
            contadorAstrologia = 0;
            calculo();
        }
        if (btnAventuras.isChecked()) {
            contadorAventuras = 1;
            //guardaInteresse [3] = "Aventura";
            calculo();
        } else if (!btnAventuras.isChecked()) {
            contadorAventuras = 0;
            calculo();
        }
        if (btnBebidas.isChecked()) {
            contadorBebidas = 1;
            //guardaInteresse [4] = "Bebidas";
            calculo();
        } else if (!btnBebidas.isChecked()) {
            contadorBebidas = 0;
            calculo();
        }
        if (btnBotanica.isChecked()) {
            contadorBotanica = 1;
            //guardaInteresse [5] = "Botânica";
            calculo();
        } else if (!btnBotanica.isChecked()) {
            contadorBotanica = 0;
            calculo();
        }
        if (btnCantar.isChecked()) {
            contadorCantar = 1;
            //guardaInteresse [6] = "Cantar";
            calculo();
        } else if (!btnCantar.isChecked()) {
            contadorCantar = 0;
            calculo();
        }
        if (btnCarroMoto.isChecked()) {
            contadorCarroMoto = 1;
            //guardaInteresse [7] = "Carro/Moto";
            calculo();
        } else if (!btnCarroMoto.isChecked()) {
            contadorCarroMoto = 0;
            calculo();
        }
        if (btnCiclismo.isChecked()) {
            contadorCiclismo = 1;
            //guardaInteresse [8] = "Ciclismo";
            calculo();
        } else if (!btnCiclismo.isChecked()) {
            contadorCiclismo = 0;
            calculo();
        }
        if (btnCompras.isChecked()) {
            contadorCompras = 1;
            //guardaInteresse [9] = "Compras";
            calculo();
        } else if (!btnCompras.isChecked()) {
            contadorCompras = 0;
            calculo();
        }
        if (btnCozinhar.isChecked()) {
            contadorCozinhar = 1;
            //guardaInteresse [10] = "Cozinhar";
            calculo();
        } else if (!btnCozinhar.isChecked()) {
            contadorCozinhar = 0;
            calculo();
        }
        if (btnDancar.isChecked()) {
            contadorDancar = 1;
            //guardaInteresse [11] = "Dança";
            calculo();
        } else if (!btnDancar.isChecked()) {
            contadorDancar = 0;
            calculo();
        }
        if (btnDecoracao.isChecked()) {
            contadorDecoracao = 1;
            //guardaInteresse [12] = "Decoração";
            calculo();
        } else if (!btnDecoracao.isChecked()) {
            contadorDecoracao = 0;
            calculo();
        }
        if (btnDesfilar.isChecked()) {
            contadorDesfilar = 1;
            //guardaInteresse [13] = "Desfilar";
            calculo();
        } else if (!btnDesfilar.isChecked()) {
            contadorDesfilar = 0;
            calculo();
        }
        if (btnEsportes.isChecked()) {
            contadorEsportes = 1;
            //guardaInteresse [14] = "Esportes";
            calculo();
        } else if (!btnEsportes.isChecked()) {
            contadorEsportes = 0;
            calculo();
        }
        if (btnFestas.isChecked()) {
            contadorFestas = 1;
            //guardaInteresse [15] = "Festas";
            calculo();
        } else if (!btnFestas.isChecked()) {
            contadorFestas = 0;
            calculo();
        }
        if (btnFilantropia.isChecked()) {
            contadorFilantropia = 1;
            //guardaInteresse [16] = "Filantropia";
            calculo();
        } else if (!btnFilantropia.isChecked()) {
            contadorFilantropia = 0;
            calculo();
        }
        if (btnFilme.isChecked()) {
            contadorFilme = 1;
            //guardaInteresse [17] = "Filmes";
            calculo();
        } else if (!btnFilme.isChecked()) {
            contadorFilme = 0;
            calculo();
        }
        if (btnFotografia.isChecked()) {
            contadorFotografia = 1;
            //guardaInteresse [18] = "Fotografia";
            calculo();
        } else if (!btnFotografia.isChecked()) {
            contadorFotografia = 0;
            calculo();
        }
        if (btnLer.isChecked()) {
            contadorLer = 1;
            //guardaInteresse [19] = "Ler";
            calculo();
        } else if (!btnLer.isChecked()) {
            contadorLer = 0;
            calculo();
        }
        if (btnMalhacao.isChecked()) {
            contadorMalhacao = 1;
            //guardaInteresse [20] = "Malhação";
            calculo();
        } else if (!btnMalhacao.isChecked()) {
            contadorMalhacao = 0;
            calculo();
        }
        if (btnMeditar.isChecked()) {
            contadorMeditar = 1;
            //guardaInteresse [21] = "Meditação";
            calculo();
        } else if (!btnMeditar.isChecked()) {
            contadorMeditar = 0;
            calculo();
        }
        if (btnModa.isChecked()) {
            contadorModa = 1;
            //guardaInteresse [22] = "Moda";
            calculo();
        } else if (!btnModa.isChecked()) {
            contadorModa = 0;
            calculo();
        }
        if (btnNatacao.isChecked()) {
            contadorNatacao = 1;
            //guardaInteresse [23] = "Natação";
            calculo();
        } else if (!btnNatacao.isChecked()) {
            contadorNatacao = 0;
            calculo();
        }
        if (btnNovela.isChecked()) {
            contadorNovela = 1;
            //guardaInteresse [24] = "Novela";
            calculo();
        } else if (!btnNovela.isChecked()) {
            contadorNovela = 0;
            calculo();
        }
        if (btnSerie.isChecked()) {
            contadorSerie = 1;
            //guardaInteresse [25] = "Séries";
            calculo();
        } else if (!btnSerie.isChecked()) {
            contadorSerie = 0;
            calculo();
        }
        if (btnTecnologia.isChecked()) {
            contadorTecnologia = 1;
            //guardaInteresse [26] = "Tecnologia";
            calculo();
        } else if (!btnTecnologia.isChecked()) {
            contadorTecnologia = 0;
            calculo();
        }
        if (btnViajar.isChecked()) {
            contadorViajar = 1;
            //guardaInteresse [27] = "Viajar";
            calculo();
        } else if (!btnViajar.isChecked()) {
            contadorViajar = 0;
            calculo();
        }
        if (btnVideogame.isChecked()) {
            contadorVideogame = 1;
            //guardaInteresse [28] = "VideoGame";
            calculo();
        } else if (!btnVideogame.isChecked()) {
            contadorVideogame = 0;
            calculo();
        }

        if (contador == 5) {
            Toast.makeText(InteresseActivity.this, "Para adicionar outro interesse, por favor desmarque algum deles", Toast.LENGTH_SHORT).show();

            if (!btnAnimais.isChecked()) {
                btnAnimais.setClickable(false);
            }
            if (!btnAnimes.isChecked()) {
                btnAnimes.setClickable(false);
            }
            if (!btnAstrologia.isChecked()) {
                btnAstrologia.setClickable(false);
            }

            if (!btnAventuras.isChecked()) {
                btnAventuras.setClickable(false);
            }
            if (!btnBebidas.isChecked()) {
                btnBebidas.setClickable(false);
            }
            if (!btnBotanica.isChecked()) {
                btnBotanica.setClickable(false);
            }
            if (!btnCantar.isChecked()) {
                btnCantar.setClickable(false);
            }
            if (!btnCarroMoto.isChecked()) {
                btnCarroMoto.setClickable(false);
            }
            if (!btnCiclismo.isChecked()) {
                btnCiclismo.setClickable(false);
            }
            if (!btnCompras.isChecked()) {
                btnCompras.setClickable(false);
            }
            if (!btnCozinhar.isChecked()) {
                btnCozinhar.setClickable(false);
            }
            if (!btnDancar.isChecked()) {
                btnDancar.setClickable(false);
            }
            if (!btnDecoracao.isChecked()) {
                btnDecoracao.setClickable(false);
            }
            if (!btnDesfilar.isChecked()) {
                btnDesfilar.setClickable(false);
            }
            if (!btnEsportes.isChecked()) {
                btnEsportes.setClickable(false);
            }
            if (!btnFestas.isChecked()) {
                btnFestas.setClickable(false);
            }
            if (!btnFilantropia.isChecked()) {
                btnFilantropia.setClickable(false);
            }
            if (!btnFilme.isChecked()) {
                btnFilme.setClickable(false);
            }
            if (!btnFotografia.isChecked()) {
                btnFotografia.setClickable(false);
            }
            if (!btnLer.isChecked()) {
                btnLer.setClickable(false);
            }
            if (!btnMalhacao.isChecked()) {
                btnMalhacao.setClickable(false);
            }
            if (!btnMeditar.isChecked()) {
                btnMeditar.setClickable(false);
            }
            if (!btnModa.isChecked()) {
                btnModa.setClickable(false);
            }
            if (!btnNatacao.isChecked()) {
                btnNatacao.setClickable(false);
            }
            if (!btnNovela.isChecked()) {
                btnNovela.setClickable(false);
            }
            if (!btnSerie.isChecked()) {
                btnSerie.setClickable(false);
            }
            if (!btnTecnologia.isChecked()) {
                btnTecnologia.setClickable(false);
            }
            if (!btnViajar.isChecked()) {
                btnViajar.setClickable(false);
            }
            if (!btnVideogame.isChecked()) {
                btnVideogame.setClickable(false);
            }


        }


    }


    public void calculo() {
        contador = contadorAnimais + contadorAnimes + contadorAstrologia + contadorAventuras + contadorBebidas + contadorBotanica + contadorCantar + contadorCarroMoto +
                contadorCiclismo + contadorCompras + contadorCozinhar + contadorDancar + contadorDecoracao + contadorDesfilar + contadorEsportes + contadorFestas +
                contadorFilantropia + contadorFilme + contadorFotografia + contadorLer + contadorMalhacao + contadorMeditar + contadorModa + contadorNatacao + contadorNovela +
                contadorSerie + contadorTecnologia + contadorViajar + contadorVideogame;

        if (contador < 5) {

            btnAnimes.setClickable(true);
            btnAnimais.setClickable(true);
            btnAnimes.setClickable(true);
            btnAstrologia.setClickable(true);
            btnAventuras.setClickable(true);
            btnBebidas.setClickable(true);
            btnBotanica.setClickable(true);
            btnCantar.setClickable(true);
            btnCarroMoto.setClickable(true);
            btnCiclismo.setClickable(true);
            btnCompras.setClickable(true);
            btnCozinhar.setClickable(true);
            btnDancar.setClickable(true);
            btnDecoracao.setClickable(true);
            btnDesfilar.setClickable(true);
            btnEsportes.setClickable(true);
            btnFestas.setClickable(true);
            btnFilantropia.setClickable(true);
            btnFilme.setClickable(true);
            btnFotografia.setClickable(true);
            btnLer.setClickable(true);
            btnMalhacao.setClickable(true);
            btnMeditar.setClickable(true);
            btnModa.setClickable(true);
            btnNatacao.setClickable(true);
            btnNovela.setClickable(true);
            btnSerie.setClickable(true);
            btnTecnologia.setClickable(true);
            btnViajar.setClickable(true);
            btnVideogame.setClickable(true);
        }


    }


    public void armazenarInteresse() {


        if (btnAnimais.isChecked()) {
            arrayLista.add("Animais");
        }
        if (btnAnimes.isChecked()){
            arrayLista.add("Animes");
        }
        if (btnAstrologia.isChecked()) {
            arrayLista.add("Astrologia");
        }
        if (btnAventuras.isChecked()) {
            arrayLista.add("Aventura");
        }
        if (btnBebidas.isChecked()) {
            arrayLista.add("Bebidas");
        }
        if (btnBotanica.isChecked()) {
            arrayLista.add("Botânica");
        }
        if (btnCantar.isChecked()) {
            arrayLista.add("Cantar");
        }
        if (btnCarroMoto.isChecked()) {
            arrayLista.add("Carro/Moto");
        }
        if (btnCiclismo.isChecked()) {
            arrayLista.add("Ciclismo");
        }
        if (btnCompras.isChecked()) {
            arrayLista.add("Compras");
        }
        if (btnCozinhar.isChecked()) {
            arrayLista.add("Cozinhar");
        }
        if (btnDancar.isChecked()) {
            arrayLista.add("Dança");
        }
        if (btnDecoracao.isChecked()) {
            arrayLista.add("Decoração");
        }
        if (btnDesfilar.isChecked()) {
            arrayLista.add("Desfilar");
        }
        if (btnEsportes.isChecked()) {
            arrayLista.add("Esportes");
        }
        if (btnFestas.isChecked()) {
            arrayLista.add("Festas");
        }
        if (btnFilantropia.isChecked()) {
            arrayLista.add("Filantropia");
        }
        if (btnFilme.isChecked()) {
            arrayLista.add("Filmes");
        }
        if (btnFotografia.isChecked()) {
            arrayLista.add("Fotografia");
        }
        if (btnLer.isChecked()) {
            arrayLista.add("Ler");
        }
        if (btnMalhacao.isChecked()) {
            arrayLista.add("Malhação");
        }
        if (btnMeditar.isChecked()) {
            arrayLista.add("Meditação");
        }
        if (btnModa.isChecked()) {
            arrayLista.add("Moda");
        }
        if (btnNatacao.isChecked()) {
            arrayLista.add("Natação");
        }
        if (btnNovela.isChecked()) {
            arrayLista.add("Novela");
        }
        if (btnSerie.isChecked()) {
            arrayLista.add("Séries");
        }
        if (btnTecnologia.isChecked()) {
            arrayLista.add("Tecnologia");
        }
        if (btnViajar.isChecked()) {
            arrayLista.add("Viajar");
        }
        if (btnVideogame.isChecked()) {
            arrayLista.add("VideoGame");
        }

    }

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }


}










