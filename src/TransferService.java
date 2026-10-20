public class TransferService {
    public static final double TRANSFER_LIMIT = 10_000;

    public void transfer(BankAccount source, BankAccount destination, double amount)
            throws InsufficientFundsException, TransferLimitExceededException,
                   InvalidAccountException, TransferFailedException {

        // 1. Validation complète AVANT toute modification des comptes
        validate(source, destination, amount);

        // 2. Exécution avec compensation : seul le crédit est protégé,
        source.debit(amount);
        try {
            destination.credit(amount);
        } catch (RuntimeException creditFailure) {
            // Panne technique inattendue : on annule le débit
            try {
                source.credit(amount);
            } catch (RuntimeException rollbackFailure) {
                creditFailure.addSuppressed(rollbackFailure);
                throw new TransferFailedException(
                        "Échec du virement ET de la compensation : le compte " + source.getAccountNumber()
                                + " doit être corrigé manuellement", creditFailure, false);
            }
            throw new TransferFailedException(
                    "Échec technique du virement, le compte source a été restauré", creditFailure, true);
        }
    }

    private void validate(BankAccount source, BankAccount destination, double amount)
            throws InsufficientFundsException, TransferLimitExceededException, InvalidAccountException {
        // Erreur de paramètre (bug ou saisie invalide) -> exception standard non vérifiée
        if (Double.isNaN(amount) || amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif : " + amount);
        }
        if (source == null || destination == null) {
            throw new InvalidAccountException("Compte source ou destinataire inexistant");
        }
        if (source == destination || source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new InvalidAccountException("Le compte source et le compte destinataire sont identiques");
        }
        if (amount > TRANSFER_LIMIT) {
            throw new TransferLimitExceededException(amount, TRANSFER_LIMIT);
        }
        if (amount > source.getBalance()) {
            throw new InsufficientFundsException(source.getAccountNumber(), source.getBalance(), amount);
        }
    }
}
