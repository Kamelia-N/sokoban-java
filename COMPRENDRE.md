# Comprendre ton Sokoban, morceau par morceau

Le programme complet est disponible pour que tu puisses observer un vrai jeu.
Pour apprendre, nous allons relier ce que tu vois à ce que le code fait.
Lis une étape, essaie son exercice, puis passe à la suivante.

## Étape 1 — La grille est un tableau

Dans `src/Sokoban.java`, cherche `NIVEAUX`. C'est la liste des dessins de départ.
Un texte représente une ligne, et chaque caractère représente une case.

```java
"#  @   #"
```

Cette ligne contient un mur, deux espaces, le joueur, trois espaces et un mur : huit cases.
Les guillemets doubles entourent du texte. Les apostrophes, comme dans `'@'`, entourent un seul caractère.

Au lancement d'une partie, la méthode `chargerNiveau()` transforme le dessin en tableaux de travail.
Nous utilisons notamment :

```java
char[][] terrain;
```

`char` veut dire caractère. Les deux paires de crochets permettent d'utiliser deux indices : la ligne et la colonne.

```java
terrain[2][3]
```

Cela désigne la troisième ligne et la quatrième colonne, car les indices commencent à zéro.
Dans le premier niveau, cette case contient un objectif `.`.

**Exercice :** lance le niveau 1, tape q puis Entrée et observe où va le joueur.
Le joueur change-t-il de ligne ou de colonne ?

## Étape 2 — Le programme retient l'état du jeu

L'état est l'ensemble des informations nécessaires pour continuer la partie.

```java
static char[][] terrain;
static boolean[][] caisses;
static int ligneJoueur;
static int colonneJoueur;
static int mouvements;
```

- `terrain` contient les murs, les objectifs et le sol.
- `caisses` indique la présence d'une caisse avec `true`, son absence avec `false`.
- `ligneJoueur` et `colonneJoueur` indiquent la case du joueur.
- `mouvements` est un entier qui compte les déplacements réussis.

Une caisse peut recouvrir un objectif. Les deux informations doivent donc être conservées.
Par exemple, `terrain[2][3]` reste `'.'` même si `caisses[2][3]` devient `true`.
À l'écran, cette combinaison s'affiche avec `*`.
Le joueur sur un objectif s'affiche avec `+`, sans effacer le point du terrain.

`static` permet ici aux méthodes de la même classe d'accéder à ces variables partagées.
Cette organisation est choisie pour ce petit projet en une classe.

**Exercice :** au niveau 1, gagne avec z. Pourquoi le programme affiche-t-il `*` à la place de `$` ?

## Étape 3 — Les méthodes répartissent le travail

Une méthode est un morceau de programme qui réalise une tâche et que l'on peut appeler par son nom.

```java
chargerNiveau(numero);
afficherGrille();
```

`numero` est une information donnée à la première méthode : un paramètre.
`afficherGrille()` n'a pas de paramètre ; elle utilise les variables de la partie.

| Méthode | Sa responsabilité |
|---|---|
| main | Répéter le menu et lancer l'action choisie |
| lireCommande | Lire et nettoyer une saisie au clavier |
| choisirNiveau | Retenir le numéro du prochain niveau à jouer |
| chargerNiveau | Construire l'état initial et remettre le compteur à zéro |
| afficherGrille | Choisir et afficher le symbole de chaque case |
| jouer | Répéter les tours jusqu'à la victoire ou au retour au menu |
| dansGrille | Vérifier qu'une position est dans les limites |
| deplacementValide | Vérifier qu'un déplacement est autorisé |
| peutPousser | Vérifier que la destination d'une caisse est disponible |
| deplacerCaisse | Mettre à jour la position d'une caisse après validation |
| deplacerJoueur | Effectuer le déplacement autorisé et compter un mouvement |
| partieTerminee | Vérifier que toutes les caisses sont sur des objectifs |

Une méthode `void` fait une action sans renvoyer de valeur.
Une méthode `boolean` renvoie `true` ou `false` : elle répond à une question.
`return` renvoie un résultat ou termine immédiatement la méthode.

## Étape 4 — Un tour de jeu

La méthode `jouer()` répète les opérations suivantes :

```text
Afficher la grille
       |
Victoire ? -- oui --> Afficher le score et retourner au menu
       |
      non
       |
Lire une commande
       |
Choisir sa direction ou traiter r/x/une erreur
       |
Vérifier puis effectuer le déplacement si possible
       |
Revenir à l'affichage de la grille
```

`while (true)` recommence sans limite prédéfinie. Les instructions `return` permettent de sortir de la méthode.
`continue` passe directement au prochain tour de la boucle, par exemple après une saisie incorrecte.
Le `switch` choisit le traitement correspondant à la commande.

`Scanner` est le lecteur clavier fourni avec Java. `nextLine()` récupère le texte saisi jusqu'à Entrée.
Une commande est donc du texte : on la compare avec `.equals()` et on utilise `switch` pour les différents cas.

**Exercice :** tape bonjour pendant une partie. Observe le message et le compteur.
Retrouve le `default` du `switch` qui produit ce comportement.

## Étape 5 — Traduire une touche en déplacement

Une direction correspond à une variation de ligne et de colonne.
Le mot `delta` signifie ici variation.

| Touche | deltaLigne | deltaColonne |
|---|---:|---:|
| z | -1 | 0 |
| s | 1 | 0 |
| q | 0 | -1 |
| d | 0 | 1 |

