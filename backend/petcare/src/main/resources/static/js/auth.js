// Guarda o login do usuário e envia o token em todas as chamadas à API
const SESSAO = "petcare.sessao";

function lerSessao() {
    try {
        return JSON.parse(sessionStorage.getItem(SESSAO));
    } catch (erro) {
        return null;
    }
}

function salvarSessao(dados) {
    sessionStorage.setItem(SESSAO, JSON.stringify(dados));
}

function sair() {
    sessionStorage.removeItem(SESSAO);
    location.href = "login.html";
}

// Troca o fetch por uma versão que acrescenta o token nas rotas /api/
// (menos /api/auth/, que é o próprio login e cadastro)
const fetchOriginal = window.fetch.bind(window);

window.fetch = async (url, opcoes = {}) => {
    const protegida = typeof url === "string"
        && url.startsWith("/api/")
        && !url.startsWith("/api/auth/");

    if (protegida) {
        const sessao = lerSessao();
        if (!sessao) {
            location.href = "login.html";
            return new Promise(() => {}); // a página está saindo, não responde
        }
        opcoes = {
            ...opcoes,
            headers: { ...(opcoes.headers || {}), Authorization: "Bearer " + sessao.token }
        };
    }

    const resposta = await fetchOriginal(url, opcoes);
    if (protegida && resposta.status === 401) sair(); // token vencido ou inválido
    return resposta;
};

// Mostra o nome do usuário e liga o botão Sair, se a página tiver
document.addEventListener("DOMContentLoaded", () => {
    const sessao = lerSessao();
    const nome = document.getElementById("nome-usuario");
    if (sessao && nome) nome.textContent = "Olá, " + sessao.usuario.nome;

    const botao = document.getElementById("btn-sair");
    if (botao) botao.addEventListener("click", sair);
});