# Sudoku v1.0.72 — stvarna R8 analiza i OSV provjera release ovisnosti

**Cilj:** Dovršiti prethodno otvorena pitanja iz v1.0.71 bez neutemeljene tvrdnje da nema mrtvog koda ili ranjivosti.

## R8: točno što je uklonjeno iz release varijante

Android release koristi minifikaciju i resource shrinking. Novi Python analizator scripts/analyze_r8_usage.py čita stvarne Gradle datoteke:
- app/build/outputs/mapping/release/usage.txt — R8 popis uklonjenih klasa i članova.
- app/build/outputs/mapping/release/mapping.txt — zadržane/preimenovane klase.

Novi izvještaj r8-removed-symbols.json broji odvojeno uklonjene klase i članove, klasificirane prema paketu: com.brendigo.sudoku, androidx.compose, ostali AndroidX, Kotlin i druge biblioteke. Izlaz r8-unused-app-symbols.txt sadrži konkretne aplikacijske simbole koji su uklonjeni iz release APK-a. Sirovi R8 mapping se ne objavljuje.

**Ograničenje:** simbol uklonjen iz optimizirane release varijante nije automatski nepotreban u debug varijanti, testovima ili budućim nadogradnjama. Brisanje izvornog koda zahtijeva poseban test.

Prethodni audit v1.0.71 imao je 2.794 zapisa mapiranih klasa i 39.112 redaka usage izvještaja; same te brojke **nisu** broj mrtvih funkcija. Novi artefakt daje stvaran broj klasa/članova i konkretne primjere.

## OSV / CVE: sva razriješena izdanja ovisnosti

scripts/osv_release_scan.py čita svaku Maven komponentu aktualnog CycloneDX SBOM-a generiranog iz releaseRuntimeClasspath. Svaka se ispituje putem službenog OSV API-ja POST /v1/querybatch s točnim groupId:artifactId i verzijom.

Sigurnosne kontrole:

1. Pozitivna kontrola provjerava da OSV stvarno prepoznaje poznatu ranjivu verziju org.apache.logging.log4j:log4j-core:2.14.1; ta biblioteka nije Sudoku dependency i nije uključena u skenirani broj.
2. Sve komponente iz aktualnog SBOM-a moraju biti upitane i za svaku se mora dobiti odgovor, bez parcijalnog izvještaja.
3. OSV paginacija se prati do kraja. Pogreška mreže, API-ja, JSON-a ili nepotpun odgovor uzrokuje neuspjeh.
4. Za svaku pronađenu stavku dohvate se detalji, GHSA/OSV identifikatori i dostupni CVE aliasi, a rezultati se čuvaju u JSON i CSV izvještajima.
5. GitHub Android CI i objavljivanje APK/AAB blokiraju izdanje ako neka proizvodna Maven komponenta ima poznati OSV nalaz.

**Važno:** prethodni SBOM sadržavao je 90 razriješenih proizvodnih Maven komponenti. Broj se ne upisuje ručno u audit: ponovno se računa iz novog release builda. OSV pokrivenost nije potpuna sigurnosna certifikacija: izvan baze mogu postojati nove, neobjavljene ili lokalne ranjivosti.

Službena OSV dokumentacija: https://google.github.io/osv.dev/post-v1-querybatch/

## Pixel QA

Pronađeno je svih deset originalnih privatnih PNG referenci. Svih deset SHA-256 otisaka sada su u scripts/final_ten_reference_qa.py. Potpuni numerički izvještaj v1.0.71 nalazi se u docs/pixel-qa-owner-v1.0.71-measured.md. Sva su odstupanja izmjerena, ali nijedna referenca nije identična u svim pikselima.

## Isporuka

versionName=1.0.72, versionCode=73. Objaviti novo izdanje tek nakon zelenih CI, 11/11 emulatorskih poslova, R8 i online OSV audita. Bez odgovarajućeg Google Play ključa APK ostaje testni, AAB nepotpisan i GitHub Release označen prerelease.
