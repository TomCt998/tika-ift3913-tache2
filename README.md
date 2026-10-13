# IFT3913 — Tâche 2 : Qualité logicielle

Ce dépôt est utilisé dans le cadre de la tâche 2 du cours IFT3913 de l'Université de Montréal.

Équipe : Thomas Côté et Mahdi Slimani

Section Thomas Côté 


Pour cette tâche, j'ai choisi d'étudier la classe org.apache.tika.utils.CharsetUtils, appartenant au module tika-core du projet Apache Tika.
Cette classe permet de vérifier la prise en charge d'encodages de caractères, de normaliser leurs noms et de résoudre certains alias. Elle possède déjà une suite de 4 tests JUnit dans CharsetUtilsTest.java.
J'ai pris cette classe parce que sa couverture initiale était incomplète :

Mesure JaCoCo (Couverture initiale en %)

Instructions (83 %)

Branches (78 %)

Instructions de isSupported(String) (39 %)

Branches de isSupported(String) (25 %)

Les méthodes isSupported(String) et forName(String) présentaient des chemins d'exécution non couverts, dont certains associés à l'utilisation facultative de la bibliothèque ICU4J.

Je considérais donc CharsetUtils comme une candidate pertinente pour évaluer si la génération automatique de tests permettait d'améliorer une suite existante.

Une limite importante est toutefois apparue pendant l'expérimentation : bien que la classe entière soit insuffisamment couverte, la méthode clean(String) était déjà entièrment couverte par les tests originaux. Cette distinction entre la couverture globale d'une classe et celle des méthodes individuelles s'est révélée essentielle pour interpréter les résultats.


J'ai installé ChatUniTest 2.1.1 dans le pipeline Maven du module tika-core avec l'aide de ChatGPT, puis configuré son utilisation avec un modèle de langage ouvert exécuté localement.

J'ai utilisé Qwen2.5-Coder 7B avec Ollama. Comme ChatUniTest ne reconnaissait pas directement le nom qwen2.5-coder:7b, j'ai créé un alias Ollama nommé codeqwen:v1.5-chat pointant vers le même modèle, une idée de ChatGPT qui m'a assisté pour le débogage.

J'ai ensuite lancé la génération avec la commande suivante : .\mvnw.cmd -f .\tika-core\pom.xml chatunitest:class "-DselectClass=org.apache.tika.utils.CharsetUtils" "-Dthread=false" 

Il est important de préciser que j'ai demandé à ChatUniTest de traiter la classe entière, et non uniquement la méthode clean(). ChatUniTest a tenteé de générer des tests pour isSupported(), clean() et forName(). Cependant, les résultats de la génération n'ont pas été uniformes entre ces méthodes.

Lors de la première exécution, ChatUniTest n'a conservé aucun fichier Java exploitable. Plusieurs tentatives échouaient à la compilation, notamment en raison d'importations Mockito absentes du module tika-core.

J'ai ajouté les dépendances Maven mockito-core et mockito-junit-jupiter avec la portée test, puis relancé ChatUniTest.

Cette deuxième génération a produit les fichiers :

tika-core/chatunitest-tests/org/apache/tika/utils/CharsetUtils_clean_2_0_Test.java

tika-core/chatunitest-tests/org/apache/tika/utils/CharsetUtils_Suite.java

La classe CharsetUtils_clean_2_0_Test contenait 17 méthodes de test JUnit 5.

Le résultat le plus significatif est que seule la méthode clean() a donné lieu à des tests conservés et exploitables. Les tentatives portant sur isSupported() et forName() n'ont pas abouti à des tests utilisables dans cette exécution.

Or, clean() était déjà couverte à 100 % avant la génération.

Je n'avais pas demandé à l'IA de privilégier une méthode déjà couverte. J'avais sélectionné une classe présentant des lacunes de couverture, mais la génération automatique n'a réussi que pour la méthode dont la couverture était déjà complète.

Cette observation constitue une limite importante de l'outil : générer des tests supplémentaires ne garantit pas qu'ils ciblent les parties du programme où les nouveaux tests seraient les plus utiles.

Cela ne démontre pas que ChatUniTest privilégie systématiquement les méthodes déjà couvertes. Mon expérience montre seulement qu'avec cette configuration, ce modèle et cette classe, les tests exploitables provenaient d'une méthode déjà entièrement couverte.

Après avoir copié la classe générée dans src/test/java, j'ai exécuté les 17 tests sans modifier initialement leurs assertions.

J'ai obtenu les résultats suivants : 17 tests générés, 11 réussis et 6 échoués.

