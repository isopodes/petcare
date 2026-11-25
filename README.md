# WayHub PetCare+

Plataforma web para centralização e gestão do histórico de saúde de animais de estimação, reunindo registo de vacinas, consultas, exames, acompanhamento clínico e exportação de informações.

🔗 **Aplicação online (MVP):** [petcare-plus-nlse.onrender.com](https://petcare-plus-nlse.onrender.com)

---

## Tecnologias e Estrutura

- **Back-end:** Java (JDK 21) com Spring Boot e PostgreSQL
- **Front-end:** HTML, CSS e JavaScript
- **Containerização e Deploy:** Docker (`Dockerfile` com `eclipse-temurin:21`) e hospedagem no Render
- **Estrutura principal do repositório:** `backend/petcare`

---

## Principais Funcionalidades

- Autenticação e gestão de acesso de utilizadores
- Cadastro e manutenção do perfil dos animais (ex.: espécie, raça, dados clínicos)
- Registo de vacinas e reforços, consultas veterinárias, exames e observações
- Visualização cronológica do histórico de saúde e exportação em PDF

---

## Variáveis de Ambiente (Back-end / Deploy)

Para executar o back-end conectado ao banco PostgreSQL, configure:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

---

## Projeto Académico

Desenvolvido no âmbito da graduação em Análise e Desenvolvimento de Sistemas (ADS) — **WayHub PetCare+**.
