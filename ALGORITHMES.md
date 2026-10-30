# Comprendre la partie avancée : DFS et BFS

Le jeu permet à un humain de choisir des mouvements.
Un solveur choisit et teste les mouvements automatiquement pour trouver une suite qui mène à la victoire.
Cette version contient les deux méthodes demandées, avec les structures programmées dans la classe Sokoban.

## 1. Observe avant de lire le code

Lance LANCER.bat. Dans le menu : 2, puis 2 pour sélectionner le niveau 2, puis 4 pour comparer.
Valide chaque saisie avec Entrée.

Avec le code et les niveaux fournis, les résultats vérifiés sont :

| Niveau | Mouvements DFS | Mouvements BFS | États explorés DFS | États explorés BFS |
|---|---:|---:|---:|---:|
| 1 | 1 | 1 | 2 | 2 |
| 2 | 24 | 6 | 1 563 | 187 |
| 3 | 20 | 18 | 136 | 11 386 |
| 4 | 155 | 5 | 4 590 | 54 |

Les chemins et ces compteurs dépendent des niveaux, de l'ordre d'exploration et de l'élimination des coins bloqués.
Les temps dépendent aussi de la machine et de l'échauffement de Java : ils ne sont pas figés dans ce guide.
BFS trouve ici les solutions les plus courtes. Cela ne veut pas dire qu'il explore toujours moins d'états : regarde le niveau 3.

**Premier exercice :** pourquoi peut-on gagner avec 24 mouvements et aussi avec 6 mouvements sur une même grille ?
Réponse à chercher : certaines suites font des détours tout en aboutissant à la victoire.

## 2. Qu'est-ce qu'un état ?

Un état est une photographie de la position du joueur et de toutes les caisses à un instant donné.
Le terrain fixe, c'est-à-dire les murs et les objectifs, est commun à toute la recherche.

Changer la position du joueur crée un autre état, même si les caisses n'ont pas bougé.
Revenir aux mêmes positions du joueur et de toutes les caisses redonne le même état.

`encoderEtat()` transforme les positions en un texte. Chaque case y occupe une position :

```java
int position = ligne * largeur + colonne;
```

Pour retrouver les coordonnées :

```java
int ligne = position / largeur;
int colonne = position % largeur;
```

`/` est ici une division entière et `%` donne le reste.
Sur une grille de largeur 8, la case `[2][3]` a l'indice `2 * 8 + 3 = 19`.

Le texte contient `@`, `$` et des espaces. Il contient aussi un emplacement vide pour chaque mur, mais le mur lui-même est lu dans `terrain`.
Les caisses ne portent pas de numéro : échanger deux caisses ne crée pas artificiellement un état différent.
Un `String` est immuable : chaque voisin est un nouveau texte, l'ancien état ne change pas.

**Exercice :** quelle est la position aplatie du joueur initial du niveau 1, en `[4][3]` ?

## 3. Trouver les voisins

Depuis chaque état, le solveur essaie quatre directions : z, s, q, d.

`etatSuivant(etat, direction)` :

1. Trouve le joueur.
2. Calcule la case visée.
3. Vérifie les limites et les murs.
4. Si une caisse est présente, vérifie aussi la case derrière.
5. Produit le nouvel état si le déplacement est autorisé ; renvoie `null` sinon.

`null` signifie ici « aucun état voisin possible dans cette direction ».
Cette méthode ne modifie pas les variables de la vraie partie.
Les règles sont les mêmes que celles de `deplacerJoueur()` ; leurs résultats ont été comparés pendant les vérifications.

L'ensemble des états et des déplacements possibles forme un **graphe** :

```text
            Départ
           /      \
       État A    État B
       /   \        \
      C     D        E
```

Ce dessin est simplifié. Dans le jeu, plusieurs chemins peuvent rejoindre le même état et des déplacements peuvent former des boucles.

## 4. Pourquoi mémoriser les états visités ?

Sans mémoire, le solveur pourrait répéter : gauche, droite, gauche, droite...
Il doit reconnaître une position qu'il a déjà découverte.

`enregistrerEtat()` ajoute chaque état une seule fois, dès sa découverte.
Le programme utilise une **table de hachage construite avec un tableau `int[]`**, sans HashMap ni HashSet.

Le principe :

1. Calculer un numéro à partir du texte avec `hashCode()`.
2. Choisir une case du tableau à partir de ce numéro.
3. Si elle est occupée, comparer le texte complet.
4. Si c'est le même état, ne rien ajouter.
5. Si c'est un autre état, essayer la case suivante : c'est le sondage linéaire.

Deux textes différents peuvent avoir le même hash. C'est une collision, et la comparaison du texte complet évite de les confondre.
`hashCode()` fournit seulement un calcul sur le texte ; toute la structure de stockage et la gestion des collisions sont dans le projet.

