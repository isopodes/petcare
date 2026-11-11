const API = "/api";
const animalId = new URLSearchParams(location.search).get("id");

const resumo = document.getElementById("resumo-animal");
const timeline = document.getElementById("timeline");
const filtro = document.getElementById("filtro");

// Mostra nome, espécie e raça no topo e monta a ficha usada no PDF
async function carregarAnimal() {
    try {
        const resp = await fetch(`${API}/animals/${animalId}`);
        if (!resp.ok) throw new Error();
        const a = await resp.json();

        resumo.textContent = [a.nome, a.especie, a.raca].filter(Boolean).join(" · ");
        document.title = `Historico - ${a.nome}`;
        montarFicha(a);
    } catch (erro) {
        resumo.textContent = "Animal não encontrado";
    }
}

// Bloco que só aparece na impressão: dados do animal + data de geração
function montarFicha(a) {
    const ficha = document.getElementById("ficha");
    ficha.replaceChildren();

    const linhas = [
        ["Nome", a.nome],
        ["Espécie", a.especie],
        ["Raça", a.raca],
        ["Sexo", a.sexo],
        ["Nascimento estimado",
            a.nascimentoEstimado
                ? new Date(a.nascimentoEstimado + "T00:00:00").toLocaleDateString("pt-BR")
                : null],
        ["Observações do tutor", a.observacoes]
    ];

    for (const [rotulo, valorCampo] of linhas) {
        if (!valorCampo) continue;
        const p = document.createElement("p");
        const forte = document.createElement("strong");
        forte.textContent = rotulo + ": ";
        p.append(forte, valorCampo);
        ficha.append(p);
    }

    const gerado = document.createElement("p");
    gerado.className = "nota-pdf";
    gerado.textContent =
        `Gerado em ${new Date().toLocaleString("pt-BR")}. ` +
        "Documento com informações registradas pelo tutor; não substitui avaliação veterinária.";
    ficha.append(gerado);
}

// Formata a data. Consulta mostra a hora; vacina só o dia.
function formatarData(texto, tipo) {
    const data = new Date(texto);
    return tipo === "CONSULTA"
        ? data.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" })
        : data.toLocaleDateString("pt-BR");
}

// Busca o histórico (com filtro, se escolhido) e desenha na tela
async function carregarHistorico() {
    const tipo = filtro.value;
    const url = `${API}/animals/${animalId}/history` + (tipo ? `?tipo=${tipo}` : "");

    try {
        const resp = await fetch(url);
        if (!resp.ok) throw new Error();
        mostrarHistorico(await resp.json());
    } catch (erro) {
        timeline.textContent = "Não foi possível carregar o histórico.";
    }
}

function mostrarHistorico(eventos) {
    timeline.replaceChildren();

    if (eventos.length === 0) {
        timeline.textContent = "Nenhum registro ainda.";
        return;
    }

    for (const e of eventos) {
        const item = document.createElement("li");
        item.className = "evento";

        const etiqueta = document.createElement("span");
        etiqueta.className = "etiqueta " + e.tipo.toLowerCase();
        etiqueta.textContent = e.tipo === "CONSULTA" ? "Consulta" : "Vacina";

        const data = document.createElement("time");
        data.textContent = formatarData(e.data, e.tipo);

        const titulo = document.createElement("h3");
        titulo.textContent = e.titulo;

        item.append(etiqueta, data, titulo);

        if (e.descricao) {
            const desc = document.createElement("p");
            desc.textContent = e.descricao;
            item.append(desc);
        }
        timeline.append(item);
    }
}

function avisar(elemento, texto, tipo) {
    elemento.textContent = texto;
    elemento.className = tipo;
}

// Envia um formulário para a API e atualiza a timeline
async function enviar(url, dados, form, elementoMsg) {
    try {
        const resp = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        });

        if (resp.status === 400) {
            avisar(elementoMsg, "Confira os campos e as datas.", "erro");
            return;
        }
        if (!resp.ok) throw new Error();

        form.reset();
        avisar(elementoMsg, "Registrado!", "ok");
        carregarHistorico();
    } catch (erro) {
        avisar(elementoMsg, "Erro ao registrar. Tente novamente.", "erro");
    }
}

function valor(id) {
    return document.getElementById(id).value.trim();
}

document.getElementById("form-consulta").addEventListener("submit", (ev) => {
    ev.preventDefault();
    let dataHora = valor("c-data");
    if (dataHora.length === 16) dataHora += ":00";

    enviar(`${API}/animals/${animalId}/appointments`, {
        profissional: valor("c-profissional") || null,
        clinica: valor("c-clinica") || null,
        dataHora: dataHora,
        motivo: valor("c-motivo") || null
    }, ev.target, document.getElementById("msg-consulta"));
});

document.getElementById("form-vacina").addEventListener("submit", (ev) => {
    ev.preventDefault();
    enviar(`${API}/animals/${animalId}/vaccines`, {
        nome: valor("v-nome"),
        dataAplicacao: valor("v-aplicacao"),
        proximaData: valor("v-proxima") || null,
        observacoes: valor("v-obs") || null
    }, ev.target, document.getElementById("msg-vacina"));
});

filtro.addEventListener("change", carregarHistorico);
document.getElementById("btn-pdf").addEventListener("click", () => window.print());

carregarAnimal();
carregarHistorico();