Les six échecs provenaient de valeurs attendues incorrectes : 
Test (Oracle initial de ChatUniTest) (Oracle corrigé)
testCleanWithISOName ("ISO-8859-1") (null)
testCleanWithWhitespaceAndISOName ("ISO-8859-1") (null)
testCleanWithCPName ("CP1252") ("windows-1252")
testCleanWithWinName ("Windows-1252") ("windows-1252")
testCleanWithLeadingComma ("UTF-8") (null)
testCleanWithWhitespaceAndLeadingComma ("UTF-8") (null)

J'ai corrigé les six valeurs attendus. J'ai également dû corriger une erreur introduite pendant mes modifications : l'utilisation de "null" au lieu de la valeur null.

Finalement, j'ai corrigé les problèmes de style relevés par Checkstyle ainsi que le formatage avec SpotLess (idée par ChatGPT).

Les 17 tests générés et corrigés se sibt exécutés avec succès. 


Les tests générés utilisaient généralement des assertions précises de la forme :

String result = CharsetUtils.clean(charsetName);
assertEquals(expected, result);

L'oracle vérifie une valeur exacte plutôt que de simplement s'assurer qu'aucune exception n'est levée.

ChatUniTest a proposé plusieurs catégories d'entrées, notamment des noms d'encodages valides et invalides, des chaînes vides et la valeur null et des noms contenant des espaces, des guillemets ou des virgules.

Les tests étaient cependant moyennement pertinents, avec six des des dix-sept tests avec une valeur attendue incorrecte, malgré une structure JUnit valide. En comparaison, les tests originaux de CharsetUtilsTest vérifiaient déjà plusieurs comportements de normalisation et de validation à l'aide d'assertions précises.

J'ai constaté que les tests ChatUniTest étaient généralement faciles à comprendre, mais souvent redondants avec ces tests existants.

Cette expérience m'a permis de distinguer deux propriétés importantes : un test peut être correctement écrit sur le plan syntaxique tout en vérifiant un comportement attendu incorrect, et un test peut réussir sans améliorer sensiblement la capacité globale de la suite à détecter des défauts.


J'ai mesuré la couverture de CharsetUtils avec les quatre tests originaux, puis avec les 17 tests ChatUniTest supplémentaires. C'est à ce moment qu'il devient évident que la génération automatique n'a entraîné aucune amélioration de couverture, car clean() était déjà entièrement couverte.

Les tests générés ont permis de vérifier différentes entrées, mais ils n'ont pas parcouru de nouvelles instructions ou branches mesurables avec JaCoCo.

Ce résultat est selon moi très intéressant : le nombre de tests est passé de 4 à 21, sans augmentation de la couverture du code.


J'ai intégré PITest 1.20.3 dans le fichier tika-core/pom.xml, avec la prise en charge de JUnit 5 avec l'aide de ChatGPT.

J'ai configuré l'outil pour générer des mutations uniquement dans CharsetUtils, puis effectué trois analyses successives : avec les tests originaux, plus ceux générés par ChatUniTest ensuite et finalement avec 3 tests manuels supplémentaires. 

Le nombre de tests est passé de 4 à 21 puis 24 et le nombre de mutants tués de 20 à 20 à 23. Le score de mutation est donc passé de 80% pour les 2 premières itérations à 92% avec les tests manuels et la couverte des lignes PITest de 78% à 81%. 

Les mutations détecté concernaient notamment des rempalcements de valeurs de retour (true, false, null ou objet vide) et des inversions de conditions. Les tests existants détectaient ces modifications lorsqu'elles modifiaient une valeur de retour vérifiée par leurs assertions.

Après l'ajout des 17 tests ChatUniTest, aucun mutant supplémentaire n'a été tué. Les 20 mutants déjà détectés ont continué à l'être, mais les cinq mutants non couverts sont restés non couverts.

Cette situation renforce l'observation faite avec JaCoCo : les tests générés n'ont pas exercé les portions du code qui étaient responsables des lacunes de couverture.

Les cinq mutants non couverts concernaient des chemins particuliers de isSupported() et forName(), principalement liés à l'intégration facultative d'ICU4J et à la gestion d'exceptions.

J'ai donc poursuivi l'expérimentation en écrivant des tests manuels.

 ciblant ces comportements.

2.8. Tests manuels supplémentaires

J'ai créé le fichier :

tika-core/src/test/java/org/apache/tika/utils/CharsetUtilsManualTest.java

Il contient trois tests destinés à exercer des comportements que les tests existants ne couvraient pas.

Afin de couvrir les branches de CharsetUtils associées à ICU4J, j'ai d'abord tenté d'intégrrer cette bibliothèque comme dépendance de test. Toutefois, j'ai rencontré plusieurs difficultés, notamment des erreurs liées aux versions des dépendances et un changement de comportement qui faisait échouer un test original d'Apache Tika.

