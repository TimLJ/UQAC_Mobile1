# CatWalk

## Projet réalisé par :
- Timothée Le Jemtel
- Léa Rissel
- Maxime Marecesche

# Structure de l'application
## Des Scaffold
La majorité des activités sont composées d'un Scaffold afin de retrouver le même format un peu partout :
- Un header qui contient le bouton pour ramener à l'écran d'accueil si l'utilisateur n'y est pas déjà, un bouton pour les succès, une barre d'expérience, ainsi que le montant de pièces que possède le joueur.
- Le contenu principal de l'activité actuelle
- Un footer qui contient des boutons pour naviguer sur les activités "Boutique", "Chats", et "Marche", sauf dans l'activité d'interaction avec un chat, où ils sont remplacés par "Jouer", "Laver" et "Caresser" et ne mènent nulle part.

Les seules exceptions sont  l'activité de marche et le bilan de la balade, qui ne possèdent pas de header ni de footer.

## Gestion de la base de donnée
L'application possède une base de donnée interne qui utilise Room, qui ne demande pas de se connecter à Internet. Elle est séparée en plusieurs parties : 
- Un dossier "entities" qui regroupe toutes les classes des entitées utiliées dans la base de données (Chat, Succès)
- Un dossier "dao" qui contient les fonctions permettant de manipuler (get, update) les données de chaque entitée.
- Une classe abstraite AppDatabase qui hérite de Room et qui va permettre d'accéder à la base de donnée, de la peupler, etc...
- Un dataStoreMAnager qui sert à sauvegarder les informations du joueur

## Dossier dédiés à des aspecs de l'app
- Un dossier walk qui gère tout ce qui est lié aux balades (logique, UI)
- Un dossier UI qui contient les éléments clefs des vsuels de l'appli
