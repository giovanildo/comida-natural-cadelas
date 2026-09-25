# Changelog

Todas as mudanças relevantes do app ficam registradas aqui. O formato segue o
[Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/), e as versões seguem o
`versionName` do app.

As regras de alimentação vêm do site Cachorro Verde (<https://cachorroverde.com.br>).

## [Não lançado]

## [1.2] - 2026-09-25

### Corrigido

- Dose de óleo vegetal pelo peso, conforme o texto do Cachorro Verde: para
  15 a 25 kg é 1 colher de sobremesa (antes: 1 colher de sopa). Faixas:
  até 5 kg, 5 a 15, 15 a 25, 25 a 35 e acima de 35 kg.
- Dose do suplemento: Food Dog 1 g a cada 100 g de comida para adultas e
  idosas e 3 g para filhotes; Nutroplus 0,6 g e 1,5 g. Mostra a versão da
  fase (Manutenção, Sênior ou Crescimento).
- Conservação: congelador até 45 dias (com legumes, até 30), descongelar em
  12 a 18 horas e nunca congelar ovos.

### Alterado

- A citação da fonte (Cachorro Verde, com link) passou a ser o cabeçalho da aba
  Dicas e do README, em vez do rodapé. A licença continua no fim da aba Dicas.

### Adicionado

- Complementos do dia no cartão de cada cadela: suplemento, óleo vegetal,
  óleo de peixe, iogurte ou kefir, alho e sal, com as doses pelo peso.
- Rodízio de receitas: troca de receita a cada 3 dias de comida natural
  (ajustável). A receita do dia aparece no topo e no calendário, a aba Receitas
  abre nela e, no modo lote, cada receita cobre os seus dias do lote.
- Lembrete diário opcional para tirar do congelador a porção do dia seguinte,
  que também avisa quando amanhã é dia de ração ou de jejum.
- Exames de rotina de cada cadela, conforme a fase, com a data do último
  check-up e quando vence o próximo (idosas a cada 6 meses).
- Aba Dicas: tabelas de todos os complementos e regras do rodízio.
- Aba Receitas com as 4 sugestões de combinação da dieta cozida do Cachorro
  Verde, multiplicadas para 1 dia das duas cadelas ou para o lote do Preparo:
  - Ilustração de cada receita: prato visto de cima, com as fatias na
    proporção de cada grupo e os ingredientes em emoji.
  - Peso cozido de cada ingrediente e estimativa de quanto comprar cru.
  - Ovos em quantidade fixa (1 por cadela por dia, no lugar de 50 g de
    carne), com o aviso de usar 1 a 2 vezes por semana.
  - Complementos: suplemento vitamínico-mineral pelo total de comida e óleo
    pela dose do peso de cada cadela.
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

[Não lançado]: https://github.com/giovanildo/comida-natural-cadelas/compare/v1.2...HEAD
[1.2]: https://github.com/giovanildo/comida-natural-cadelas/compare/972cdc8...v1.2
[1.1]: https://github.com/giovanildo/comida-natural-cadelas/compare/8ac452a...972cdc8
[1.0]: https://github.com/giovanildo/comida-natural-cadelas/commit/8ac452a
