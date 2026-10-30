# Vérifications de la version standard et avancée

Vérifications effectuées avec le JDK 17 installé sur ce poste.

## Compilation et lancement

- Compilation du code avec `javac -Xlint:all` : réussie, sans avertissement.
- Compilation puis partie complète via LANCER.bat : réussies.
- Aucune bibliothèque externe ; une seule classe Java dans le projet livré.
- File, pile, historique, stockage des parents et table des visités programmés avec des tableaux.

## Jeu et recherche

- 33 vérifications du moteur : murs, limites, poussées, objectifs, victoire, remise à zéro et mouvements refusés.
- 490 vérifications avancées : solutions légales, victoire après relecture, absence de modification de la partie pendant la recherche, annulation, agrandissement de l'historique, blocages simples, recherche sans solution, départ déjà gagné, plafond de recherche, collisions et doublons dans la table de hachage.
- 58 800 transitions comparées entre le moteur du joueur et celui des solveurs sur les états découverts des quatre niveaux : mêmes déplacements autorisés et mêmes positions obtenues.
- Agrandissement des tableaux de recherche au-delà de 1 024 états vérifié.
- Longueur de chaque solution BFS comparée à une recherche de référence indépendante sur les mêmes dessins : 1, 6, 18 et 5 mouvements.
- Quatre solutions DFS et quatre solutions BFS rejouées intégralement dans la console jusqu'à la victoire.
- Menus de résolution et de comparaison, saisies incorrectes, annulations, redémarrage, sortie de partie et fin de saisie vérifiés.

## Résultats déterministes de cette version

| Niveau | Mouvements DFS | États explorés DFS | Mouvements BFS | États explorés BFS |
|---|---:|---:|---:|---:|
| 1 | 1 | 2 | 1 | 2 |
| 2 | 24 | 1 563 | 6 | 187 |
| 3 | 20 | 136 | 18 | 11 386 |
| 4 | 155 | 4 590 | 5 | 54 |

Ces résultats ne sont pas des valeurs écrites dans les solveurs : ils sont calculés à chaque exécution.
Changer les niveaux, l'ordre d'exploration ou les optimisations peut modifier les compteurs et les solutions DFS.
Les temps d'exécution varient et ne figurent donc pas dans le tableau.

## Limites explicites

La détection rapide des blocages couvre les caisses dans un coin formé par les murs ou les limites, hors objectif.
Elle ne couvre pas tous les blocages possibles de Sokoban.
La recherche est plafonnée à 100 000 états distincts. Une interruption à ce plafond produit un résultat indéterminé, pas une déclaration d'impossibilité.
Ces vérifications couvrent les niveaux fournis et plusieurs situations de bord ; elles ne garantissent pas les performances sur n'importe quelle grande grille.