Une case du tableau vaut 0 si elle est vide. Sinon elle contient l'indice de l'état plus 1.
La table possède au moins deux fois plus de cases que le maximum d'états autorisés.

**Exercice :** pourquoi ne faut-il pas considérer deux états comme identiques uniquement parce que leur hash est identique ?

## 5. BFS : explorer par distance croissante

BFS veut dire « recherche en largeur ».
Il explore d'abord les états à zéro mouvement, puis un mouvement, puis deux mouvements, etc.

Il utilise une **file FIFO** : premier entré, premier sorti.
Comme dans une file d'attente, on ajoute à la fin et on retire au début.

Dans le code :

```java
frontiere[finFrontiere++] = indice;    // Ajouter à la fin.
indice = frontiere[debutFrontiere++];  // Retirer au début.
```

Le tableau `frontiere` contient des indices vers les états, pas des copies supplémentaires des grilles.
Les compteurs de début et de fin font partie de notre implémentation de la file.

Sur le dessin simplifié, si A entre avant B, BFS explore :

```text
Départ, A, B, C, D, E
```

Chaque déplacement autorisé coûte exactement un mouvement, qu'il pousse une caisse ou non.
Donc **la première victoire retirée de la file a un nombre minimal de mouvements**.
Cela minimise les déplacements du joueur, pas uniquement le nombre de poussées.
La suppression des caisses bloquées dans un coin hors objectif ne retire aucune solution valide.

Dans `resoudreBFS()`, l'appel `rechercher(true)` choisit le fonctionnement de la file.

## 6. DFS : explorer d'abord en profondeur

DFS veut dire « recherche en profondeur ».
Il poursuit une branche avant de revenir vers les autres possibilités.

Il utilise ici une **pile LIFO** : dernier entré, premier sorti.
Imagine une pile d'assiettes : celle ajoutée en dernier est retirée en premier.

```java
frontiere[finFrontiere++] = indice; // Empiler.
indice = frontiere[--finFrontiere]; // Dépiler.
```

Sur le dessin simplifié, DFS peut explorer :

```text
Départ, A, C, D, B, E
```

Dans `resoudreDFS()`, l'appel `rechercher(false)` choisit le fonctionnement de la pile.
Les voisins sont empilés dans l'ordre inverse pour donner la priorité à z, puis s, q, d lorsqu'ils sont retirés.
La priorité locale est donc la même que celle de BFS, même si l'ordre global des états explorés diffère.

DFS peut rencontrer une solution avec des détours avant d'essayer une solution courte.
Il ne garantit pas le minimum de mouvements. Il peut cependant trouver rapidement une victoire, comme au niveau 3.

### Et la récursivité demandée dans les explications du sujet ?

Le sujet autorise une réalisation récursive de DFS ; cette version choisit une pile explicite en tableau.
Cela évite de dépendre de la profondeur limitée de la pile d'appels de Java.

Pour expliquer l'équivalent récursif :

- **Cas de base réussi :** l'état est gagnant, on renvoie le chemin.
- **Cas sans poursuite :** état déjà visité, bloqué ou sans nouveau voisin exploitable.
- **Cas récursif :** rechercher une solution depuis chacun des nouveaux états voisins, un par un.
- **Condition d'arrêt globale :** une victoire est trouvée, toutes les possibilités retenues sont épuisées, ou la limite de ressources est atteinte.

Dans notre version, la boucle et la pile explicite réalisent cette exploration sans appeler `resoudreDFS()` récursivement.
La table des visités empêche les cycles d'être répétés indéfiniment.

**Exercice :** à partir de `rechercher()`, retrouve l'unique choix de retrait qui transforme la file BFS en pile DFS.

## 7. Retrouver la suite des mouvements

Savoir qu'une victoire existe ne suffit pas : il faut afficher comment y arriver.
Pour chaque état découvert, le programme conserve :

| Tableau | Information |
|---|---|
| `etatsRecherche` | Le texte de l'état |
| `parentsRecherche` | L'indice de l'état précédent |
| `coupsRecherche` | La touche utilisée pour venir de ce parent |
| `frontiere` | Les indices à explorer dans la file ou la pile |
| `visites` | La table permettant de reconnaître les états déjà découverts |

L'état de départ a pour parent `-1` : il n'a pas de prédécesseur.

À la victoire, `reconstruireSolution()` remonte les parents jusqu'au départ et place les touches dans l'ordre :

```text
Départ --q--> A --z--> B --d--> Victoire
Chemin reconstruit : qzd
```

Cela évite de mémoriser un texte de chemin complet pour chaque état.
La solution affichée peut ensuite être rejouée avec `rejouerSolution()` dans le vrai jeu.

## 8. Agrandir les tableaux

