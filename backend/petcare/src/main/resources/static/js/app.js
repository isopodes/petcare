const API = "/api";
const USUARIO_ID = 1; // por enquanto, sem login: usamos o usuário Maria

const lista = document.getElementById("lista");
const form = document.getElementById("form-animal");
const mensagem = document.getElementById("mensagem");

// Busca os animais na API e manda desenhar na tela
async function carregarAnimais() {
    try {
        const resp = await fetch(`${API}/animals?usuarioId=${USUARIO_ID}`);
        if (!resp.ok) throw new Error("Falha ao carregar");
        mostrarAnimais(await resp.json());
    } catch (erro) {
        lista.textContent = "Não foi possível carregar os animais.";
    }
}

// Cria um card para cada animal
function mostrarAnimais(animais) {
    lista.replaceChildren();

    if (animais.length === 0) {
        lista.textContent = "Nenhum animal cadastrado ainda.";
        return;
    }

    for (const a of animais) {
        const card = document.createElement("a");
        card.className = "card";
        card.href = `animal.html?id=${a.id}`;

        const titulo = document.createElement("h3");
        titulo.textContent = a.nome;

        const info = document.createElement("p");
        info.textContent = [a.especie, a.raca].filter(Boolean).join(" · ");

        card.append(titulo, info);
        lista.append(card);
    }
}

function valor(id) {
    return document.getElementById(id).value.trim();
}

function avisar(texto, tipo) {
    mensagem.textContent = texto;
    mensagem.className = tipo;
}

// Quando o formulário é enviado, manda os dados para a API
form.addEventListener("submit", async (evento) => {
    evento.preventDefault(); // impede a página de recarregar

    const dados = {
        usuarioId: USUARIO_ID,
        nome: valor("nome"),
        especie: valor("especie"),
        raca: valor("raca") || null,
        nascimentoEstimado: valor("nascimento") || null,
        sexo: valor("sexo") || null,
        observacoes: valor("observacoes") || null
    };

    try {
        const resp = await fetch(`${API}/animals`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (resp.status === 400) {
            avisar("Confira os campos (a data de nascimento não pode ser futura).", "erro");
            return;
        }
        if (resp.status === 404) {
            avisar("Usuário não encontrado. Confira o USUARIO_ID no app.js.", "erro");
            return;
        }
        if (!resp.ok) throw new Error("Erro no servidor");

        form.reset();
        avisar("Animal cadastrado!", "ok");
        carregarAnimais();
    } catch (erro) {
        avisar("Erro ao cadastrar. Tente novamente.", "erro");
    }
});

carregarAnimais();