J'ai donc choisi de ne pas conserver ICU4J dans la configuration finale du projet afin de préserver le fonctionnement des tests existants. J'ai plutôt utilisé la réflexion Java pour remplacer temporairement la référence à la méthode isSupportedICU par des méthodes simulées. ChatGPt m'a guidé dans l'utilisation de cette méthode après m'avoir aidé dans le débogage. 

Cette approche m'a permis de tester les différents comportements de CharsetUtils lorsque la méthode ICU4J retourne true, retourne false ou provoque une exception, sans dépendre de la bibliothèque réelle. La référence originale est restaurée après chaque test pour éviter d'affecter les autres tests.

Test manuel 1 : testICUSupportedCharset()

Intention : vérifier que isSupported() retourne true lorsque la méthode ICU4J simulée indique qu'un encodage est reconnu.

Donnée utilisée : "unknown-icu-charset".

Motivation : j'ai choisi un nom non reconnu par le JDK dans mon environnement de test pour m'assurer que le résultat positif provient bien de la branche ICU4J simulée.

Oracle : assertTrue(...), puisque la méthode simulée retoune explicitement true.

Test manuel 2 : testICUFallbackToJDK()
testICUFallbackToJDK()

Intention : vérifier le mécanisme de repli vers le JDK lorsque la méthode ICU4J simulée retourne false.

Données utilisées : "UTF-8" et "unknown-icu-charset".

Motivation : UTF-8 représente un encodage reconnu, tandis que le second nom représente un encodage inconnu. Cette combinaison permet de vérifier les deux résultats possibles du mécanisme de repli.

Oracles :
assertTrue(CharsetUtils.isSupported("UTF-8"));
assertTrue(CharsetUtils.isSupported("UTF-8"));
assertFalse(CharsetUtils.isSupported("unknown-icu-charset"));

Test manuel 3 : testICUUnexpectedException()

Intention : vérifier que isSupported() retourne false lorsqu'une exception inattendue survient pendant l'appel à la méthode ICU4J simulée.

Donnée utilisée : "UTF-8".

Motivation : cet encodage est normalement reconnu. Il permet donc de vérifier que le retour false découle de la gestion de l'exception plutôt que de l'invalidité de l'encodage.

Oracle :
assertFalse(CharsetUtils.isSupported("UTF-8"));


Avec ces trois tests supplémentaires, PITest a tué 23 mutants sur 25, plutôt que 20.

Le score de mutation est donc passé de :

80 % à 92 %, soit une augmentation de 12 points de pourcentage.

Les deux mutants restants étaient classés NO_COVERAGE, et non SURVIVED.

J'ai choisi de conserver ce résultat plutôt que d'ajouter davantage de tests artificiellement pour atteindre 100 %.


Section Mahdi Slimani




Welcome to Apache Tika  <https://tika.apache.org/>
=================================================