Les tableaux de recherche commencent avec 1 024 emplacements au maximum.
S'ils sont pleins, `agrandirRecherche()` crée des tableaux deux fois plus grands, dans la limite autorisée.
`System.arraycopy()` copie les éléments existants.

Cette méthode de copie fait partie de Java ; elle ne remplace pas l'implémentation de la pile, de la file ou de la table de visités.
Aucune collection comme ArrayList, ArrayDeque, Stack, HashMap ou HashSet n'est utilisée.
Le même principe sert à agrandir l'historique d'annulation.

## 9. Détecter les blocages simples

Une caisse située hors d'un objectif, avec un mur vertical et un mur horizontal adjacents, est coincée dans un coin.
Puisqu'on ne peut pas tirer, elle ne pourra plus sortir.

`etatBloque()` détecte ces situations :

- Pendant une partie, le jeu signale le problème et propose u pour annuler ou r pour recommencer.
- Pendant une recherche, le solveur écarte cet état inutile.

Une caisse dans un coin **sur un objectif** reste acceptée.
Ce détecteur ne reconnaît pas tous les blocages : par exemple, certaines caisses sont immobilisées entre elles ou contre un long mur.
Le solveur devra alors explorer davantage pour conclure.

## 10. Lire les résultats correctement

- **États découverts :** états distincts mémorisés, y compris ceux encore en attente.
- **États explorés :** états retirés de la file ou de la pile pour être examinés, victoire comprise.
- **Configurations bloquées écartées :** rencontres rejetées par le détecteur de coins. La même configuration peut être rencontrée plusieurs fois.
- **Mouvements :** longueur du chemin gagnant.
- **Temps :** durée de la recherche, initialisation et reconstruction comprises, sans les saisies et sans la lecture pas à pas.

Le programme distingue trois résultats :

| Statut | Signification |
|---|---|
| TROUVEE | Une solution a été trouvée et peut être rejouée |
| IMPOSSIBLE | Les états accessibles retenus ont été épuisés, ou le départ est définitivement bloqué |
| LIMITE | Le plafond de mémorisation empêche de poursuivre : on ne sait pas conclure |

Le plafond par défaut est de 100 000 états mémorisés, défini par `MAX_ETATS`.
Il est vérifié lorsqu'un nouvel état distinct doit être ajouté.
Atteindre ce plafond n'est pas une preuve qu'aucune solution n'existe.

## 11. Avantages, limites et complexité

| Aspect | DFS dans ce projet | BFS dans ce projet |
|---|---|---|
| Structure d'attente | Pile en tableau | File en tableau |
| Ordre | Une branche en profondeur | Distance croissante depuis le départ |
| Plus courte solution | Non garantie | Garantie si une solution est trouvée |
| Rapidité | Dépend des premières branches suivies | Dépend notamment de la profondeur de la solution |
| Mémoire | Tous les états découverts restent mémorisés | Tous les états découverts restent mémorisés |

Si V est le nombre d'états mémorisés, E le nombre de transitions examinées et C le nombre de cases :

- Le parcours abstrait d'un graphe est en O(V + E) avec des opérations de base constantes.
- Ici, copier, vérifier et comparer un état dépend de sa taille C. Avec un hachage bien réparti, le temps attendu est de l'ordre de O((V + E) × C).
- En cas de nombreuses collisions, le sondage peut devenir coûteux : le comportement attendu du hachage n'est pas une garantie au pire cas.
- La mémoire des états vaut O(V × C), à laquelle s'ajoutent les tableaux d'indices en O(V).
- Chaque état a au plus quatre voisins, mais le nombre d'états peut devenir très grand quand les grilles et le nombre de caisses augmentent.

Dans cette implémentation, DFS ne conserve pas seulement le chemin courant : il mémorise aussi les états visités et leurs parents.
Il ne faut donc pas affirmer qu'il consomme toujours beaucoup moins de mémoire que BFS.
Les mesures d'une seule exécution sont indicatives ; les mêmes performances ne sont pas garanties sur d'autres niveaux.

## 12. Un ordre conseillé pour étudier le code

1. Rejouer la solution du niveau 1.
2. Comprendre `encoderEtat()` et les indices.
3. Suivre un appel à `etatSuivant()`.
4. Comprendre la différence entre empiler/dépiler et enfiler/défiler.
5. Lire `rechercher()` en ignorant d'abord les détails du hachage.
6. Suivre `reconstruireSolution()` sur un chemin de trois touches.
7. Revenir à `enregistrerEtat()` pour les doublons et les collisions.
8. Comparer les niveaux 2 et 3 et expliquer leurs résultats avec tes mots.

Il n'est pas nécessaire de tout retenir en une lecture. Le jeu permet d'observer chaque notion avant de la relier au code.
