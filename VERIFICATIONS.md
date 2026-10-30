# Vérifications du jeu

La version actuelle contient le jeu Sokoban en console avec quatre niveaux.

## Résultats

- Compilation Java 17 avec `javac -Xlint:all` réussie, sans avertissement.
- Lancement via LANCER.bat vérifié jusqu'à la victoire.
- 33 vérifications du moteur : déplacements, murs, limites de grille, poussées, conservation des objectifs, victoire et compteur.
- 208 vérifications des annulations et des coins bloqués, y compris l'agrandissement de l'historique et sa remise à zéro.
- Les quatre niveaux ont été joués automatiquement par le protocole de vérification jusqu'à la victoire : ils sont tous gagnables.
- Saisies incorrectes, commandes vides, redémarrage, retour au menu et fin de saisie vérifiés.

## Parcours utilisés pour vérifier les niveaux

Chaque lettre ci-dessous est une commande à saisir puis à valider avec Entrée.
Ces parcours servent à reproduire une partie gagnante si nécessaire.

| Niveau | Commandes | Nombre de mouvements |
|---|---|---:|
| 1 | z | 1 |
| 2 | qzsddz | 6 |
| 3 | qzzzsssddzzzssddzz | 18 |
| 4 | zddzz | 5 |

Les tests vérifient aussi qu'une tentative refusée n'augmente pas le compteur et qu'un objectif reste présent sous le joueur ou une caisse.

La détection des blocages signale uniquement les caisses coincées dans un coin hors objectif. D'autres blocages restent possibles : annuler ou recommencer permet de les corriger.
