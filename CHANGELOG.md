# Changelog

Todas as mudanças relevantes do app ficam registradas aqui. O formato segue o
[Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/), e as versões seguem o
`versionName` do app.

As regras de alimentação vêm do site Cachorro Verde (<https://cachorroverde.com.br>).

## [Não lançado]

### Alterado

- A citação da fonte (Cachorro Verde, com link) passou a ser o cabeçalho da aba
  Dicas e do README, em vez do rodapé. A licença continua no fim da aba Dicas.

### Adicionado

- Este changelog.

## [1.1] - 2026-09-25

### Adicionado

- Peso ideal de cada cadela, com a faixa de porção diária sugerida pelo porte
  (% do peso) e se a porção atual está dentro dela.
- Data de nascimento de cada cadela. A porção sugerida passa a considerar a
  idade:
  - Filhotes: tabela por idade e porte adulto, calculada sobre o peso atual.
    A fase de filhote vai até 12, 18 ou 24 meses, conforme o porte.
  - Adultas: ponto de partida dentro da faixa conforme a fase da vida (jovem
    adulta, adulta, meia-idade, idosa).
  - Refeições por dia conforme a idade.
- Opção "castrada", que desce a faixa em 0,5%.
- Doses de óleo de peixe e de óleo vegetal pelo peso, no cartão de cada cadela.
- Cartão de conservação no Preparo: geladeira, congelador e descongelamento.
- Aba Dicas: conservação, preparo, alimentos tóxicos e a evitar, tabelas de
  porção por peso e por idade, fase sênior, ajuste fino e óleos.
- Seção de fonte e licença no app, com links para o Cachorro Verde, a GPLv3 e o
  código-fonte.
- README com a fonte das informações e as páginas consultadas.
- Cabeçalho da GPLv3 nos arquivos de código.

### Alterado

- Carboidrato padrão de 30% para 35%, como no Cachorro Verde (30% carne,
  5% vísceras, 30% vegetais, 35% carboidrato).
- Valores padrão da Mari e da Poranga: 22 kg de peso ideal e nascimento em
  04/2015 e 03/2021.

## [1.0] - 2026-09-25

### Adicionado

- Preparo por dias (quanto preparar) e por kg (até quando dura), sem contar os
  dias de ração e de jejum.
- Ingredientes do lote com peso e custo; preço da carne pela média dos tipos.
- Porção diária e total no lote de cada cadela.
- Arroz: arroz cru e água a partir do peso cozido.
- Calendário com dias de comida natural, ração (a cada 5 dias) e jejum (a cada
  15 dias).
- Ajustes: cadelas, proporções, preços, fator do arroz e rotina, salvos no
  aparelho.
- Licença GPLv3.

[Não lançado]: https://github.com/giovanildo/comida-natural-cadelas/compare/972cdc8...HEAD
[1.1]: https://github.com/giovanildo/comida-natural-cadelas/compare/8ac452a...972cdc8
[1.0]: https://github.com/giovanildo/comida-natural-cadelas/commit/8ac452a
