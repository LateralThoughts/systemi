Manuel de l'utilisateur
=====================================

### Se connecter à l'application

Pour se connecter à l'application, il faut utiliser son compte google @lateral-thoughts.com. 

Présentation de l'application
------

### Page d'accueil

Voici la page d'accueil actuelle

![Écran d'accueil](/doc/images/home.png?raw=true "L'écran d'accueil")

À moyen terme, sur cette page seront affichés les statistiques concernant l'utilisateur connecté (Frais, Variable...) et
concernant la situation financière de l'entreprise

On peut voir les liens suivants dans la barre de menu au-dessus :
* **Factures** : création et gestion des factures émises par LT envers ses clients. Les factures sont des rentrées d'argent
* **Cra** :  création et gestion des compte-rendus d'activité que certains clients nous demandent.
* **Sortie** : les dépenses des membres 
* **Intendance** : gestion plus fine de la boite, ce menu déroulant se décompose en :
  * **Biz Points** : la gestion des points business, c'est à dire des actions que les membres de LT ont fait envers la boite et qui mérite rétribution
  * **Clients** : la création et la mise à jour des informations clients
  * **Membres** : la gestion des membres et de leurs budgets
  * **Config** : la configuration des ratios de LT, par exemple quel pourcentage part en budget Timeoff, en budget frais...
  * **Mouvement** : la gestion des mouvements d'argent entre les membres de LT
  
### Factures

Sur la page des factures, il existe 4 onglets :
* **Liste des factures**, qui liste toutes les factures
* **Créer une facture**, permettant de créer une facture
* **En retard de paiement**, listant toutes les factures en retard de paiement
* **Numérotation**, permettant d'administrer la numérotation des factures

#### Liste des factures

![Factures](/doc/images/invoice.png?raw=true "liste des factures")

La page factures s'ouvre sur la liste des factures. On peut voir :
* le nom, le numéro, le client et les montant Hors Taxe et avec TVA de la facture dans la colonne "project"
* l'historique des status de la facture dans la colonne "status"
* les actions possibles sur la factures dans la colonne "actions"

Il existe des filtres sur les factures :
* Filtre sur l'éditeur, c'est à dire celui qui a créé la facture
* Filtre sur le client, c'est l'entité qui doit payer la facture
* Filtre sur les status de la facture, qui sont détaillés ci-dessous

Les actions disponibles sur la facture sont au nombre de cinq :
* Afficher la facture (PDF) : affiche le fichier PDF de la facture dans un modal
* Affecter la facture : lier la facture à un budget (appartenant à un membre LT), le status de la facture passera alors à "Affectée"
* Réaffecter la facture : lier la facture à un budget (appartenant à un membre LT), le status de la facture ne change pas
* Le paiement a échoué : annule le paiement, le status de la facture passera alors à "Affectée"
* Annuler la facture : Annule la facture. Dans ce cas le PDF de la facture sera barré avec un filigrane "ANNULÉE" et le status de la facture sera annulée.

Pour plus de détail sur les changement de status, voir la section "Status de la facture" ci-dessous

##### Status de la facture

Les factures ont 4 status qui sont les suivants :
* Créée (Created) : la facture est créée, c'est le premier status
* Affectée (Allocated) : la facture a été affectée à un budget
* Payée (Paid) : la facture a été payée
* Annulée (Canceled) : la facture a été annulée

À ces 4 status de base s'ajoute deux metastatus qui sont :
* En cours (In Progress) : la facture est soit créée, soit affectée
* Terminée (Finished) : la facture est soit payée, soit annulée

Les changement de status sont explicités sur le schéma suivant :

![Status](/doc/images/statuses.png?raw=true "Schéma des status")



#### Créer une facture

![Factures](/doc/images/invoice_create.png?raw=true "création d'une facture")

La page de création de facture permet de créer une facture. Pour cela il faut remplir les champs de formulaire :
* Numéro de la facture : le numéro de la facture, par exemple VT201. Ce champ n'est pas modifiable. Pour le modifier, voir la section "Numérotation"
* Titre de la facture : le titre de la facture, par exemple "Jean Dubois Octobre 2014" ou "Formation MongoDB 21-22 avril 2014"
* Avec ou sans taxes : si le client doit payer la TVA (cela dépend de la culture du pays du client).
* Client : la personne physique ou morale qui paiera la facture. Peut-être recherché si le client a déjà été enregistré, sinon il est possible de compléter les informations clients "one shot".

Pour les lignes de facturation :
* Description : description de la prestation, par exemple le classique "Prestations de Services Informatiques"
* Jours : le nombre de jours de la prestation
* TJM : le tarif journalier de la prestation en euros hors taxe
* TVA : la TVA, par défaut 20%

En cliquant sur le bouton "Générer la facture et upload" vous serez redirigé vers la page listant les factures avec un modal ouvert sur le fichier PDF de la facture.

#### En retard de paiement

Page listant toutes les factures en retard de paiement.

#### Numérotation

Page permettant d'administrer la numérotation des factures. Sur cette page on peut :
* Voir le numéro actuelle de la facture
* Incrémenter ce numéro
* Réinitialiser ce numéro

Attention, dans le cadre de l'utilisation normal de l'application, il n'est pas nécessaire de réinitialiser les numéros
de factures. Si vous avez raté l'édition d'une facture, il faut l'annuler et recréer une facture avec le numéro suivant
  
Glossaire
------

### Membre

Un membre de LT est soit une personne physique ou l'entité LT en elle même. Un membre peut avoir des factures affectées, 
peut avoir différents budgets (timeoff, frais...)

### Budget

Un budget correspond à une ligne de compte sur l'ancien tableau "compta-double"

### Facture

Une facture est une rentrée d'argent pour LT. Une facture est émise de LT vers un client. Une facture est affectée à un membre, 
c'est sur son compte que sera versé l'argent gagné lors du paiement de la facture. Une facture peut être non-affectée, 
en cours de payment, payée ou annulée.

#### Éditeur

La personne qui a créée la facture

### Activité

Une activité est le compte rendu d'un travail de prestation effectué chez un client. À partir d'une activité on peut générer
un compte rendu d'activité (justification de la prestation) et une facture (paiement de la prestation)

### Sortie/Dépense

Une sortie (dépense) est une sortie d'argent de LT vers une autre entité. Une sortie est affectée à un membre sur un budget particulier.
Par exemple, on peut imaginer affecter la location d'une maison pour un timeoff au budget "timeoff" du membre "LT"

### Mouvement

Un mouvement est un transfert d'argent entre deux membres de LT ou entre deux budgets d'un même membre. 

### Client

Un client est une entité que LT facture. 
