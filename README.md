# Sokoban Java — ton premier projet complet

Un jeu de réflexion en console, avec les parties standard et avancée du projet ESIEA.
Le code utilise une seule classe Java, des tableaux et la bibliothèque standard.
Les solveurs DFS et BFS sont inclus. Les piles, files et états visités sont gérés avec des tableaux programmés dans le projet, sans ArrayList, HashMap ou HashSet.

## 1. Commence par jouer

Ouvre le dossier du projet et double-clique sur **LANCER.bat**.
Le script compile le code puis ouvre le jeu dans une console. Java 17 est installé sur ton PC.

Dans le menu, tape **1** puis **Entrée** pour jouer au premier niveau.
Tape ensuite **z** puis **Entrée** : tu pousses la caisse sur son objectif et tu gagnes.
Ce premier niveau est volontairement très simple pour comprendre la règle.

Toutes les commandes doivent être validées avec Entrée.

| Touche | Action |
|---|---|
| z | Monter |
| s | Descendre |
| q | Aller à gauche |
| d | Aller à droite |
| r | Recommencer le niveau depuis le début |
| u | Annuler le dernier mouvement réussi, même une poussée |
| x | Retourner au menu pendant une partie |

Pour essayer un autre niveau : choisis **2** dans le menu, entre son numéro, puis choisis **1** pour le lancer.
Quitter une partie abandonne sa progression ; rejouer recharge le niveau initial.
Les tentatives bloquées et les commandes incorrectes n'augmentent pas le compteur.

## 2. Les fichiers

```text
Sokoban-Java/
  LANCER.bat         Compile et lance le jeu sous Windows
  README.md          Ce mode d'emploi
  COMPRENDRE.md      Explication du jeu avec exercices
  ALGORITHMES.md     Explication de DFS/BFS et des structures
  VERIFICATIONS.md   Résultats des vérifications effectuées
  src/
    Sokoban.java     Tout le code du jeu
  build/             Créé au premier lancement : programme compilé
```

Le fichier `.java` est le texte que tu modifies. Le fichier `.class` dans `build` est produit par Java.
Après une modification, ferme le jeu puis relance LANCER.bat pour recompiler.
Le projet peut aussi être ouvert dans un éditeur Java ; le fichier à exécuter est `src/Sokoban.java`.

## 3. Comprendre les symboles

| Symbole | Signification |
|---|---|
| # | Mur |
| espace | Sol libre |
| @ | Joueur sur le sol |
| . | Objectif |
| $ | Caisse sur le sol |
| * | Caisse sur un objectif |
| + | Joueur sur un objectif |

Tu peux pousser une caisse si la case derrière elle est disponible.
Tu ne peux pas tirer une caisse, pousser deux caisses à la fois ni traverser un mur.
Toutes les caisses doivent être sur les objectifs pour gagner.
Une caisse peut rester bloquée dans un coin : utilise r pour recommencer.
Les caisses coincées dans un coin hors objectif sont détectées. Les autres types de blocage peuvent nécessiter la recherche complète du solveur.

## 4. Ce que cette version contient

- Affichage et déplacements dans une console.
- Poussée des caisses et conservation des objectifs.
- Gestion des murs et des limites de grille.
- Détection de la victoire et compteur de mouvements réussis.
- Quatre niveaux et menu de sélection.
- Validation des saisies, y compris une ligne vide ou plusieurs lettres.
- Résolution automatique par DFS et BFS, avec affichage des touches et lecture pas à pas.
- Comparaison des mouvements, des états explorés, des états découverts et du temps.
- Détection des états déjà visités par une table de hachage construite avec des tableaux.
- Bonus : redémarrage avec r, annulation avec u, niveau supplémentaire, détection de certains blocages, réduction des recherches inutiles et comparaison des temps.
- BFS trouve une solution qui minimise les déplacements du joueur, poussées comprises, lorsque la recherche aboutit.

## 5. Utiliser la partie avancée

Menu principal :

```text
1. Jouer au niveau selectionne
2. Choisir un niveau
3. Resoudre automatiquement (DFS ou BFS)
4. Comparer DFS et BFS
5. Lire les regles
6. Quitter
```

Pour découvrir la différence entre les algorithmes :

1. Choisis **2**, puis **2** pour sélectionner le niveau 2.
2. Choisis **4** pour afficher la comparaison.
3. Choisis **3**, puis **2** pour résoudre avec BFS.
4. Le programme affiche la suite de touches gagnante. Tape **v** pour la regarder pas à pas.
5. Appuie sur Entrée pour chaque mouvement, ou tape **x** pour revenir au menu.

Les solveurs partent du début du niveau sélectionné. Ils ne modifient pas les positions pendant la recherche.
La lecture pas à pas applique ensuite les coups au jeu, ce qui permet de les observer.
La comparaison utilise le même départ, le même ordre de priorité des directions et les mêmes règles de blocage.
Le temps affiché est une mesure indicative d'une exécution, pas un classement universel des algorithmes.

Pour limiter la mémoire utilisée, la recherche s'arrête à **100 000 états distincts mémorisés** si elle doit encore ajouter un état.
Dans ce cas, elle affiche que le résultat est **indéterminé**. Elle ne prétend pas que le niveau est impossible.
Les quatre niveaux fournis sont résolus par les deux algorithmes sous cette limite.

## 6. Lancer à la main dans PowerShell

Ouvre un terminal dans le dossier Sokoban-Java puis exécute :

```powershell
New-Item -ItemType Directory -Path build -Force | Out-Null
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d build src\Sokoban.java
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -cp build Sokoban
```

La première commande prépare le dossier de compilation.
`javac` compile ; `java` lance le programme. Aucune connexion Internet n'est nécessaire.
LANCER.bat utilise JAVA_HOME si cette variable désigne un JDK, sinon le JDK 17 installé sur ton PC.
Si le projet est déplacé vers un autre ordinateur, il faudra y installer un JDK.

## 7. Pour apprendre

Lis maintenant **COMPRENDRE.md**, une étape à la fois, en gardant le code ouvert à côté.
Commence par l'étape 1 et son exercice ; tu n'as pas besoin de comprendre tous les fichiers d'un coup.

Après le fonctionnement du jeu, lis **ALGORITHMES.md**. Il explique les états, DFS, BFS, les tableaux de recherche et les différences observées.
