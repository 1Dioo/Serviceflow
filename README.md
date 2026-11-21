# ServiceFlow

> Sistema desktop para gerenciamento de serviços, clientes e agendamentos.

<p align="center">
  <img src="https://img.shields.io/badge/vers%C3%A3o-3.6.1-blue" alt="Versão 3.6.1">
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

**3.6.1**

O projeto segue o [versionamento semântico](https://semver.org/lang/pt-BR/) (`MAJOR.MINOR.PATCH`).

## Novidades da versão 3.6.1

**Padronização visual e do tema**

Versão de correção visual: nenhuma funcionalidade ou regra de negócio foi alterada.

- O painel administrativo passou a usar o mesmo tema escuro da janela principal. A moldura clara em volta das telas foi removida, e a barra lateral ficou igual nas duas janelas.
- A tela **Funcionários**, que usava um tema claro com tabela no estilo padrão do JavaFX, foi convertida para o tema escuro com os mesmos cartões, campo de busca, tabela e botões das demais telas.
- O **azul**, que já era a cor de destaque em Configurações, passou a ser usado em todos os botões principais (Entrar, Criar conta, Novo agendamento, Novo serviço, Salvar etc.), no foco dos campos e na seleção de listas e tabelas.
- Botões padronizados: principal (azul), secundário (cancelar/voltar), perigo (excluir) e pequenos (Editar/Excluir nas linhas), com a mesma altura, cantos e tipografia.
- Campos de texto, senha, áreas de texto, listas de seleção e datas passaram a ter a mesma altura, borda e estado de foco em todas as telas, inclusive em Configurações, Relatórios e Profissionais.
- O menu lateral agora destaca a seção aberta, com um marcador azul, na janela principal e no painel administrativo.
- Espaçamentos, cantos arredondados e tamanhos de texto foram uniformizados entre as telas.
- O contraste dos textos auxiliares foi aumentado para melhorar a legibilidade.
- As cores do tema foram centralizadas em uma única paleta (`css/base.css`), e os componentes reutilizáveis, em `css/components.css`.

## Novidades da versão 3.6.0

**Agenda e Serviços no painel administrativo**

- As seções **Agenda** e **Serviços** do painel administrativo, que antes exibiam o aviso "será integrada posteriormente", agora funcionam.
- O painel reutiliza as mesmas telas e os mesmos dados da janela principal: os agendamentos exibidos, com cliente, serviço, horário, duração e preço, são os mesmos, e as alterações feitas em uma janela aparecem na outra.
- Na Agenda do painel valem as mesmas regras de horário de funcionamento e de conflito de horários.
- Em Serviços, o administrador pode criar, editar e excluir serviços sem sair do painel, com as regras atuais: nome não pode repetir, o preço é calculado pela duração e a exclusão não altera agendamentos já registrados.
- Somente contas ADMIN acessam o painel. Ele só abre as telas previstas para ele, e as regras de acesso passaram a ficar centralizadas em um único lugar.
- Ao sair da conta administrativa, a navegação do painel é encerrada junto com a sessão.
- A seção **Clientes** do painel continua indisponível nesta versão.

## Novidades da versão 3.5.0

**Validação do horário de funcionamento**

- Os agendamentos passaram a respeitar o expediente do ServiceFlow: **das 08:00 às 18:00**, o mesmo horário que já era usado na grade da Agenda e nos valores padrão da tela de Configurações.
- Não é possível agendar um atendimento que comece antes da abertura ou que termine depois do fechamento. Atendimentos que terminam exatamente às 18:00 são permitidos.
- A validação considera a duração completa do serviço. Por exemplo, um serviço de 4 horas pode começar até as 14:00.
- As mesmas regras valem na criação e na edição de agendamentos, inclusive quando a edição muda o horário ou troca o serviço por outro mais longo.
- Quando o horário é inválido, a mensagem informa o motivo e, quando o atendimento passaria do fechamento, o último horário de início possível para aquela duração.
- O formulário de agendamento oferece horários de início das 08:00 às 17:30 (o horário de 18:00, que nunca seria válido, foi removido da lista).
- As validações também são aplicadas internamente ao salvar, e não apenas no formulário.
- A verificação de conflitos entre agendamentos continua igual.
- Ainda não há restrição por dia da semana, e o horário não pode ser alterado pela tela de Configurações, que continua sem salvar os valores.

## Novidades da versão 3.4.0

**Agendamentos vinculados aos clientes cadastrados**

- O campo de cliente do agendamento deixou de ser texto livre: agora é um seletor com os clientes cadastrados, em ordem alfabética, exibindo nome e telefone para diferenciar clientes com o mesmo nome.
- Selecionar um cliente cadastrado passou a ser obrigatório para salvar um agendamento. Agendamentos sem cliente ou com cliente inexistente são bloqueados.
- O vínculo usa uma identificação única de cada cliente, e não o nome exibido. Ao editar um agendamento, o cliente associado já aparece selecionado, e é possível trocá-lo por outro cliente cadastrado.
- Alterações no cadastro do cliente (como o nome) aparecem automaticamente nos agendamentos dele.
- Um cliente com agendamentos não pode ser excluído: o sistema informa quantos agendamentos estão vinculados e orienta a excluí-los ou trocar o cliente deles antes. Nenhum agendamento é apagado automaticamente.
- As regras de serviços, duração, preço e conflito de horários continuam as mesmas.

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

## Configuração do modo administrativo

A conta administrativa **não possui senha no código-fonte**. Ela é criada ao iniciar a aplicação somente se uma senha válida for informada pelo ambiente de execução.

| Configuração | Origem | Obrigatória | Descrição |
|---|---|---|---|
| `-Dserviceflow.admin.password` | Propriedade da JVM | Sim (ou a variável abaixo) | Senha do administrador. Tem prioridade sobre a variável de ambiente. |
| `SERVICEFLOW_ADMIN_PASSWORD` | Variável de ambiente | Sim (ou a propriedade acima) | Senha do administrador. |
| `SERVICEFLOW_ADMIN_USER` | Variável de ambiente | Não | Nome do usuário administrador. Padrão: `admin`. |

Regras:

- A senha deve ter **pelo menos 12 caracteres**.
- Se a senha não for informada ou tiver menos de 12 caracteres, a conta administrativa **não é criada**. A tela de login administrativo informa que o modo administrativo não está configurado e não permite a autenticação.
- O login de usuários comuns funciona normalmente, com ou sem o modo administrativo configurado.
- A senha é guardada apenas como hash PBKDF2 e nunca é exibida em logs ou mensagens.
- A configuração é lida ao iniciar a aplicação: após alterá-la, reinicie o ServiceFlow.

Nos exemplos abaixo, substitua `<senha-administrativa>` por uma senha forte escolhida por você (o texto entre `<` e `>` não é uma senha válida). Não salve a senha em arquivos do projeto nem a envie ao repositório.

### Variável de ambiente (recomendado com Maven)

Windows (PowerShell), válida apenas para o terminal atual:

```powershell
$env:SERVICEFLOW_ADMIN_PASSWORD = "<senha-administrativa>"
$env:SERVICEFLOW_ADMIN_USER = "<usuario-administrador>"   # opcional
mvn clean javafx:run
```

Linux/macOS:

```bash
export SERVICEFLOW_ADMIN_PASSWORD='<senha-administrativa>'
export SERVICEFLOW_ADMIN_USER='<usuario-administrador>'   # opcional
mvn clean javafx:run
```

Para remover a configuração do terminal: `Remove-Item Env:SERVICEFLOW_ADMIN_PASSWORD` (PowerShell) ou `unset SERVICEFLOW_ADMIN_PASSWORD` (Linux/macOS).

### Propriedade da JVM

A propriedade deve ser passada para a JVM que executa a aplicação, por exemplo nas *VM options* da configuração de execução da IDE:

```text
-Dserviceflow.admin.password=<senha-administrativa>
```

> Observação: o `mvn javafx:run` inicia a aplicação em uma JVM separada, por isso `mvn -Dserviceflow.admin.password=... javafx:run` **não** repassa a propriedade para a aplicação. Com Maven, use a variável de ambiente.
>
> Valores passados na linha de comando podem ficar no histórico do terminal e ser visíveis na lista de processos; prefira definir a senha apenas na sessão em que a aplicação será executada.

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
  <strong>ServiceFlow · Versão 3.6.1</strong>
</p>
