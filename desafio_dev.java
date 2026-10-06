import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class desafio_dev {

    private static final BigDecimal UM_PORCENTO = new BigDecimal("0.01");
    private static final BigDecimal CINCO_PORCENTO = new BigDecimal("0.05");
    private static final BigDecimal JUROS_DIARIOS = new BigDecimal("0.025");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Dados transcritos dos JSONs fornecidos no enunciado.
    private static final List<Venda> VENDAS = criarVendas();
    private static final Map<Integer, String> PRODUTOS = criarProdutos();
    private static final Map<Integer, Integer> ESTOQUE = criarEstoque();
    private static int proximoIdMovimentacao = 1;

    private record Venda(String vendedor, BigDecimal valor) {}

    private record Movimento(
            int id,
            int codigoProduto,
            String descricao,
            String tipo,
            int quantidade,
            int estoqueFinal
    ) {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== DESAFIO DEV ===");
                System.out.println("1 - Calcular comissões");
                System.out.println("2 - Movimentar estoque");
                System.out.println("3 - Calcular juros por atraso");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opção: ");

                String opcao = scanner.nextLine().trim();
                try {
                    switch (opcao) {
                        case "1" -> mostrarComissoes();
                        case "2" -> registrarMovimentacao(scanner);
                        case "3" -> mostrarJuros(scanner);
                        case "0" -> {
                            System.out.println("Programa encerrado.");
                            return;
                        }
                        default -> System.out.println("Opção inválida.");
                    }
                } catch (IllegalArgumentException | IllegalStateException erro) {
                    System.out.println("Erro: " + erro.getMessage());
                }
            }
        }
    }

    // DESAFIO 1 - Cálculo de comissões
    private static void mostrarComissoes() {
        Map<String, BigDecimal> comissoes = calcularComissoes(VENDAS);

        System.out.println("\n--- COMISSÕES ---");
        comissoes.forEach((vendedor, comissao) ->
                System.out.printf(Locale.forLanguageTag("pt-BR"),
                        "%s: R$ %.2f%n", vendedor, comissao));
    }

    private static Map<String, BigDecimal> calcularComissoes(List<Venda> vendas) {
        Map<String, BigDecimal> comissoes = new LinkedHashMap<>();

        for (Venda venda : vendas) {
            BigDecimal comissao = calcularComissaoVenda(venda.valor());
            comissoes.merge(venda.vendedor(), comissao, BigDecimal::add);
        }

        return comissoes;
    }

    private static BigDecimal calcularComissaoVenda(BigDecimal valor) {
        if (valor.compareTo(new BigDecimal("100.00")) < 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        BigDecimal percentual = valor.compareTo(new BigDecimal("500.00")) < 0
                ? UM_PORCENTO
                : CINCO_PORCENTO;

        return valor.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
    }

    // DESAFIO 2 - Movimentação de estoque
    private static void registrarMovimentacao(Scanner scanner) {
        int codigo = lerInteiro(scanner, "Código do produto: ");
        String tipo = lerTipo(scanner);
        int quantidade = lerInteiro(scanner, "Quantidade: ");
        String descricao = lerTexto(scanner, "Descrição da movimentação: ");

        Movimento movimento = movimentarEstoque(codigo, tipo, quantidade, descricao);

        System.out.println("\nMovimentação registrada com sucesso.");
        System.out.println("ID: " + movimento.id());
        System.out.println("Produto: " + movimento.codigoProduto() + " - " + movimento.descricao());
        System.out.println("Tipo: " + movimento.tipo());
        System.out.println("Quantidade: " + movimento.quantidade());
        System.out.println("Estoque final: " + movimento.estoqueFinal());
    }

    private static Movimento movimentarEstoque(
            int codigo,
            String tipo,
            int quantidade,
            String descricao
    ) {
        if (!PRODUTOS.containsKey(codigo)) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }

        int estoqueAtual = ESTOQUE.get(codigo);
        int estoqueFinal = tipo.equals("entrada")
                ? estoqueAtual + quantidade
                : estoqueAtual - quantidade;

        if (estoqueFinal < 0) {
            throw new IllegalStateException("Estoque insuficiente. Saldo atual: " + estoqueAtual);
        }

        ESTOQUE.put(codigo, estoqueFinal);
        return new Movimento(
                proximoIdMovimentacao++,
                codigo,
                PRODUTOS.get(codigo),
                tipo,
                quantidade,
                estoqueFinal
        );
    }

    // DESAFIO 3 - Cálculo de juros por atraso
    private static void mostrarJuros(Scanner scanner) {
        BigDecimal valor = lerValor(scanner, "Valor da cobrança: ");
        LocalDate vencimento = lerData(scanner, "Data de vencimento (dd/MM/yyyy): ");
        LocalDate hoje = LocalDate.now();
        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(vencimento, hoje));

        BigDecimal juros = valor
                .multiply(JUROS_DIARIOS)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = valor.add(juros).setScale(2, RoundingMode.HALF_UP);

        System.out.println("\n--- JUROS POR ATRASO ---");
        System.out.println("Data de hoje: " + hoje.format(FORMATO_DATA));
        System.out.println("Dias de atraso: " + diasAtraso);
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Juros: R$ %.2f%n", juros);
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Valor total: R$ %.2f%n", total);
    }

    private static String lerTipo(Scanner scanner) {
        String tipo = lerTexto(scanner, "Tipo (entrada/saida): ").toLowerCase(Locale.ROOT);
        if (tipo.equals("e") || tipo.equals("entrada")) {
            return "entrada";
        }
        if (tipo.equals("s") || tipo.equals("saida") || tipo.equals("saída")) {
            return "saida";
        }
        throw new IllegalArgumentException("O tipo deve ser entrada ou saida.");
    }

    private static int lerInteiro(Scanner scanner, String mensagem) {
        String valor = lerTexto(scanner, mensagem);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe um número inteiro válido.");
        }
    }

    private static BigDecimal lerValor(Scanner scanner, String mensagem) {
        String valor = lerTexto(scanner, mensagem).replace(',', '.');
        try {
            BigDecimal resultado = new BigDecimal(valor);
            if (resultado.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("O valor não pode ser negativo.");
            }
            return resultado;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe um valor válido.");
        }
    }

    private static LocalDate lerData(Scanner scanner, String mensagem) {
        String valor = lerTexto(scanner, mensagem);
        try {
            return LocalDate.parse(valor, FORMATO_DATA);
        } catch (DateTimeParseException erro) {
            throw new IllegalArgumentException("Informe a data no formato dd/MM/yyyy.");
        }
    }

    private static String lerTexto(Scanner scanner, String mensagem) {
        System.out.print(mensagem);
        String valor = scanner.nextLine().trim();
        if (valor.isBlank()) {
            throw new IllegalArgumentException("O campo não pode ficar vazio.");
        }
        return valor;
    }

    private static List<Venda> criarVendas() {
        List<Venda> vendas = new ArrayList<>();
        adicionarVendas(vendas, "João Silva", "1200.50", "950.75", "1800.00", "1400.30", "1100.90", "1550.00", "1700.80", "250.30", "480.75", "320.40");
        adicionarVendas(vendas, "Maria Souza", "2100.40", "1350.60", "950.20", "1600.75", "1750.00", "1450.90", "400.50", "180.20", "90.75");
        adicionarVendas(vendas, "Carlos Oliveira", "800.50", "1200.00", "1950.30", "1750.80", "1300.60", "300.40", "500.00", "125.75");
        adicionarVendas(vendas, "Ana Lima", "1000.00", "1100.50", "1250.75", "1400.20", "1550.90", "1650.00", "75.30", "420.90", "315.40");
        return List.copyOf(vendas);
    }

    private static void adicionarVendas(List<Venda> vendas, String vendedor, String... valores) {
        for (String valor : valores) {
            vendas.add(new Venda(vendedor, new BigDecimal(valor)));
        }
    }

    private static Map<Integer, String> criarProdutos() {
        Map<Integer, String> produtos = new LinkedHashMap<>();
        produtos.put(101, "Caneta Azul");
        produtos.put(102, "Caderno Universitário");
        produtos.put(103, "Borracha Branca");
        produtos.put(104, "Lápis Preto HB");
        produtos.put(105, "Marcador de Texto Amarelo");
        return produtos;
    }

    private static Map<Integer, Integer> criarEstoque() {
        Map<Integer, Integer> estoque = new LinkedHashMap<>();
        estoque.put(101, 150);
        estoque.put(102, 75);
        estoque.put(103, 200);
        estoque.put(104, 320);
        estoque.put(105, 90);
        return estoque;
    }
}
