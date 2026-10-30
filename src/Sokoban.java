import java.util.Scanner;

/**
 * Projet Sokoban - parties standard et avancee, en console.
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
                    resoudreAutomatiquement();
                    break;
                case "4":
                    comparerAlgorithmes();
                    break;
                case "5":
                    afficherRegles();
                    break;
                case "6":
                case "x":
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide : entre un chiffre de 1 a 6.");
            }
        }
        System.out.println("A bientot !");
    }

    public static void afficherMenu() {
        System.out.println("\n========== SOKOBAN ==========");
        System.out.println("Niveau selectionne : " + (niveauChoisi + 1));
        System.out.println("1. Jouer au niveau selectionne");
        System.out.println("2. Choisir un niveau");
        System.out.println("3. Resoudre automatiquement (DFS ou BFS)");
        System.out.println("4. Comparer DFS et BFS");
        System.out.println("5. Lire les regles");
        System.out.println("6. Quitter");
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

    // ===== PARTIE AVANCEE : un etat est une photographie du jeu =====
    // Le terrain fixe reste commun. Le texte contient seulement @, $ et des espaces.
    // Deux caisses ne portent pas de numero : permuter deux caisses ne cree pas un etat different.
    static final char[] TOUCHES = {'z', 's', 'q', 'd'};
    static final int[] DELTA_LIGNES = {-1, 1, 0, 0};
    static final int[] DELTA_COLONNES = {0, 0, -1, 1};
    static final int MAX_ETATS = 100_000;
    static int limiteEtats = MAX_ETATS;

    // Toutes les structures de recherche sont implementees avec des tableaux.
    static String[] etatsRecherche;
    static int[] parentsRecherche;
    static char[] coupsRecherche;
    static int[] frontiere;
    static int[] visites;
    static int nombreEtats;
    static int debutFrontiere;
    static int finFrontiere;
    static int etatsExplores;
    static int etatsBloques;
    static boolean limiteAtteinte;
    static double tempsMillisecondes;
    static String statutRecherche;

    // Historique pour le bonus "annuler le dernier mouvement".
    static String[] historique;
    static int tailleHistorique;

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

    public static void memoriserMouvement() {
        if (tailleHistorique == historique.length) {
            String[] agrandi = new String[historique.length * 2];
            System.arraycopy(historique, 0, agrandi, 0, historique.length);
            historique = agrandi;
        }
        historique[tailleHistorique++] = encoderEtat();
    }

    public static boolean annulerMouvement() {
        if (tailleHistorique == 0) {
            return false;
        }
        restaurerEtat(historique[--tailleHistorique]);
        historique[tailleHistorique] = null;
        mouvements--;
        return true;
    }

    // Calcule un voisin SANS modifier la vraie partie. null = mouvement impossible.
    public static String etatSuivant(String etat, int direction) {
        int largeur = terrain[0].length;
        int position = etat.indexOf('@');
        int ligne = position / largeur;
        int colonne = position % largeur;
        int prochaineLigne = ligne + DELTA_LIGNES[direction];
        int prochaineColonne = colonne + DELTA_COLONNES[direction];
        if (!dansGrille(prochaineLigne, prochaineColonne)
                || terrain[prochaineLigne][prochaineColonne] == '#') {
            return null;
        }
        int prochainePosition = prochaineLigne * largeur + prochaineColonne;
        char[] contenu = etat.toCharArray();
        if (contenu[prochainePosition] == '$') {
            int derriereLigne = prochaineLigne + DELTA_LIGNES[direction];
            int derriereColonne = prochaineColonne + DELTA_COLONNES[direction];
            if (!dansGrille(derriereLigne, derriereColonne)
                    || terrain[derriereLigne][derriereColonne] == '#'
                    || contenu[derriereLigne * largeur + derriereColonne] == '$') {
                return null;
            }
            contenu[derriereLigne * largeur + derriereColonne] = '$';
        }
        contenu[position] = ' ';
        contenu[prochainePosition] = '@';
        return new String(contenu);
    }

    public static boolean etatGagnant(String etat) {
        int largeur = terrain[0].length;
        for (int position = 0; position < etat.length(); position++) {
            if (etat.charAt(position) == '$'
                    && terrain[position / largeur][position % largeur] != '.') {
                return false;
            }
        }
        return true;
    }

    public static boolean murOuBord(int ligne, int colonne) {
        return !dansGrille(ligne, colonne) || terrain[ligne][colonne] == '#';
    }

    // Bonus : une caisse hors objectif coincee entre deux murs perpendiculaires
    // ne peut plus etre poussee hors du coin. Les autres blocages ne sont pas detectes.
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

    // File FIFO et pile LIFO partagent un tableau mais retirent a des endroits differents.
    // BFS : lire au debut. DFS : retirer a la fin. Ajout toujours a la fin.
    public static String resoudreBFS() {
        return rechercher(true);
    }

    public static String resoudreDFS() {
        return rechercher(false);
    }

    public static String rechercher(boolean largeur) {
        long debutTemps = System.nanoTime();
        int capacite = Math.min(1024, limiteEtats);
        if (capacite < 1) {
            throw new IllegalArgumentException("La limite de recherche doit etre positive.");
        }
        etatsRecherche = new String[capacite];
        parentsRecherche = new int[capacite];
        coupsRecherche = new char[capacite];
        frontiere = new int[capacite];
        // Au moins deux cases de hachage par etat : on garde des emplacements libres.
        int capaciteVisites = 1;
        while (capaciteVisites < limiteEtats * 2) {
            capaciteVisites *= 2;
        }
        visites = new int[capaciteVisites];
        nombreEtats = 0;
        debutFrontiere = 0;
        finFrontiere = 0;
        etatsExplores = 0;
        etatsBloques = 0;
        limiteAtteinte = false;
        statutRecherche = "IMPOSSIBLE";
        String solution = null;
        enregistrerEtat(encoderEtat(), -1, ' ');

        while (debutFrontiere < finFrontiere && !limiteAtteinte) {
            int indice;
            if (largeur) {
                indice = frontiere[debutFrontiere++]; // FIFO : premier entre, premier sorti.
            } else {
                indice = frontiere[--finFrontiere]; // LIFO : dernier entre, premier sorti.
            }
            String etat = etatsRecherche[indice];
            etatsExplores++;
            // Cas d'arret reussi : l'etat retire est une victoire.
            if (etatGagnant(etat)) {
                solution = reconstruireSolution(indice);
                statutRecherche = "TROUVEE";
                break;
            }
            if (etatBloque(etat)) {
                etatsBloques++;
                continue;
            }
            for (int i = 0; i < TOUCHES.length; i++) {
                // Une pile inverse l'ordre : inserer d,q,s,z fait explorer z en premier.
                int direction = largeur ? i : TOUCHES.length - 1 - i;
                String voisin = etatSuivant(etat, direction);
                if (voisin == null) {
                    continue;
                }
                if (etatBloque(voisin)) {
                    etatsBloques++;
                    continue;
                }
                enregistrerEtat(voisin, indice, TOUCHES[direction]);
                if (limiteAtteinte) {
                    break;
                }
            }
        }
        if (limiteAtteinte && solution == null) {
            statutRecherche = "LIMITE"; // Ce n'est PAS une preuve d'absence de solution.
        }
        tempsMillisecondes = (System.nanoTime() - debutTemps) / 1_000_000.0;
        return solution;
    }

    // Table de hachage maison, sans HashMap/HashSet : collisions par sondage lineaire.
    // Une case vaut 0 si libre, sinon indice de l'etat + 1.
    // On compare aussi le texte entier : deux hash identiques ne suffisent pas.
    public static void enregistrerEtat(String etat, int parent, char coup) {
        int caseTable = (etat.hashCode() & 0x7fffffff) % visites.length;
        while (visites[caseTable] != 0) {
            int indiceExistant = visites[caseTable] - 1;
            if (etatsRecherche[indiceExistant].equals(etat)) {
                return; // Deja decouvert : ne pas l'ajouter une seconde fois.
            }
            caseTable = (caseTable + 1) % visites.length;
        }
        if (nombreEtats == limiteEtats) {
            limiteAtteinte = true;
            return;
        }
        if (nombreEtats == etatsRecherche.length) {
            agrandirRecherche();
        }
        int indice = nombreEtats++;
        etatsRecherche[indice] = etat;
        parentsRecherche[indice] = parent;
        coupsRecherche[indice] = coup;
        visites[caseTable] = indice + 1;
        frontiere[finFrontiere++] = indice;
    }

    public static void agrandirRecherche() {
        int capacite = Math.min(etatsRecherche.length * 2, limiteEtats);
        String[] nouveauxEtats = new String[capacite];
        int[] nouveauxParents = new int[capacite];
        char[] nouveauxCoups = new char[capacite];
        int[] nouvelleFrontiere = new int[capacite];
        System.arraycopy(etatsRecherche, 0, nouveauxEtats, 0, nombreEtats);
        System.arraycopy(parentsRecherche, 0, nouveauxParents, 0, nombreEtats);
        System.arraycopy(coupsRecherche, 0, nouveauxCoups, 0, nombreEtats);
        System.arraycopy(frontiere, 0, nouvelleFrontiere, 0, finFrontiere);
        etatsRecherche = nouveauxEtats;
        parentsRecherche = nouveauxParents;
        coupsRecherche = nouveauxCoups;
        frontiere = nouvelleFrontiere;
    }

    // On remonte du but vers le depart grace aux indices parents, puis on remplit a l'envers.
    public static String reconstruireSolution(int indice) {
        int longueur = 0;
        for (int courant = indice; parentsRecherche[courant] != -1; courant = parentsRecherche[courant]) {
            longueur++;
        }
        char[] chemin = new char[longueur];
        int courant = indice;
        for (int position = longueur - 1; position >= 0; position--) {
            chemin[position] = coupsRecherche[courant];
            courant = parentsRecherche[courant];
        }
        return new String(chemin);
    }

    public static void afficherResultat(String algorithme, String solution) {
        System.out.println("\n--- " + algorithme + " ---");
        if (solution != null) {
            System.out.println("Solution trouvee : oui");
            System.out.println("Mouvements : " + solution.length());
        } else if (statutRecherche.equals("LIMITE")) {
            System.out.println("Recherche interrompue : limite de " + limiteEtats + " etats decouverts.");
            System.out.println("Existence d'une solution : indeterminee.");
        } else {
            System.out.println("Solution trouvee : non (recherche terminee)");
        }
        System.out.println("Etats explores (retires de la file/pile) : " + etatsExplores);
        System.out.println("Etats decouverts (uniques memorises) : " + nombreEtats);
        System.out.println("Configurations bloquees ecartees (avec repetitions possibles) : " + etatsBloques);
        System.out.printf(java.util.Locale.ROOT, "Temps : %.3f ms%n", tempsMillisecondes);
    }

    public static void resoudreAutomatiquement() {
        System.out.print("\n1. DFS (profondeur) | 2. BFS (largeur) | x. Retour : ");
        String choix = lireCommande();
        if (choix.equals("x")) {
            return;
        }
        if (!choix.equals("1") && !choix.equals("2")) {
            System.out.println("Choix invalide : 1, 2 ou x.");
            return;
        }
        chargerNiveau(niveauChoisi);
        boolean largeur = choix.equals("2");
        String solution = largeur ? resoudreBFS() : resoudreDFS();
        afficherResultat(largeur ? "BFS" : "DFS", solution);
        if (solution == null) {
            return;
        }
        System.out.println("Solution a jouer (z : haut, s : bas, q : gauche, d : droite) :");
        if (solution.isEmpty()) {
            System.out.println("Aucun mouvement : le niveau est deja resolu.");
        }
        for (int i = 0; i < solution.length(); i += 70) {
            System.out.println(solution.substring(i, Math.min(i + 70, solution.length())));
        }
        System.out.print("v : voir la solution pas a pas | x : menu : ");
        if (lireCommande().equals("v")) {
            rejouerSolution(solution);
        }
    }

    public static void rejouerSolution(String solution) {
        afficherGrille();
        for (int i = 0; i < solution.length(); i++) {
            System.out.print("Etape " + (i + 1) + "/" + solution.length()
                + " : " + solution.charAt(i) + " | Entree : avancer, x : menu : ");
            String choix = lireCommande();
            if (choix.equals("x")) {
                return;
            }
            if (!choix.isEmpty()) {
                System.out.println("Appuie sur Entree ou tape x.");
                i--;
                continue;
            }
            int direction = 0;
            while (TOUCHES[direction] != solution.charAt(i)) {
                direction++;
            }
            if (!deplacerJoueur(DELTA_LIGNES[direction], DELTA_COLONNES[direction])) {
                throw new IllegalStateException("La solution contient un mouvement impossible.");
            }
            afficherGrille();
        }
        if (partieTerminee()) {
            System.out.println("Victoire ! Solution rejouee en " + mouvements + " mouvements.");
        }
    }

    public static void comparerAlgorithmes() {
        chargerNiveau(niveauChoisi);
        System.out.println("\n========== COMPARAISON : NIVEAU " + (niveauChoisi + 1) + " ==========");
        System.out.println("Meme depart, memes regles et meme detection des coins bloques.");
        String dfs = resoudreDFS();
        afficherResultat("DFS", dfs);
        String bfs = resoudreBFS();
        afficherResultat("BFS", bfs);
        System.out.println("\nDFS explore une branche en profondeur : sa solution peut etre longue.");
        System.out.println("BFS explore par nombre de mouvements croissant.");
        System.out.println("Si BFS trouve une solution, elle minimise le nombre de deplacements, poussees comprises.");
        System.out.println("Les temps varient selon la machine et l'echauffement de Java.");
    }

}
