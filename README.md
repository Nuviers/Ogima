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

Desenvolvido por **[Rafael Benedet Fernandes]**.
