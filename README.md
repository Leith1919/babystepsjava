# 👶 BabySteps – Votre grossesse en toute sérénité

---

## 🌟 Description du projet

**BabySteps** est une application complète de suivi de grossesse et de suivi bébé destinée aux **femmes enceintes** et aux **médecins**.  
Le projet est divisé en deux parties complémentaires :

- Une **application Web** développée avec **Symfony** 🖥️
- Une **application Desktop** développée en **JavaFX** ☕  
Les deux applications sont connectées à une **base de données MySQL commune**, garantissant la synchronisation des données.

> 🎯 Objectif : Offrir une plateforme innovante, intelligente et intuitive pour assurer un **suivi médical sécurisé** pendant la grossesse et après l’accouchement.

---

## 🧩 Modules Fonctionnels

### 👩‍🍼 Suivi de grossesse
- Ajout et consultation de poids, tension, symptômes
- Recommandations médicales personnalisées
- Détection d’anomalies via **intelligence artificielle (Flask API)**

### 👶 Suivi bébé
- Suivi de poids, taille, alimentation
- Observations médicales associées à chaque étape post-natale

### 📅 Rendez-vous & Disponibilités
- Prise de rendez-vous par la patiente
- Gestion de disponibilités par le médecin

### 💊 Traitements & Ordonnances
- Saisie par le médecin depuis JavaFX
- Consultable par la patiente en ligne

### 📰 Blog & Commentaires
- Articles informatifs sur la santé maternelle
- Interaction via des commentaires

### 📢 Forum
- Espace d’échange entre patientes

### 🏥 Réservation de chambres
- Système de réservation dans les établissements partenaires

### 📈 Statistiques médicales
- Visualisation des suivis et alertes
- Graphiques de l’évolution poids/tension

---

## 🛠️ Technologies utilisées

| Composant         | Technologies                                 |
|-------------------|----------------------------------------------|
| Backend Web       | PHP, Symfony, Doctrine ORM, MySQL            |
| Frontend Web      | Twig, HTML/CSS, JavaScript, Chart.js         |
| Application Desktop | Java, JavaFX, JDBC                         |
| IA/API            | Python, Flask, modèle IA (détection anomalies) |
| QR Code (optionnel) | ZXing (Java) / KnpSnappyBundle (PDF)       |

---

## 🔁 Architecture d'intégration

- ✅ Base de données **MySQL partagée**
- ✅ **Appels API Flask** depuis Symfony pour l’analyse intelligente
- ✅ **Synchronisation** des modules entre Web et Java
- ✅ Communication fluide et cohérente

---


## ⚙️ Installation rapide

### 💻 Partie Web

```bash
git clone https://github.com/tonutilisateur/babysteps-web.git
cd babysteps-web
composer install
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate
symfony server:start
