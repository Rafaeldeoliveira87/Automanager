# AutoManager — Sistema de Gestão para Concessionária

Sistema acadêmico de gestão de concessionária com aplicação desktop para gerência, API REST e aplicativo móvel para vendedores. Os módulos usam PostgreSQL como banco compartilhado.

## Módulos

- [`concessionaria-desk/concessionaria-deskt`](concessionaria-desk/concessionaria-deskt): aplicação desktop Java Swing para gerenciar funcionários, usuários, veículos e vendas.
- [`concessionaria-api`](concessionaria-api): API Java Spring Boot que autentica usuários e fornece veículos disponíveis, vendas e histórico.
- [`Concessionaria Mobile`](Concessionaria%20Mobile): aplicativo React Native com Expo para vendedores consultarem veículos e registrarem vendas.

## Tecnologias

Java 17, Maven, Spring Boot, PostgreSQL, JWT, BCrypt, React Native e Expo.

## Requisitos

- Java 17 ou superior nos computadores que executam os módulos Java.
- Maven para compilar a API e o aplicativo desktop.
- Node.js e npm para executar o aplicativo móvel.
- Um banco PostgreSQL acessível pela API e pelo desktop.

## Executar o desktop

O desktop pode ser distribuído como um JAR executável com as dependências incluídas. Para gerar o arquivo, abra um terminal na pasta `concessionaria-desk/concessionaria-deskt` e execute:

```bash
mvn clean package
```

O arquivo será gerado em `target/concessionaria-desktop.jar`. Copie esse JAR e o arquivo `resources/config.properties.example` para uma pasta no computador de destino. Nessa pasta, crie `config.properties` a partir do exemplo e preencha os dados do PostgreSQL. **Não distribua nem publique o arquivo preenchido com credenciais.**

Execute na pasta que contém o JAR e `config.properties`:

```bash
java -jar concessionaria-desktop.jar
```

O computador precisa ter Java 17+ instalado e acesso de rede ao banco. O JAR inclui as bibliotecas do programa, mas não inclui Java nem o banco de dados. O arquivo local de configuração fica fora do JAR.

## Executar a API localmente

Defina `DB_URL`, `DB_USER` e `DB_PASSWORD` para conectar ao PostgreSQL. Para autenticação, defina também `JWT_SECRET` com pelo menos 32 bytes. A API encerra a inicialização se faltar configuração ou se não conseguir inicializar o schema.

Na pasta `concessionaria-api`:

```bash
mvn spring-boot:run
```

O endpoint de status fica em `http://localhost:8080/status`. Mais detalhes de configuração estão em [`concessionaria-api/README.md`](concessionaria-api/README.md).

## Executar o aplicativo móvel

Na pasta `Concessionaria Mobile`:

```bash
npm install
npx expo start
```

Configure `BASE_URL` em `src/api.js` para apontar para a API em execução. Para testar no celular, o aparelho e o computador precisam conseguir acessar o endereço da API. Consulte [`Concessionaria Mobile/README.md`](Concessionaria%20Mobile/README.md) para instruções adicionais.

## Fluxo de vendas

O vendedor entra com um usuário vinculado a um funcionário. O aplicativo envia o identificador do veículo; a API valida a sessão, calcula a comissão no servidor e grava a venda junto com a atualização do status do veículo em uma transação. O desktop compartilha o banco e também impede vender novamente um veículo já vendido.

## Segurança e configuração

- Não armazene credenciais reais no Git. Use os arquivos `.example` como modelos.
- Configure `JWT_SECRET` como segredo estável e privado no ambiente da API.
- Os usuários armazenam senhas com BCrypt.
- Usuários vendedores precisam ter `papel = VENDEDOR` e `funcionario_id` válido.
