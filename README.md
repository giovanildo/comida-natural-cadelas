# Comida das Cadelas

## Fonte das informações

As proporções da dieta, a quantidade diária por peso, idade e castração, as doses
de óleo, a conservação, as dicas de preparo e a lista de alimentos tóxicos vêm do
site **Cachorro Verde** — Alimentação Natural pra Cães e Gatos:

**<https://cachorroverde.com.br>**

Páginas consultadas:

- [Dieta Cozida para cães](https://cachorroverde.com.br/caes/dieta-cozida-para-caes/)
- [Dieta Crua sem Ossos para Cães](https://cachorroverde.com.br/caes/dieta-crua-sem-ossos-para-caes/)
- [Alimentação Natural do pet idoso](https://cachorroverde.com.br/idoso/)
- [Alimentação Natural para Cães Idosos](https://www.cachorroverde.com.br/alimentacao-natural-para-caes-idosos/)
- [Congelamento e descongelamento da Alimentação Natural](https://cachorroverde.com.br/congelamento-descongelamento/)
- [Cuidados no Descongelamento](https://cachorroverde.com.br/cuidados-no-descongelamento/)
- [Alimentos, medicamentos e plantas tóxicas para os pets](https://cachorroverde.com.br/toxicos/)
- [Carboidratos na Alimentação Natural](https://cachorroverde.com.br/carboidratos/)

O conteúdo do Cachorro Verde pertence aos seus autores e não é coberto pela
licença deste repositório. O app apenas resume e aplica essas orientações; para
o texto completo, consulte o site.

As informações do app não substituem a orientação de um médico-veterinário.

## O app

App Android para planejar a alimentação natural (AN) da Mari e da Poranga.

- **Preparo:** calcula quanto preparar para um período (por dias) ou quanto tempo
  dura uma quantidade (por kg). Mostra o peso e o custo de cada ingrediente, a
  porção de cada cadela, o arroz cru e a água, e como conservar o lote.
- **Calendário:** marca os dias de comida natural, de ração (a cada 5 dias) e de
  jejum (a cada 15 dias).
- **Receitas:** as 4 sugestões de combinação da dieta cozida do Cachorro Verde,
  ilustradas e multiplicadas para 1 dia ou para o lote, com o peso cozido e a
  estimativa de compra de cada ingrediente.
- **Dicas:** conservação, preparo, alimentos tóxicos e a evitar, porção por peso
  e por idade, e doses de óleo.
- **Ajustes:** peso, idade e castração de cada cadela, proporções, preços e rotina.
  Tudo fica salvo no aparelho; o app não acessa a rede.

## Como compilar

```sh
./gradlew assembleRelease
```

O APK sai em `app/build/outputs/apk/release/app-release.apk`.

## Mudanças

O histórico de versões está no [CHANGELOG](CHANGELOG.md).

## Licença

Copyright (C) 2026 Giovanildo

Este programa é software livre: você pode redistribuí-lo e/ou modificá-lo sob os
termos da GNU General Public License, versão 3, publicada pela Free Software
Foundation. Veja o arquivo [LICENSE](LICENSE) ou
<https://www.gnu.org/licenses/gpl-3.0.html>.
