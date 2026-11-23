# Klavírní samouk

Android aplikace (tablet, na šířku) pro děti, které se učí hrát na klavír:

- **správný prstoklad** – čísla prstů 1–5, každý prst má svou barvu na ruce, klávese i nad notou,
- **noty a klávesy** – nota v osnově s houslovým klíčem a k ní patřící klávesa,
- **lidové písničky** – Skákal pes, Prší prší, Kočka leze dírou, Pec nám spadla (s prstokladem a textem),
- **body, hvězdičky, série, odznaky a úrovně** pro motivaci,
- **poslech skutečného piana** mikrofonem (algoritmus YIN) – aplikace pozná, jestli žák zahrál správnou klávesu,
- **ťukání na klávesy na displeji**, když piano není po ruce,
- **profily žáků** a **předčítání textů** (🔊, volitelně automaticky) pro děti, které ještě neumí číst,
- **aktualizace z aplikace** (Pro rodiče → Zkontrolovat aktualizace).

## Instalace do tabletu

Nejnovější APK je v sekci [Releases](../../releases/latest). Stáhněte ho v tabletu a nainstalujte
(Android se jednou zeptá na povolení instalace z prohlížeče).

## Struktura

| Modul | Obsah |
|---|---|
| `jadro/` | čistý Kotlin: noty, písničky, lekce, body, profily, rozpoznání tónu (YIN) a detektor úderů – včetně testů |
| `app/` | Android aplikace v Jetpack Compose: obrazovky, mikrofon, zvuk kláves, předčítání, aktualizace |

Testy jádra jdou spustit i bez Android SDK: `./gradlew -PjenJadro :jadro:test`

## Sestavení

GitHub Actions při každém pushi do `main` spustí testy, sestaví APK a vydá ho jako Release
`build-<číslo>`. Podle tohoto čísla aplikace pozná, že je k dispozici novější verze.

APK je podepsané klíčem prototypu `podpis/prototyp.jks` (stejný klíč je nutný, aby šly instalovat
aktualizace). Pro ostrý provoz klíč přesuňte do GitHub Secrets.

## Zdroje a licence

- Melodie písniček jsou lidové (volné dílo), přepsané z notových zápisů pro začátečníky
  (pavelhokr.cz, prodeticokoliv.cz). Prstoklad je navržený pro tuto aplikaci.
- Písma Baloo 2 a Noto Music jsou pod licencí SIL Open Font License 1.1.
