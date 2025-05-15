# 👩‍⚕️ README - Suivi Grossesse & Suivi Bébé (JavaFX Desktop Version)

## 🌟 Description

Cette application JavaFX permet aux **médecins** de gérer le suivi médical des femmes enceintes et des nourrissons. Elle s’intègre dans un écosystème complet où la partie web (Symfony) est utilisée par les patientes pour consulter leurs suivis.

L'application repose sur une base de données **MySQL** partagée avec le site web. Elle suit une architecture **MVC (Model-View-Controller)** claire, facilitant l’évolution et la maintenance.

---

## 🔢 Tables Utilisées

### Table `suivi_grossesse`

| Champ               | Type     | Description                                 |
|---------------------|----------|---------------------------------------------|
| id                  | int      | Identifiant unique                          |
| patient_id          | int      | Clé étrangère vers la patiente              |
| date_suivi          | date     | Date du suivi                               |
| poids               | float    | Poids de la mère                            |
| tension             | varchar  | Tension artérielle                          |
| symptomes           | text     | Symptômes déclarés                          |
| recommandations     | text     | Recommandations données par le médecin      |

### Table `suivi_bebe`

| Champ               | Type     | Description                                 |
|---------------------|----------|---------------------------------------------|
| id                  | int      | Identifiant unique                          |
| grossesse_id        | int      | Clé étrangère vers `suivi_grossesse`        |
| date_suivi          | date     | Date du suivi bébé                          |
| poids               | float    | Poids du bébé                               |
| taille              | float    | Taille du bébé                              |
| observations        | text     | Observations médicales                      |

---

## 🔧 Fonctions implémentées (JavaFX)

### 👨‍⚕️ Côté Médecin

* Gestion des **suivis grossesse**
  → Ajout, modification, suppression, consultation

* Gestion des **suivis bébé**
  → Association à un suivi grossesse, gestion complète

* Affichage **statistique** du suivi (graphiques poids, tension…)

* Affichage des **alertes critiques**
  → Détection d’anomalies (ex. : tension trop élevée)

* Connexion via **base de données centralisée**

---

## ✅ README - Suivi Grossesse & Suivi Bébé (Symfony Web Version)

# 👩‍🍼 README - Suivi Grossesse & Suivi Bébé (Symfony Web Version)

## 🌟 Description

La version Symfony permet aux **patientes** de consulter leurs suivis grossesse et bébé, ajouter des informations secondaires, et visualiser les **alertes médicales** générées par l’IA.  
Elle est connectée à la même base de données que l’application JavaFX utilisée par les médecins.

---

## 🔢 Tables Utilisées

Identiques à la version JavaFX (voir ci-dessus).

---

## 🔧 Fonctions implémentées (Symfony)

### 👩‍🍼 Côté Patiente

* Visualisation des **suivis grossesse**
  → Historique avec poids, tension, recommandations

* Visualisation des **suivis bébé**
  → Taille, poids, observations

* Alertes médicales (ex. : anomalie détectée)
* Espace personnel sécurisé par authentification

### 🛠️ Côté Médecin (Back-end admin)

* Gestion complète des suivis (CRUD)
* Filtrage des suivis par patient
* Visualisation des données saisies côté Web

---

## 🔄 Synchronisation entre les deux versions

* 💽 **Base de données MySQL commune** utilisée par Symfony & JavaFX
* 👨‍⚕️ **Médecins** utilisent l’interface JavaFX
* 👩‍🍼 **Patientes** utilisent le site Web Symfony
* 📡 Les modifications dans une plateforme sont visibles instantanément dans l’autre

---

## ✅ Bonus : QR Code (si implémenté)

* Un QR code est généré sur demande contenant :
  - Le dernier suivi grossesse
  - Le poids et taille bébé
  - À scanner par un médecin pour un accès rapide

---

Souhaites-tu maintenant que je te **génère ces README en fichiers `.md` téléchargeables** ?  
Ou préfères-tu que je t’aide à rédiger la **capsule vidéo** à partir de ces modules ?
