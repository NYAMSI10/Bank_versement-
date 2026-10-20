# Réponses aux questions

## Partie 1 – Exceptions standard
Avant la Partie 2, on utilise des exceptions standard non vérifiées : `IllegalArgumentException` pour un montant ≤ 0, un compte identique, un plafond dépassé ;
`IllegalArgumentException` (ou `NullPointerException` via `Objects.requireNonNull`) pour un compte `null` ; `IllegalStateException` pour des fonds insuffisants (l'état du compte ne permet pas l'opération).
Dans la version finale, seul le montant ≤ 0 reste en `IllegalArgumentException` : c'est une erreur de paramètre, pas une règle métier.

## Partie 3 – Pourquoi `finally` ?
`finally` s'exécute que le virement réussisse, échoue avec une exception attrapée ou même non attrapée. C'est l'endroit garanti pour le code de clôture.
Dans une vraie application on y libérerait : connexions à la base de données, transactions ouvertes, verrous (`Lock.unlock()`), fichiers/flux, sockets, et on y écrirait la trace d'audit.
(En pratique, pour les ressources `AutoCloseable`, on préfère `try-with-resources`, comme pour le `Scanner` dans `Main`.)

## Partie 4 – Analyse de la défaillance
1. Soldes finaux : source = 1 500 €, destinataire inchangé. 500 € ont « disparu ».
2. Non. `try/catch` ne fait qu'intercepter l'exception ; il n'annule rien de ce qui a déjà été exécuté.
3. Non : la somme totale des comptes a diminué de 500 €, le système est incohérent.
4. Il faut rendre l'opération atomique : valider tout avant de modifier, puis soit compenser (re-créditer la source si le crédit échoue), soit utiliser une vraie transaction (commit/rollback).

## Partie 5 – Justification du périmètre
- On n'entoure pas toute la méthode avec `catch (Exception e)` : les exceptions de validation n'ont rien à annuler et doivent remonter telles quelles.
- Le `try` couvre uniquement `credit()`, l'étape risquée après le débit.
- On attrape `RuntimeException` (panne imprévue), on compense, puis on **relance** une `TransferFailedException` avec la cause d'origine : l'erreur n'est jamais avalée.
- Si la compensation échoue aussi, l'erreur secondaire est ajoutée via `addSuppressed` et `isRolledBack()` vaut `false` pour signaler une intervention manuelle.

## Partie 6 – Option A ou B ?
- **Application utilisateur → Option B** : chaque cas a un message et une action différents (« il manque 120 € », « plafond de 10 000 € »…). On peut finir par un `catch (BankingException e)` comme filet de sécurité pour les futurs cas.
- **Journalisation → Option A** : le logger traite toutes les erreurs métier pareil (niveau, message, stack trace) ; un seul `catch` suffit.
- **Contrôleur API → plutôt A avec une table de correspondance** (ex. `@ExceptionHandler` / `ControllerAdvice`) : un gestionnaire central traduit chaque sous-type en code HTTP (422 fonds insuffisants, 400 compte invalide…), ce qui évite de dupliquer les `catch` dans chaque endpoint.

## Questions de discussion
1. **Exception spécifique vs `throw Exception`** : le type porte le sens de l'erreur, permet un `catch` ciblé, transporte des données utiles, et ne force pas l'appelant à attraper des erreurs qu'il ne sait pas gérer.
2. **`BankingException extends RuntimeException`** : elle deviendrait non vérifiée ; le compilateur n'obligerait plus à la déclarer ni à la traiter. Code plus léger, mais risque d'oublier un cas métier prévisible.
3. **Lever / déclarer / gérer** : *lever* = `throw new X()` crée et lance l'exception ; *déclarer* = `throws X` dans la signature annonce qu'elle peut sortir de la méthode ; *gérer* = `try/catch` l'intercepte et réagit.
4. **`catch (Exception e)` systématique** : il mélange bugs (`NullPointerException`), erreurs techniques et métier, empêche un traitement adapté, masque souvent des erreurs (catch vide), et attrape des exceptions qu'on n'avait pas prévues.
5. **Technique vs métier au même niveau ?** Non. Les erreurs métier se gèrent près de l'utilisateur (message, nouvelle saisie). Les erreurs techniques (panne BDD, réseau) remontent vers une couche centrale (log, alerte, retry, message générique).
6. **Intercepter garantit-il la cohérence ?** Non (cf. Partie 4). Le `catch` ne défait rien : la cohérence vient de la conception (validation préalable, compensation, transactions).
7. **En production** : les **transactions de base de données** (ACID, `commit`/`rollback`, ex. `@Transactional` avec Spring/JPA). Pour plusieurs systèmes distribués : commit à deux phases ou pattern **Saga** (compensations orchestrées).
