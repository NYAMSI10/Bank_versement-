# Virements bancaires – stratégie de gestion des exceptions

- **Validation d'abord** : `TransferService.transfer()` vérifie toutes les règles (montant, comptes, plafond, solde) *avant* de toucher aux comptes ; un rejet ne laisse donc aucune trace.
- **Deux familles d'exceptions** : les erreurs de paramètre (montant ≤ 0) lèvent `IllegalArgumentException` (non vérifiée) ; les règles métier lèvent des sous-classes de `BankingException` (vérifiée, déclarée avec `throws`).
- **Exceptions riches** : chaque exception métier porte ses données (solde, montant manquant, plafond) pour permettre un message précis côté appelant.
- **Compensation** : seul `destination.credit()` est entouré d'un `try`, car c'est la seule étape qui peut échouer après une modification. En cas de `RuntimeException`, le débit est annulé, puis une `TransferFailedException` est levée avec la cause d'origine (et `addSuppressed` si la compensation échoue elle aussi).
- **Côté appelant** : `Main` utilise un `catch` par cas (saisie invalide, fonds, plafond, compte, panne) et un `finally` qui s'exécute dans tous les cas.
- Tests : `java TransferServiceTest` rejoue les 7 scénarios de l'énoncé.
