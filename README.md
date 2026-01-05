# Ogima - Rede Social Android (Java)

**Ogima** é um aplicativo de rede social nativo desenvolvido em Java. Este projeto foi meu primeiro projeto desenvolvido, onde ao longo de 3 anos ele serviu como forma de estudo prático e aplicação de conhecimentos em desenvolvimento Android, integração com Firebase e lógica de programação.

> **Status:** Projeto de Portfólio / Atualizado para Android SDK 31+

---

## 📺 Demonstração

Assista ao vídeo do aplicativo funcionando (Chat, Feed e Perfil):

[Demonstração completa do app] (https://youtu.be/ARD69cdaoqw)

---

## 📱 Funcionalidades e Implementações

O foco do projeto foi criar um aplicativo funcional com recursos reais de uma rede social.

### 1. Feed Personalizado
Criei uma lógica para não exibir apenas postagens aleatórias.
- O app pega os interesses do usuário no banco de dados.
- Realiza buscas separadas no Firebase para cada interesse.
- Mistura os resultados para criar um feed variado na tela inicial.

### 2. Chat em Tempo Real
Sistema de mensagens instantâneas entre usuários.
- **Sincronização de Hora:** Implementei uma verificação de horário online (NTP) para evitar erros caso o relógio do celular do usuário esteja errado, garantindo a ordem correta das mensagens.
- **Organização:** Estrutura de banco de dados separando as "Mensagens" da "Lista de Conversas" para o app carregar mais rápido.

### 3. Manutenção e Correções Recentes
Como o projeto tem alguns anos, precisei realizar atualizações importantes em 2025:
- **Bibliotecas:** Substituí bibliotecas que deixaram de existir (devido ao fim do JCenter) por arquivos locais (`.aar`) para o projeto voltar a compilar.
- **Correção de Bugs:** Ajustei problemas onde a lista de postagens ou o chat não carregavam corretamente na primeira abertura (erros de sincronia entre o banco de dados e a tela).

### 4. Sistema de Moedas e Anúncios
Implementei uma lógica de gamificação simples:
- O usuário assiste anúncios (AdMob) para ganhar moedas virtuais.
- Pode usar essas moedas para ver quem visitou seu perfil (funcionalidade de "Perfil Oculto" com efeito borrado).

---

## 🛠 Tecnologias Utilizadas

- **Linguagem:** Java
- **Banco de Dados:** Firebase Realtime Database
- **Login:** Firebase Auth (Email/Senha e Google)
- **Bibliotecas:** 
  - `Glide` (Imagens)
  - `Retrofit` (Requisições HTTP)
  - `ExoPlayer` (Vídeo)
  - `MediaRecorder` (Gravação de áudio nativa)

---

## 📸 Galeria do Projeto

### Onboarding e Autenticação
| Tela Inicial | Login | Cadastro |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/d31c02c9-87be-4874-b263-d345a96b9f19" width="200"> | <img src="https://github.com/user-attachments/assets/8eb689c6-8b67-4f7a-a83a-6a4c74dd9f73" width="200"> | <img src="https://github.com/user-attachments/assets/7a2a8da4-21b1-4d7f-8239-9654431d10a6" width="200"> |

### Perfil e Edição
| Perfil do Usuário | Edição de Perfil | Interesses |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/0da16701-7bfe-496f-9435-1162e95e4b27" width="200"> | <img src="https://github.com/user-attachments/assets/b156673d-2a22-41ec-bef6-78d0e6f6602b" width="200"> | <img src="https://github.com/user-attachments/assets/a2542100-3484-474e-b066-6814f380c9c7" width="200"> |

### Feed e Postagens
| Feed Principal | Criação de Post | Crop de Imagem |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/9d1d4e02-4a0f-485e-b9b0-1aa5afb17c98" width="200"> | <img src="https://github.com/user-attachments/assets/3b59f7f7-f1b5-475c-beba-22b5a590e009" width="200"> | <img src="https://github.com/user-attachments/assets/e452c70c-d87c-4819-9ca6-10fd191e12ad" width="200"> |

### Chat e Comunidades
| Lista de Conversas | Chat (Mídia) | Comunidades |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/ceabf74c-e87c-48fc-b54e-c9599eccf00d" width="200"> | <img src="https://github.com/user-attachments/assets/bbbbd03a-6939-4c52-885b-b958dbea0bd1" width="200"> | <img src="https://github.com/user-attachments/assets/0c4d9829-bc28-436f-b46c-9f84829fefd1" width="200"> |

### Recursos Especiais
| Perfil Oculto (Blur) | QR Code | Suporte a GIFs |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/8aa86380-adbd-4b5d-94f0-4b27d03eff93" width="200"> | <img src="https://github.com/user-attachments/assets/032743f9-8411-44f5-9212-54ac7694d97e" width="200"> | <img src="https://github.com/user-attachments/assets/5881dd0f-04fc-4e1f-b389-38bea49413e0" width="200"> |

---

Desenvolvido por **Rafael Benedet Fernandes**.








