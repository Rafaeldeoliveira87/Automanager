# API da Concessionária

## Configuração

A API exige estas variáveis de ambiente antes de iniciar:

- `DB_URL`, `DB_USER` e `DB_PASSWORD`: credenciais do PostgreSQL. Para desenvolvimento local, copie `src/main/resources/config.properties.example` para `config.properties` e preencha os valores.
- `JWT_SECRET`: segredo estável com pelo menos 32 bytes. Configure-o no ambiente local ou no painel do serviço de hospedagem. A API não inicia sem ele.

A aplicação também encerra a inicialização se não conseguir conectar ao banco ou preparar o schema.
