# Groupe B2-4/4  
NICOTTE Loann, VIRBEL Louis, RABIER Valentin

## Routes implémentées

### Routes d’ingress  
- **POST `/ingress/windturbine`** : reçoit les données des éoliennes (ID, timestamp, vitesse, puissance)  
- **UDP port 12345** : reçoit les données des panneaux solaires au format `id:temperature:power:timestamp`

### Routes API

#### Gestion des utilisateurs  
- **GET `/persons`** : liste tous les ID des utilisateurs  
- **GET `/person/:id`** : récupère les détails d’un utilisateur spécifique  
- **POST `/person/:id`** : met à jour un utilisateur existant  
- **DELETE `/person/:id`** : supprime un utilisateur  
- **PUT `/person`** : crée un nouvel utilisateur

#### Gestion des grilles  
- **GET `/grids`** : liste tous les ID des grilles  
- **GET `/grid/:id`** : récupère les détails d’une grille spécifique  
- **GET `/grid/:id/production`** : calcule la production totale d’une grille  
- **GET `/grid/:id/consumption`** : calcule la consommation totale d’une grille

#### Gestion des capteurs  
- **GET `/sensor/:id`** : récupère les détails d’un capteur spécifique  
- **GET `/sensors/:kind`** : liste tous les capteurs d’un type spécifique (SolarPanel, WindTurbine, EVCharger)  
- **GET `/consumers`** : liste tous les capteurs consommateurs  
- **GET `/producers`** : liste tous les capteurs producteurs  
- **POST `/sensor/:id`** : met à jour un capteur existant

#### Gestion des mesures  
- **GET `/measurement/:id`** : récupère les détails d’une mesure spécifique  
- **GET `/measurement/:id/values`** : récupère les valeurs d’une mesure, avec option de filtrage par période

## Tests pour les **GET** à partir du back-end (vérification via la BDD)

- **GET `/grids`** : renvoie bien la liste d’ID des grilles  
- **GET `/grid/:id`** : JSON complet de la grille `:id`  
- **GET `/grid/:id/production`** : renvoie bien la production de la grille `:id`  
- **GET `/grid/:id/consumption`** : renvoie bien la consommation de la grille `:id`  
- **GET `/persons`** : renvoie bien la liste d’ID des personnes  
- **GET `/person/:id`** : JSON complet de la personne `:id`  
- **GET `/sensor/:id`** : JSON complet du capteur `:id`  
- **GET `/sensors/:kind`** :  
  - `kind` = EVCharger | SolarPanel | WindTurbine → renvoie la liste d’ID des capteurs concernés  
- **GET `/consumers`** / **GET `/producers`** : renvoie la liste complète des JSON liés aux consommateurs ou producteurs  
- **GET `/measurement/:id`** : JSON de la mesure  
- **GET `/measurement/:id/values`**  
  - sans `from`/`to` → toutes les valeurs  
  - avec `from`/`to` → valeurs filtrées entre `from` et `to`

## Tests pour les **GET** à partir du front-end

Toutes les informations présentes sur le front-end ont été vérifiées via la BDD.

## Tests pour les **POST / PUT / DELETE** à partir du front-end

- **POST `/person/:id`**  
  - modification de tous les champs possible  
  - met bien à jour la liste des owners des sensors en cas de modification

- **PUT `/person`**  
  - l’ajout s’effectue correctement  
  - avec ou sans capteurs sélectionnés  
  - met bien à jour la liste des owners des sensors si un capteur est sélectionné
 
- **DELETE `/person/:id`**  
  - Supprime bien la personne
  - met bien à jour la liste des owners des capteurs liés

- **POST `/sensor/:id`**  
  - modification de tous les champs possible  
  - met bien à jour la liste des sensors des owners si une personne est sélectionné

- **POST `/ingress/windturbine`**  
  - ajoute bien un datapoint `speed`, `power` et `total_energy_produced` toutes les 60 secondes  
  - graphique sur le front-end en accord avec la BDD

- **UDP 12345 (solar panel)**  
  - ajoute bien un datapoint `temperature`, `power` et `total_energy_produced` toutes les 60 secondes  
  - graphique sur le front-end en accord avec la BDD

## Bogues résiduels identifiés

- Dans certains payloads, certains champs sont à null  
- Sur le front end, une erreur est renvoyée quand on modifie un capteur et que l’on ne sélectionne aucune personne  
