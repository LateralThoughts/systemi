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

#### Liste des factures

![Factures](/doc/images/invoice.png?raw=true "liste des factures")

La page factures s'ouvre sur la liste des factures non affectées. On peut voir :
* le nom et le numéro de la facture dans la colonne "project"
* l'historique des status de la facture dans la colonne "status"
* les actions possibles sur la factures dans la colonne "actions"

Les actions disponibles sur la facture sont au nombre de trois :
* Voir la facture : récupérer le fichier PDF de la facture
* Affecter la facture : lier la facture à un membre LT, le status de la facture passera alors soit à "en cours de paiement", soit à "payé"
* Annuler la facture : Annule la facture. Dans ce cas le PDF de la facture sera barré avec un filigrane "ANNULÉE" et le status de la facture sera annulée.

#### Créer une facture

![Factures](/doc/images/invoice_create.png?raw=true "création d'une facture")

La page de création de facture permet de créer une facture. Pour cela il faut remplir les champs de formulaire :
* Numéro de la facture : le numéro de la facture, par exemple VT201
* Titre de la facture : le titre de la facture, par exemple "Jean Dubois Octobre 2014" ou "Formation MongoDB 21-22 avril 2014"
* Avec ou sans taxes : si le client doit payer la TVA (cela dépend de la culture du pays du client).
* Client : la personne physique ou morale qui paiera la facture. Peut-être recherché si le client a déjà été enregistré, sinon il est possible de compléter les informations clients "one shot".

Pour les lignes de facturation :
* Description : description de la prestation, par exemple le classique "Prestations de Services Informatiques"
* Jours : le nombre de jours de la prestation
* TJM : le tarif journalier de la prestation en euros hors taxe
* TVA : la TVA, par défaut 20%

On peut également cocher ou décocher la case "Uploader la facture dans le système". Si cette case est décoché, cela veut dire
que la facture ne sera pas enregistrée dans mongo et ne sera pas envoyée sur le drive. Il ne faut décocher la case que si
vous souhaitez faire des tests de génération de facture.

En cliquant sur le bouton "Générer la facture et upload" (ou seulement "générer la facture") vous serez redirigé vers
le fichier PDF de la facture.

  
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
