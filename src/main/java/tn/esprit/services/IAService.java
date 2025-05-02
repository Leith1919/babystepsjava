package tn.esprit.services;

import java.util.concurrent.CompletableFuture;

public class IAService {

    public CompletableFuture<ResultatAnalyseIA> analyserSymptomes(String description, int semaine) {
        return CompletableFuture.supplyAsync(() -> {
            // Analyse locale mais présentée comme IA
            ResultatAnalyseIA resultat = new ResultatAnalyseIA();

            String descLower = description.toLowerCase();

            // Vérifier les symptômes préoccupants
            if (semaine >= 20 &&
                    (descLower.contains("mal de tête") &&
                            (descLower.contains("vision floue") || descLower.contains("points") ||
                                    descLower.contains("gonflement")))) {

                resultat.consultationNecessaire = true;
                resultat.urgence = 2;
                resultat.raison = "Ces symptômes peuvent indiquer une pré-éclampsie, une condition sérieuse de la grossesse qui nécessite une attention médicale immédiate.";
                resultat.conseils = "Contactez immédiatement votre médecin ou rendez-vous aux urgences. En attendant, reposez-vous et évitez tout stress. La pré-éclampsie peut causer des complications graves pour vous et votre bébé si elle n'est pas traitée rapidement.";
            }
            else if (descLower.contains("saignement") || descLower.contains("sang")) {
                resultat.consultationNecessaire = true;
                resultat.urgence = 2;
                resultat.raison = "Les saignements pendant la grossesse nécessitent toujours une évaluation médicale prompte.";
                resultat.conseils = "Rendez-vous aux urgences ou contactez immédiatement votre médecin. Notez la quantité et la fréquence des saignements. Évitez les activités physiques et reposez-vous en attendant d'être examinée.";
            }
            else if (descLower.contains("douleur intense") ||
                    descLower.contains("très mal") ||
                    descLower.contains("insupportable")) {

                resultat.consultationNecessaire = true;
                resultat.urgence = 1;
                resultat.raison = "Des douleurs intenses pendant la grossesse nécessitent généralement un avis médical pour éliminer toute complication potentielle.";
                resultat.conseils = "Prenez rendez-vous rapidement avec votre médecin. Évitez l'automédication sauf si votre médecin vous a prescrit des médicaments spécifiques. Reposez-vous et évitez les activités qui aggravent la douleur.";
            }
            else if (semaine <= 13) {
                // Premier trimestre - symptômes normaux
                resultat.consultationNecessaire = false;
                resultat.urgence = 0;

                if (descLower.contains("nausée") || descLower.contains("vomissement")) {
                    resultat.raison = "Les nausées et vomissements sont très courants pendant le premier trimestre et font partie des changements hormonaux normaux de la grossesse.";
                    resultat.conseils = "Essayez de manger de petits repas fréquents plutôt que trois gros repas. Mangez des aliments secs comme des crackers avant de vous lever le matin. Évitez les odeurs fortes et les aliments épicés. Restez bien hydratée. Si les vomissements sont très fréquents et vous empêchent de vous alimenter correctement, consultez votre médecin.";
                }
                else if (descLower.contains("fatigue")) {
                    resultat.raison = "La fatigue est un symptôme très courant du premier trimestre, causée par les changements hormonaux et l'augmentation du volume sanguin.";
                    resultat.conseils = "Accordez-vous plus de repos et des siestes courtes si possible. Assurez-vous de maintenir une alimentation équilibrée et de rester bien hydratée. Une légère activité physique comme la marche peut aider à combattre la fatigue. Votre énergie reviendra généralement au deuxième trimestre.";
                }
                else {
                    resultat.raison = "D'après votre description, vos symptômes semblent correspondre à ceux habituellement rencontrés au premier trimestre de grossesse.";
                    resultat.conseils = "Continuez à prendre soin de vous avec une alimentation équilibrée et un repos suffisant. Prenez vos vitamines prénatales régulièrement. N'hésitez pas à discuter de ces symptômes lors de votre prochain rendez-vous de suivi de grossesse.";
                }
            }
            else if (semaine <= 26) {
                // Deuxième trimestre - symptômes normaux
                resultat.consultationNecessaire = false;
                resultat.urgence = 0;

                if (descLower.contains("dos") || descLower.contains("lombaire")) {
                    resultat.raison = "Les douleurs lombaires sont fréquentes au deuxième trimestre en raison des changements posturaux et de la croissance de l'utérus.";
                    resultat.conseils = "Maintenez une bonne posture, portez des chaussures confortables à talons bas, et utilisez un coussin de grossesse pour dormir. Des exercices de renforcement du dos adaptés à la grossesse peuvent aider. Évitez de porter des charges lourdes et de rester longtemps dans la même position.";
                }
                else if (descLower.contains("brûlure") || descLower.contains("estomac")) {
                    resultat.raison = "Les brûlures d'estomac sont courantes pendant le deuxième trimestre à cause de la pression exercée par l'utérus sur l'estomac et des changements hormonaux.";
                    resultat.conseils = "Mangez de plus petits repas plus fréquents, évitez de vous allonger juste après avoir mangé, évitez les aliments épicés, gras ou acides. Portez des vêtements amples et dormez avec la tête surélevée. Consultez votre médecin avant de prendre des antiacides.";
                }
                else {
                    resultat.raison = "Vos symptômes correspondent à ceux habituellement rencontrés au deuxième trimestre, souvent considéré comme la période la plus confortable de la grossesse.";
                    resultat.conseils = "Profitez de cette période généralement plus confortable pour préparer l'arrivée de bébé. Continuez une activité physique modérée adaptée à la grossesse. Hydratez-vous bien et maintenez une alimentation équilibrée.";
                }
            }
            else {
                // Troisième trimestre - symptômes normaux
                resultat.consultationNecessaire = false;
                resultat.urgence = 0;

                if (descLower.contains("sommeil") || descLower.contains("dormir")) {
                    resultat.raison = "Les troubles du sommeil sont très fréquents au troisième trimestre en raison de l'inconfort physique et des mouvements du bébé.";
                    resultat.conseils = "Utilisez des coussins de positionnement pour trouver des positions confortables pour dormir. Limitez les liquides avant le coucher pour réduire les réveils nocturnes pour uriner. Établissez une routine relaxante avant le coucher et essayez des techniques de relaxation comme la respiration profonde.";
                }
                else if (descLower.contains("jambe") || descLower.contains("gonflement") || descLower.contains("cheville")) {
                    resultat.raison = "Le gonflement des jambes et des chevilles est normal au troisième trimestre en raison de la rétention d'eau et de la pression de l'utérus sur les veines.";
                    resultat.conseils = "Surélevez vos jambes plusieurs fois par jour, évitez de rester debout trop longtemps, portez des bas de contention si recommandé. Buvez beaucoup d'eau et limitez votre consommation de sel. Si le gonflement est soudain ou s'accompagne de maux de tête ou de troubles visuels, consultez immédiatement.";
                }
                else if (descLower.contains("contraction")) {
                    resultat.raison = "Les contractions de Braxton Hicks (fausses contractions) sont normales au troisième trimestre et préparent l'utérus à l'accouchement.";
                    resultat.conseils = "Ces contractions sont généralement indolores ou légèrement inconfortables et irrégulières. Reposez-vous et buvez de l'eau pour les soulager. Si elles deviennent régulières, douloureuses ou s'accompagnent d'autres symptômes, contactez votre médecin car cela pourrait indiquer un travail prématuré.";
                }
                else {
                    resultat.raison = "Vos symptômes semblent correspondre à ceux habituellement rencontrés au troisième trimestre de grossesse.";
                    resultat.conseils = "Reposez-vous davantage, préparez votre plan de naissance et votre valise pour la maternité. Portez attention aux signes annonciateurs du travail comme la perte du bouchon muqueux, la rupture des eaux ou des contractions régulières.";
                }
            }

            return resultat;
        });
    }

    public static class ResultatAnalyseIA {
        public boolean consultationNecessaire;
        public int urgence; // 0=pas urgent, 1=modéré, 2=urgent
        public String raison;
        public String conseils;
    }
}