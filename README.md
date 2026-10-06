# Desafio Técnico Java

Solução simples para os três exercícios do desafio técnico:

1. cálculo de comissão por venda;
2. movimentação de estoque;
3. cálculo de juros de 2,5% ao dia por atraso.

## Tecnologia

- Java 17 ou superior
- Aplicação de console
- Sem frameworks ou dependências externas

## Como executar

No PowerShell, dentro desta pasta:

```powershell
javac -encoding UTF-8 desafio_dev.java
java desafio_dev
```

O programa apresenta um menu para escolher cada desafio. Os dados de vendas e produtos são os dados fornecidos no enunciado.

## Decisões principais

- Valores financeiros usam `BigDecimal`.
- O arredondamento financeiro usa `RoundingMode.HALF_UP`.
- Cada movimentação de estoque recebe um identificador único.
- Saídas que deixariam o estoque negativo são rejeitadas.
- O cálculo de juros considera dias completos de atraso.