Les lignes sont comptées du haut vers le bas. Monter diminue donc le numéro de ligne.

```java
int prochaineLigne = ligneJoueur + deltaLigne;
int prochaineColonne = colonneJoueur + deltaColonne;
```

Dans le niveau 1, le joueur commence en `[4][3]`.
Avec z, il vise `[3][3]`, qui contient la caisse. Il faut alors vérifier la case `[2][3]` derrière elle.
Cette case contient un objectif et aucune caisse : la poussée est autorisée.

**Exercice :** depuis la position initiale, quelle case le joueur vise-t-il si tu tapes d ?

## Étape 6 — Vérifier avant de modifier

`deplacementValide()` vérifie la case visée. Une position hors du tableau ou un mur bloque le déplacement.
Si la case contient une caisse, `peutPousser()` vérifie la case suivante :

```text
Joueur -> Caisse -> Case libre ou objectif : autorisé
Joueur -> Caisse -> Mur                   : bloqué
Joueur -> Caisse -> Autre caisse          : bloqué
```

Ensuite seulement, `deplacerJoueur()` modifie la partie :

1. Déplacer la caisse s'il y en a une.
2. Déplacer le joueur dans la case visée.
3. Ajouter 1 au compteur.

Le terrain fixe n'est jamais modifié par ces déplacements.
`mouvements++` ajoute un au compteur. Un mouvement impossible quitte la méthode avant cette instruction.

Quelques symboles utiles pour lire les conditions :

| Java | Sens |
|---|---|
| `=` | Affecter une valeur |
| `==` | Comparer deux valeurs primitives, par exemple deux caractères |
| `!=` | Différent de |
| `&&` | Et |
| `\|\|` | Ou |
| `!` | Négation |

`&&` et `||` évaluent de gauche à droite et s'arrêtent quand le résultat est connu.
C'est pourquoi le programme peut vérifier les limites avant de lire une case du tableau.

**Exercice :** depuis le début du niveau 1, tape q, q, puis q.
Les deux premiers déplacements passent ; le troisième rencontre le mur. Le compteur doit rester à 2.

## Étape 7 — Afficher et détecter la victoire

`afficherGrille()` utilise deux boucles `for` : une pour parcourir les lignes, l'autre pour les colonnes.
`System.out.print` affiche une case sans retour à la ligne.
Après une ligne entière, `System.out.println()` passe à la suivante.

Cette instruction choisit entre deux caractères :

```java
symbole = (symbole == '.') ? '*' : '$';
```

Elle veut dire : si le sol est un objectif, afficher `*` ; sinon afficher `$`.
C'est une forme courte de `if` / `else`, appelée opérateur ternaire.

`partieTerminee()` parcourt toutes les cases. Dès qu'elle trouve une caisse hors d'un objectif, elle renvoie `false`.
Si aucune caisse n'est mal placée, elle renvoie `true`.
Le chargement a déjà vérifié que le niveau contient au moins une caisse et autant d'objectifs que de caisses.

**Exercice :** explique avec tes mots pourquoi voir une seule caisse sur un objectif ne suffit pas à gagner au niveau 2.

## Étape 8 — Modifier le projet toi-même

Pour un premier changement sans difficulté :

1. Trouve le message `Bienvenue dans Sokoban !` dans `main`.
2. Remplace-le par ton propre message, en gardant les guillemets et le point-virgule.
3. Enregistre et relance LANCER.bat.
4. Vérifie que ton message apparaît.

Pour modifier un niveau, garde toutes les lignes à la même largeur, un seul joueur et autant de caisses que d'objectifs.
Entoure la zone de murs. Un dessin valide peut quand même être impossible à résoudre : il faut aussi tester la partie.

## Vérifier une version après modification

- Jouer au niveau 1 et gagner avec z.
- Essayer une commande vide et une commande inconnue.
- Marcher contre un mur et vérifier que le compteur reste inchangé.
- Recommencer avec r et vérifier que les positions et le compteur sont réinitialisés.
- Retourner au menu avec x, puis sélectionner un autre niveau.
- Vérifier qu'un objectif reste visible après le passage du joueur ou d'une caisse.

Tu peux maintenant relier une action à l'écran à la méthode responsable dans le code.
Lors de notre prochaine explication, nous pouvons prendre une seule méthode et la suivre ligne par ligne.


## Étape 9 — Annuler un mouvement

Avant chaque déplacement réussi, `memoriserMouvement()` enregistre la position du joueur et des caisses.
Ce petit instantané est ajouté à un tableau de textes appelé `historique`.

`annulerMouvement()` retire le dernier instantané, restaure les positions et diminue le compteur.
C'est une **pile** : le dernier mouvement effectué est le premier annulé.
Un mouvement refusé n'est pas mémorisé. Recommencer le niveau vide l'historique.

**Exercice :** au niveau 2, fais un déplacement puis une poussée. Tape u deux fois.
Vérifie les caisses, le joueur et le compteur. Tape u une troisième fois : aucune annulation ne doit être possible.

## Étape 10 — Passer aux solveurs

Une fois les déplacements compris, ouvre **ALGORITHMES.md**.
Les solveurs réutilisent les mêmes règles, mais testent de nombreuses positions sans déplacer le joueur à l'écran.
La lecture pas à pas permet ensuite de voir la solution dans le vrai jeu.
