# ServiceFlow

> Sistema desktop para gerenciamento de serviços, clientes e agendamentos.

<p align="center">
  <img src="https://img.shields.io/badge/vers%C3%A3o-3.3-blue" alt="Versão 3.3">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/JavaFX-21-007396" alt="JavaFX">
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-orange" alt="Em desenvolvimento">
</p>

## Sobre o projeto

O **ServiceFlow** é uma aplicação desktop desenvolvida em Java com JavaFX, criada para centralizar o gerenciamento de serviços, clientes, profissionais e agendamentos.

O projeto busca oferecer uma interface organizada, moderna e intuitiva, facilitando a visualização das informações e a navegação entre as principais áreas do sistema.

Atualmente, o desenvolvimento está concentrado na interface desktop e no aprimoramento da experiência de uso.

## Versão atual

**3.3**

## Funcionalidades

- **Dashboard:** visão geral das informações principais do sistema.
- **Agenda:** organização e visualização dos agendamentos.
- **Clientes:** área de cadastro e gerenciamento de clientes.
- **Serviços:** gerenciamento dos serviços oferecidos.
- **Profissionais:** área dedicada aos profissionais.
- **Relatórios:** visualização de indicadores e informações relacionadas aos serviços.
- **Configurações:** opções para configurar a aplicação.

> Observação: as funcionalidades estão em desenvolvimento e podem apresentar diferentes níveis de implementação nesta versão.

## Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| Java 17 | Linguagem principal |
| JavaFX 21 | Construção da interface desktop |
| FXML | Estruturação das telas |
| CSS | Estilização dos componentes |
| Maven | Gerenciamento de dependências e execução |
| Git | Controle de versão |

## Requisitos

Para executar o projeto, você precisará de:

- JDK 17 instalado.
- Maven instalado e configurado no PATH.
- Uma IDE compatível com Java, como IntelliJ IDEA, Eclipse ou Visual Studio Code com suporte a Java.

## Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/1Dioo/Serviceflow.git
```

### 2. Acessar a pasta do projeto

```bash
cd ServiceFlow
```

### 3. Executar a aplicação

```bash
mvn clean javafx:run
```

O Maven irá preparar o projeto, compilar os arquivos Java e iniciar a aplicação JavaFX.

> Caso o repositório esteja em outra URL ou o nome da pasta seja diferente, ajuste os comandos conforme a localização do seu projeto.

## Estrutura do projeto

A aplicação segue uma organização baseada em código Java, telas FXML e recursos visuais.

```text
ServiceFlow/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── serviceflow/
│       │           ├── controller/
│       │           └── ...
│       └── resources/
│           └── com/
│               └── serviceflow/
│                   ├── view/
│                   └── css/
├── pom.xml
└── README.md
```

- `src/main/java/`: classes Java e controladores.
- `src/main/resources/`: recursos utilizados pela aplicação.
- `view/`: arquivos FXML das telas.
- `css/`: folhas de estilo da interface.
- `pom.xml`: configuração do Maven e dependências.
- `README.md`: documentação do projeto.

## Estado do desenvolvimento

O ServiceFlow encontra-se **em desenvolvimento**.

As prioridades atuais incluem:

- Aprimoramento da interface desktop.
- Melhoria da navegação entre telas.
- Ajustes de responsividade para diferentes tamanhos de janela.
- Evolução das funcionalidades existentes.

Novas melhorias poderão ser incorporadas nas próximas versões.

## Autor

**Diogo**

- GitHub: [@1Dioo](https://github.com/1Dioo)

---

<p align="center">
  Desenvolvido com Java e JavaFX.
  <br>
  <strong>ServiceFlow · Versão 3.3</strong>
</p>
