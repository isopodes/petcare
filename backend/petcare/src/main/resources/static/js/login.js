const msgLogin = document.getElementById("msg-login");
const msgCadastro = document.getElementById("msg-cadastro");

// Quem já está logado vai direto para a lista de animais
if (lerSessao()) location.href = "index.html";

function avisar(elemento, texto, tipo) {
    elemento.textContent = texto;
    elemento.className = tipo;
}

function postar(url, dados) {
    return fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dados)
    });
}

document.getElementById("form-login").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    try {
        const resp = await postar("/api/auth/login", {
            email: document.getElementById("l-email").value.trim(),
            senha: document.getElementById("l-senha").value
        });

        if (resp.status === 401) {
            avisar(msgLogin, "E-mail ou senha inválidos.", "erro");
            return;
        }
        if (!resp.ok) throw new Error();

        salvarSessao(await resp.json());
        location.href = "index.html";
    } catch (erro) {
        avisar(msgLogin, "Erro ao entrar. Tente novamente.", "erro");
    }
});

document.getElementById("form-cadastro").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const nome = document.getElementById("r-nome").value.trim();
    const email = document.getElementById("r-email").value.trim();
    const senha = document.getElementById("r-senha").value;

    try {
        const resp = await postar("/api/auth/register", { nome, email, senha });

        if (resp.status === 409) {
            avisar(msgCadastro, "Este e-mail já está cadastrado.", "erro");
            return;
        }
        if (resp.status === 400) {
            avisar(msgCadastro, "Confira os campos. A senha precisa ter no mínimo 8 caracteres.", "erro");
            return;
        }
        if (!resp.ok) throw new Error();

        // Conta criada: já entra automaticamente
        const login = await postar("/api/auth/login", { email, senha });
        if (!login.ok) throw new Error();
        salvarSessao(await login.json());
        location.href = "index.html";
    } catch (erro) {
        avisar(msgCadastro, "Erro ao criar a conta. Tente novamente.", "erro");
    }
});