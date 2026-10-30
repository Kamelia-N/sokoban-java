import java.util.Scanner;

/**
 * Projet Sokoban - partie standard, en console.
 * Le terrain (murs/objectifs) et les caisses sont memorises separement.
 * Ainsi, un objectif reste present quand le joueur ou une caisse le recouvre.
 */
public class Sokoban {
    // Chaque texte represente une ligne. Toutes les lignes ont la meme largeur.
    static final String[][] NIVEAUX = {
        {
            "########",
            "#      #",
            "#  .   #",
            "#  $   #",
            "#  @   #",
            "#      #",
            "########"
        },
        {
            "########",
            "# . .  #",
            "# $ $  #",
            "#  @   #",
            "#      #",
            "########"
        },
        {
            "########",
            "# . . .#",
            "#      #",
            "# $ $ $#",
            "#  #   #",
            "#  @   #",
            "########"
        },
        {
            "#########",
            "#   .   #",
            "#   $   #",
            "# #   # #",
            "#  $ .  #",
            "# @     #",
            "#########"
        }
    };

    // Etat de la partie : ce que le programme doit retenir entre deux commandes.
    static char[][] terrain;
    static boolean[][] caisses;
    static int ligneJoueur;
    static int colonneJoueur;
    static int mouvements;
    static int niveauChoisi = 0; // Indice 0 = niveau affiche comme "Niveau 1".

    // Photographies des positions precedentes pour annuler les mouvements.
    static String[] historique;
    static int tailleHistorique;

    // Un seul lecteur pour toutes les saisies du programme.
    static final Scanner CLAVIER = new Scanner(System.in);

    public static void main(String[] args) {
        boolean continuer = true;
        System.out.println("Bienvenue dans Sokoban !");
        while (continuer) {
            afficherMenu();
            String choix = lireCommande();
            switch (choix) {
                case "1":
                    jouer(niveauChoisi);
                    break;
                case "2":
                    choisirNiveau();
                    break;
                case "3":
                    afficherRegles();
                    break;
                case "4":
                case "x":
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide : entre 1, 2, 3 ou 4.");
            }
        }
        System.out.println("A bientot !");
    }

    public static void afficherMenu() {
        System.out.println("\n========== SOKOBAN ==========");
        System.out.println("Niveau selectionne : " + (niveauChoisi + 1));
        System.out.println("1. Jouer au niveau selectionne");
        System.out.println("2. Choisir un niveau");
        System.out.println("3. Lire les regles");
        System.out.println("4. Quitter");
        System.out.print("Ton choix : ");
    }