[![license](https://img.shields.io/github/license/apache/tika.svg?maxAge=2592000)](http://www.apache.org/licenses/LICENSE-2.0)
[![Jenkins](https://img.shields.io/jenkins/s/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/)
[![Jenkins tests](https://img.shields.io/jenkins/t/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/lastBuild/testReport/)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.tika/tika.svg?maxAge=86400)](http://search.maven.org/#search|ga|1|g%3A%22org.apache.tika%22)

Apache Tika(TM) is a toolkit for detecting and extracting metadata and structured text content from various documents using existing parser libraries.

Tika is a project of the [Apache Software Foundation](https://www.apache.org).

Apache Tika, Tika, Apache, the Apache feather logo, and the Apache Tika project logo are trademarks of The Apache Software Foundation.

Quick Start
===========

**Parse a file in Java:**

```java
import org.apache.tika.Tika;

Tika tika = new Tika();
String text = tika.parseToString(new File("document.pdf"));
System.out.println(text);
```

**From the command line:**

```bash
java -jar tika-app-*.jar --text document.pdf
```

**Maven dependency:**

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>4.x.y</version>
    <type>pom</type>
</dependency>
```

Getting Started
===============
Pre-built binaries of Apache Tika standalone applications are available
from https://tika.apache.org/download.html . Pre-built binaries of all the
Tika jars can be fetched from Maven Central or your favourite Maven mirror.

**Tika 2.X and support for Java 8 reached End of Life (EOL) in April, 2025. 
See [Tika Roadmap 2.x, 3.x and beyond](https://cwiki.apache.org/confluence/display/TIKA/Tika+Roadmap+--+2.x%2C+3.x+and+Beyond).** 

Tika is based on **Java 17** and uses the [Maven 3](https://maven.apache.org) build system.
**N.B.** [Docker](https://www.docker.com/products/personal) is used for tests in tika-integration-tests. If Docker is not installed, those tests are skipped.

To build Tika from source, use the following command in the main directory:

    ./mvnw clean install

The Maven wrapper (`mvnw`) is included in the repository and will automatically download
the correct Maven version if needed. On Windows, use `mvnw.cmd` instead.

The build consists of a number of components, including a standalone runnable jar that you can use to try out Tika features. You can run it like this:

    java -jar tika-app/target/tika-app-*.jar --help


To build a specific project (for example, tika-server-standard):

    ./mvnw clean install -am -pl :tika-server-standard

If the ossindex-maven-plugin is causing the build to fail because a dependency
has now been discovered to have a vulnerability:

    ./mvnw clean install -Dossindex.skip


Faster Builds
=============

**Fast profile** - Use `-Pfast` to skip tests, checkstyle, and spotless:

    ./mvnw clean install -Pfast

**Parallel builds** - Add `-T1C` to build with 1 thread per CPU core:

    ./mvnw clean install -Pfast -T1C

**Maven Daemon (mvnd)** - Keeps a warm JVM running for 2-3x faster rebuilds:

```bash
# Install: https://github.com/apache/maven-mvnd
# macOS: brew install mvndaemon/tap/mvnd

# Use exactly like mvn
mvnd clean install -Pfast
mvnd test -pl :tika-core
```

**Combine both** for maximum speed during development:

    mvnd clean install -Pfast -T1C


Reproducible Builds
===================

Apache Tika supports [reproducible builds](https://reproducible-builds.org/). This means
that building the same source code with the same JDK version should produce
byte-for-byte identical artifacts, regardless of the build machine or time.

Key configuration:
- `project.build.outputTimestamp` is set in `tika-parent/pom.xml`
- All Maven plugins are configured to produce deterministic output

To verify the build plan supports reproducibility:

    ./mvnw artifact:check-buildplan

To verify two builds produce identical artifacts:

    ./mvnw clean install -DskipTests
    mv ~/.m2/repository/org/apache/tika tika-build-1
    ./mvnw clean install -DskipTests
    diff -r tika-build-1 ~/.m2/repository/org/apache/tika


Maven Dependencies
==================

Apache Tika provides *Bill of Material* (BOM) artifact to align Tika module versions and simplify version management. 
To avoid convergence errors in your own project, import this
bom or Tika's parent pom.xml in your dependency management section.

If you use Apache Maven:

```xml
<project>
  <dependencyManagement>
    <dependencies>
      <dependency>
       <groupId>org.apache.tika</groupId>
       <artifactId>tika-bom</artifactId>
       <version>4.x.y</version>
       <type>pom</type>
       <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.apache.tika</groupId>
      <artifactId>tika-parsers-standard-package</artifactId>
      <type>pom</type>
      <!-- version not required since BOM included -->
    </dependency>
  </dependencies>
</project>
```

For Gradle:

```kotlin
dependencies {
  implementation(platform("org.apache.tika:tika-bom:4.x.y"))

  // version not required since bom (platform in Gradle terms)
  implementation("org.apache.tika:tika-parsers-standard-package@pom")
}
```

Migrating to 4.x
================
TBD

Contributing
============
See [CONTRIBUTING.md](CONTRIBUTING.md) and https://tika.apache.org/contribute.html

[![contributors](https://contributors-img.web.app/image?repo=apache/tika)](https://github.com/apache/tika/graphs/contributors)

Building from a Specific Tag
============================
Let's assume that you want to build the 3.0.1 tag:
```
0. Download and install hub.github.com
1. git clone https://github.com/apache/tika.git
2. cd tika
3. git checkout 3.0.1
4. ./mvnw clean install
```

If a new vulnerability has been discovered between the date of the
tag and the date you are building the tag, you may need to build with:

```
4. ./mvnw clean install -Dossindex.skip
```

If a local test is not working in your environment, please notify
 the project at dev@tika.apache.org. As an immediate workaround,
 you can turn off individual tests with e.g.:

```
4. ./mvnw clean install -Dossindex.skip -Dtest=\!UnpackerResourceTest#testPDFImages
```

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2011 The Apache Software Foundation.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at

<https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.

Apache Tika includes a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the licenses listed in the LICENSE.txt file.

Export Control
==============

This distribution includes cryptographic software.  The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.  BEFORE using any encryption software, please  check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to  see if this is permitted.  See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms.  The form and manner of this Apache Software Foundation distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

Apache Tika uses the Bouncy Castle generic encryption libraries for extracting text content and metadata from encrypted PDF files.  See <http://www.bouncycastle.org/> for more details on Bouncy Castle.  

Mailing Lists
=============

* user@tika.apache.org - About using Tika
* dev@tika.apache.org - About developing Tika

Subscribe by sending a message to `{list}-subscribe@tika.apache.org`.

Issue Tracker
=============

https://issues.apache.org/jira/browse/TIKA

Security
========

See [SECURITY.md](SECURITY.md) and https://tika.apache.org/security.html