    // nextLine lit toute la ligne : une saisie comme "zz" pourra etre refusee.
    // trim retire les espaces autour ; les majuscules sont acceptees aussi.
    // Si l'entree est fermee, "x" permet de terminer proprement.
    public static String lireCommande() {
        if (!CLAVIER.hasNextLine()) {
            return "x";
        }
        return CLAVIER.nextLine().trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static void choisirNiveau() {
        System.out.println("\nNiveaux disponibles :");
        for (int i = 0; i < NIVEAUX.length; i++) {
            System.out.println((i + 1) + ". Niveau " + (i + 1));
        }
        System.out.print("Numero du niveau (x : retour) : ");
        String choix = lireCommande();
        if (choix.equals("x")) {
            return;
        }
        // On compare des textes pour gerer aussi les lettres et les grands nombres.
        for (int i = 0; i < NIVEAUX.length; i++) {
            if (choix.equals(Integer.toString(i + 1))) {
                niveauChoisi = i;
                System.out.println("Niveau " + (i + 1) + " selectionne. Choisis 1 pour jouer.");
                return;
            }
        }
        System.out.println("Ce niveau n'existe pas.");
    }

    public static void afficherRegles() {
        System.out.println("\nPlace toutes les caisses sur les objectifs.");
        System.out.println("# : mur    . : objectif    @ : joueur    $ : caisse");
        System.out.println("* : caisse sur objectif    + : joueur sur objectif");
        System.out.println("Une case vide est representee par un espace.");
        System.out.println("Tu peux pousser une seule caisse si la case derriere est libre.");
        System.out.println("Tu ne peux pas tirer une caisse ni traverser un mur.");
        System.out.println("z : haut | s : bas | q : gauche | d : droite");
        System.out.println("r : recommencer | u : annuler | x : retourner au menu");
        System.out.println("Tape une commande puis appuie sur Entree.");
        System.out.println("Seuls les deplacements reussis comptent comme mouvements.");
    }

    // Transforme le dessin initial en tableaux independants pour une nouvelle partie.
    public static void chargerNiveau(int numero) {
        if (numero < 0 || numero >= NIVEAUX.length) {
            throw new IllegalArgumentException("Numero de niveau invalide.");
        }
        String[] dessin = NIVEAUX[numero];
        int hauteur = dessin.length;
        int largeur = dessin[0].length();
        terrain = new char[hauteur][largeur];
        caisses = new boolean[hauteur][largeur];
        mouvements = 0;
        historique = new String[64];
        tailleHistorique = 0;
        int joueurs = 0;
        int nombreCaisses = 0;
        int objectifs = 0;

        for (int ligne = 0; ligne < hauteur; ligne++) {
            if (dessin[ligne].length() != largeur) {
                throw new IllegalArgumentException("Les lignes doivent avoir la meme largeur.");
            }
            for (int colonne = 0; colonne < largeur; colonne++) {
                char symbole = dessin[ligne].charAt(colonne);
                if ("# @$.+*".indexOf(symbole) < 0) {
                    throw new IllegalArgumentException("Symbole de niveau inconnu.");
                }
                terrain[ligne][colonne] = ' ';
                if (symbole == '#') {
                    terrain[ligne][colonne] = '#';
                }
                if (symbole == '.' || symbole == '*' || symbole == '+') {
                    terrain[ligne][colonne] = '.';
                    objectifs++;
                }
                if (symbole == '$' || symbole == '*') {
                    caisses[ligne][colonne] = true;
                    nombreCaisses++;
                }
                if (symbole == '@' || symbole == '+') {
                    ligneJoueur = ligne;
                    colonneJoueur = colonne;
                    joueurs++;
                }
            }
        }
        if (joueurs != 1 || nombreCaisses == 0 || nombreCaisses != objectifs) {
            throw new IllegalArgumentException("Il faut un joueur et autant d'objectifs que de caisses.");
        }
    }

    public static void afficherGrille() {
        System.out.println("\nNiveau " + (niveauChoisi + 1) + " | Mouvements : " + mouvements);
        for (int ligne = 0; ligne < terrain.length; ligne++) {
            for (int colonne = 0; colonne < terrain[ligne].length; colonne++) {
                char symbole = terrain[ligne][colonne];
                // On choisit un symbole a afficher sans modifier le terrain.
                if (caisses[ligne][colonne]) {
                    symbole = (symbole == '.') ? '*' : '$';
                }
                if (ligne == ligneJoueur && colonne == colonneJoueur) {
                    symbole = (terrain[ligne][colonne] == '.') ? '+' : '@';
                }
                System.out.print(symbole);
            }
            System.out.println();
        }
    }

    // Les limites sont verifiees AVANT tout acces au tableau.
    public static boolean dansGrille(int ligne, int colonne) {
        return ligne >= 0 && ligne < terrain.length
            && colonne >= 0 && colonne < terrain[ligne].length;
    }

    public static boolean peutPousser(int ligneCaisse, int colonneCaisse,
                                     int deltaLigne, int deltaColonne) {
        int destinationLigne = ligneCaisse + deltaLigne;
        int destinationColonne = colonneCaisse + deltaColonne;
        return dansGrille(destinationLigne, destinationColonne)
            && terrain[destinationLigne][destinationColonne] != '#'
            && !caisses[destinationLigne][destinationColonne];
    }

    public static boolean deplacementValide(int deltaLigne, int deltaColonne) {
        // Un deplacement autorise change exactement une coordonnee d'une case.
        if (Math.abs(deltaLigne) + Math.abs(deltaColonne) != 1) {
            return false;
        }
        int prochaineLigne = ligneJoueur + deltaLigne;
        int prochaineColonne = colonneJoueur + deltaColonne;
        if (!dansGrille(prochaineLigne, prochaineColonne)
                || terrain[prochaineLigne][prochaineColonne] == '#') {
            return false;
        }
        if (caisses[prochaineLigne][prochaineColonne]) {
            return peutPousser(prochaineLigne, prochaineColonne, deltaLigne, deltaColonne);
        }
        return true;
    }

    // Appelee uniquement apres la validation du mouvement.
    public static void deplacerCaisse(int ligne, int colonne,
                                     int deltaLigne, int deltaColonne) {
        caisses[ligne][colonne] = false;
        caisses[ligne + deltaLigne][colonne + deltaColonne] = true;
    }

    // true = mouvement effectue ; false = etat du jeu conserve.
    public static boolean deplacerJoueur(int deltaLigne, int deltaColonne) {
        if (!deplacementValide(deltaLigne, deltaColonne)) {
            return false;
        }
        memoriserMouvement();
        int prochaineLigne = ligneJoueur + deltaLigne;
        int prochaineColonne = colonneJoueur + deltaColonne;
        if (caisses[prochaineLigne][prochaineColonne]) {
            deplacerCaisse(prochaineLigne, prochaineColonne, deltaLigne, deltaColonne);
        }
        ligneJoueur = prochaineLigne;
        colonneJoueur = prochaineColonne;
        mouvements++;
        return true;
    }

    public static boolean partieTerminee() {
        for (int ligne = 0; ligne < terrain.length; ligne++) {
            for (int colonne = 0; colonne < terrain[ligne].length; colonne++) {
                if (caisses[ligne][colonne] && terrain[ligne][colonne] != '.') {
                    return false; // Une seule caisse mal placee suffit a continuer.
                }
            }
        }
        return true;
    }

    public static void jouer(int numero) {
        chargerNiveau(numero);
        System.out.println("\nz : haut | s : bas | q : gauche | d : droite");
        System.out.println("r : recommencer | u : annuler | x : menu (puis Entree)");
        while (true) {
            afficherGrille();
            if (partieTerminee()) {
                System.out.println("Victoire ! Nombre de mouvements : " + mouvements);
                return;
            }
            System.out.print("Commande : ");
            String commande = lireCommande();
            int deltaLigne = 0;
            int deltaColonne = 0;
            switch (commande) {
                case "z": deltaLigne = -1; break;
                case "s": deltaLigne = 1; break;
                case "q": deltaColonne = -1; break;
                case "d": deltaColonne = 1; break;
                case "x": return;
                case "u":
                    if (!annulerMouvement()) {
                        System.out.println("Aucun mouvement a annuler.");
                    }
                    continue;
                case "r":
                    chargerNiveau(numero);
                    System.out.println("Niveau recommence.");
                    continue;
                default:
                    System.out.println("Commande invalide : z, s, q, d, r, u ou x.");
                    continue;
            }
            if (!deplacerJoueur(deltaLigne, deltaColonne)) {
                System.out.println("Deplacement impossible : mur ou caisse bloquee.");
            } else if (etatBloque(encoderEtat())) {
                System.out.println("Caisse coincee dans un coin hors objectif : u pour annuler, r pour recommencer.");
            }
        }
    }

    // Une photographie des positions : un caractere par case, ligne apres ligne.
    public static String encoderEtat() {
        int largeur = terrain[0].length;
        char[] contenu = new char[terrain.length * largeur];
        for (int ligne = 0; ligne < terrain.length; ligne++) {
            for (int colonne = 0; colonne < largeur; colonne++) {
                contenu[ligne * largeur + colonne] = caisses[ligne][colonne] ? '$' : ' ';
            }
        }
        contenu[ligneJoueur * largeur + colonneJoueur] = '@';
        return new String(contenu);
    }

    // Retablit une photographie sans changer le terrain (murs et objectifs).
    public static void restaurerEtat(String etat) {
        int largeur = terrain[0].length;
        for (int ligne = 0; ligne < terrain.length; ligne++) {
            for (int colonne = 0; colonne < largeur; colonne++) {
                char symbole = etat.charAt(ligne * largeur + colonne);
                caisses[ligne][colonne] = symbole == '$';
                if (symbole == '@') {
                    ligneJoueur = ligne;
                    colonneJoueur = colonne;
                }
            }
        }
    }

    // Sauvegarde les positions avant un mouvement pour pouvoir l'annuler.
    public static void memoriserMouvement() {
        if (tailleHistorique == historique.length) {
            String[] agrandi = new String[historique.length * 2];
            System.arraycopy(historique, 0, agrandi, 0, historique.length);
            historique = agrandi;
        }
        historique[tailleHistorique++] = encoderEtat();
    }

    // Retrouve la derniere photographie et retire un mouvement au compteur.
    public static boolean annulerMouvement() {
        if (tailleHistorique == 0) {
            return false;
        }
        restaurerEtat(historique[--tailleHistorique]);
        historique[tailleHistorique] = null;
        mouvements--;
        return true;
    }

    // Les murs et les bords peuvent empecher une caisse de sortir d'un coin.
    public static boolean murOuBord(int ligne, int colonne) {
        return !dansGrille(ligne, colonne) || terrain[ligne][colonne] == '#';
    }

    // Signale une caisse dans un coin hors objectif. Ne detecte pas tous les blocages.
    public static boolean etatBloque(String etat) {
        int largeur = terrain[0].length;
        for (int position = 0; position < etat.length(); position++) {
            if (etat.charAt(position) != '$') {
                continue;
            }
            int ligne = position / largeur;
            int colonne = position % largeur;
            if (terrain[ligne][colonne] == '.') {
                continue; // Une caisse dans un coin objectif n'est pas rejetee.
            }
            boolean vertical = murOuBord(ligne - 1, colonne) || murOuBord(ligne + 1, colonne);
            boolean horizontal = murOuBord(ligne, colonne - 1) || murOuBord(ligne, colonne + 1);
            if (vertical && horizontal) {
                return true;
            }
        }
        return false;
    }
}
